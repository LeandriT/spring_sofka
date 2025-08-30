package ec.com.sofka.account_service.service.impl;

import ec.com.sofka.account_service.dto.movement.request.MovementPartialUpdateRequest;
import ec.com.sofka.account_service.dto.movement.request.MovementRequest;
import ec.com.sofka.account_service.dto.movement.response.MovementResponse;
import ec.com.sofka.account_service.exception.BalanceTypeSigNumUnavailableException;
import ec.com.sofka.account_service.exception.InactiveAccountException;
import ec.com.sofka.account_service.exception.InsufficientFoundsException;
import ec.com.sofka.account_service.exception.MovementNotFoundException;
import ec.com.sofka.account_service.mapper.MovementMapper;
import ec.com.sofka.account_service.model.Account;
import ec.com.sofka.account_service.model.Movement;
import ec.com.sofka.account_service.model.enums.MovementTypeEnum;
import ec.com.sofka.account_service.repository.MovementRepository;
import ec.com.sofka.account_service.service.AccountService;
import ec.com.sofka.account_service.service.MovementService;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MovementServiceImpl implements MovementService {
    private final AccountService accountService;
    private final MovementRepository movementRepository;
    private final MovementMapper mapper;
    private static final String MOVEMENT_NOT_FOUND = "Movimiento no encontrado: ";

    @Override
    public Page<MovementResponse> index(Pageable pageable) {
        return movementRepository.findAll(pageable).map(mapper::toResponse);
    }

    @Override
    public Page<MovementResponse> byAccount(Long accountId, Pageable pageable) {
        return movementRepository.findByAccountIdOrderByDateDesc(accountId, pageable)
                .map(mapper::toResponse);
    }

    @Override
    public MovementResponse show(Long id) {
        Movement movement = movementRepository.findById(id)
                .orElseThrow(() -> new MovementNotFoundException(MOVEMENT_NOT_FOUND + id));
        return mapper.toResponse(movement);
    }

    @Override
    @Transactional
    public MovementResponse create(MovementRequest request) {
        log.info("Starting create movement");
        this.validateTransactionType(request);
        Movement entity = mapper.toModel(request);
        this.buildAccount(entity, request.getAccountId());
        this.validateAccountStatus(entity.getAccount());
        this.validateInsufficientFounds(entity.getAccount(), request);
        this.buildTransactionType(request, entity);
        movementRepository.save(entity);
        log.info("Movement created successfully - id: {}", entity.getId());
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public MovementResponse update(Long id, MovementRequest request) {
        log.info("Starting update movement");
        Movement entity = movementRepository.findById(id)
                .orElseThrow(() -> new MovementNotFoundException(MOVEMENT_NOT_FOUND + id));
        if (request.getAccountId() != null &&
                (entity.getAccount() == null || !request.getAccountId().equals(entity.getAccount().getId()))) {
            this.buildAccount(entity, request.getAccountId());
        }
        this.validateAccountStatus(entity.getAccount());
        request.setId(id);
        this.validateInsufficientFounds(entity.getAccount(), request);
        mapper.updateModel(request, entity);
        this.buildTransactionType(request, entity);
        movementRepository.save(entity);
        log.info("End update movement");
        return mapper.toResponse(entity);
    }


    @Override
    @Transactional
    public MovementResponse partialUpdate(Long id, MovementPartialUpdateRequest patch) {
        log.info("Starting partialUpdate movement");
        Movement entity = movementRepository.findById(id)
                .orElseThrow(() -> new MovementNotFoundException(MOVEMENT_NOT_FOUND + id));
        this.validateAccountStatus(entity.getAccount());
        entity.setDate(patch.getDate());
        movementRepository.save(entity);
        log.info("End partialUpdate movement");
        return mapper.toResponse(entity);
    }

    @Override
    public void delete(Long id) {
        log.info("Starting delete movement");
        Movement entity = movementRepository.findById(id)
                .orElseThrow(() -> new MovementNotFoundException(MOVEMENT_NOT_FOUND + id));
        movementRepository.delete(entity);
        log.info("End delete movement");
    }

    void buildAccount(Movement movement, Long accountId) {
        Account account = accountService.showById(accountId);
        movement.setAccount(account);
    }

    void validateAccountStatus(Account account) {
        if (!account.isActive()) {
            throw new InactiveAccountException(
                    "Cannot create movement for inactive account: " + account.getAccountNumber());
        }
    }

    void validateTransactionType(MovementRequest request) {
        MovementTypeEnum movementType = request.getMovementType();
        if (movementType == MovementTypeEnum.DEPOSIT || movementType == MovementTypeEnum.INITIAL_DEPOSIT) {
            if (request.getAmount().signum() == -1) {
                String message = String.format("El monto del DEPOSITO no puede ser negativo: %s", request.getAmount());
                throw new BalanceTypeSigNumUnavailableException(message);
            }
        } else if (movementType == MovementTypeEnum.WITHDRAWAL) {
            if (request.getAmount().signum() != -1) {
                String message = String.format("El monto del RETIRO no puede ser positivo: %s", request.getAmount());
                throw new BalanceTypeSigNumUnavailableException(message);
            }
        }
    }


    void buildTransactionType(MovementRequest request, Movement entity) {
        MovementTypeEnum type = request.getMovementType();
        BigDecimal amount = request.getAmount().abs();
        BigDecimal balanceBefore;
        if (entity.getId() == null) {
            balanceBefore = currentBalance(entity.getAccount());
        } else {
            balanceBefore = currentBalance(entity.getAccount(), entity.getId());
        }
        BigDecimal balanceAfter = (type == MovementTypeEnum.DEPOSIT || type == MovementTypeEnum.INITIAL_DEPOSIT)
                ? balanceBefore.add(amount)
                : balanceBefore.subtract(amount);
        entity.setAmount(amount);
        entity.setBalance(balanceAfter);
        entity.setMovementType(type);
    }


    void validateInsufficientFounds(Account account, MovementRequest request) {
        if (request.getMovementType() == MovementTypeEnum.WITHDRAWAL) {
            BigDecimal balanceExcludingUpdated;
            if (request.getId() == null) {
                balanceExcludingUpdated = currentBalance(account);
            } else {
                balanceExcludingUpdated = currentBalance(account, request.getId());
            }
            BigDecimal toWithdrawal = request.getAmount().abs();
            if (balanceExcludingUpdated.subtract(toWithdrawal).signum() == -1) {
                String message = "Saldo no disponible";
                throw new InsufficientFoundsException(message);
            }
        }
    }


    private BigDecimal currentBalance(Account account, Long excludedMovementId) {
        return account.getInitialBalance().add(account.getMovements().stream()
                .filter(m -> !m.getId().equals(excludedMovementId))
                .map(m -> (m.getMovementType() == MovementTypeEnum.DEPOSIT ||
                        m.getMovementType() == MovementTypeEnum.INITIAL_DEPOSIT)
                        ? m.getAmount()
                        : m.getAmount().negate())
                .reduce(BigDecimal.ZERO, BigDecimal::add));
    }


    private BigDecimal currentBalance(Account account) {
        return account.getMovements().stream()
                .map(m -> (m.getMovementType() == MovementTypeEnum.DEPOSIT ||
                        m.getMovementType() == MovementTypeEnum.INITIAL_DEPOSIT)
                        ? m.getAmount()
                        : m.getAmount().negate())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}