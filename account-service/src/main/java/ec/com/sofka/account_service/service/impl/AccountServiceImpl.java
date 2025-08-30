package ec.com.sofka.account_service.service.impl;

import ec.com.sofka.account_service.client.ClientHttp;
import ec.com.sofka.account_service.dto.account.request.AccountPartialUpdateRequest;
import ec.com.sofka.account_service.dto.account.request.AccountRequest;
import ec.com.sofka.account_service.dto.account.response.AccountResponse;
import ec.com.sofka.account_service.event_handler.dto.MovementDto;
import ec.com.sofka.account_service.exception.AccountNotFoundException;
import ec.com.sofka.account_service.exception.ClientNotFoundException;
import ec.com.sofka.account_service.exception.DuplicateAccountException;
import ec.com.sofka.account_service.mapper.AccountMapper;
import ec.com.sofka.account_service.model.Account;
import ec.com.sofka.account_service.model.enums.MovementTypeEnum;
import ec.com.sofka.account_service.repository.AccountRepository;
import ec.com.sofka.account_service.service.AccountService;
import feign.FeignException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepository;
    private final ClientHttp clientHttp;
    private final AccountMapper accountMapper;
    private final ApplicationEventPublisher applicationEventPublisher;

    private static final String ACCOUNT_NOT_FOUND_MESSAGE = "Account with ID %d does not exist";
    private static final String CLIENT_NOT_FOUND_MESSAGE = "Client with ID %d does not exist";

    @Override
    public Page<AccountResponse> index(Pageable pageable) {
        return accountRepository.findAll(pageable).map(accountMapper::toResponse);
    }

    @Override
    public AccountResponse show(Long id) {
        Account account = accountRepository.findById(id).orElseThrow(() -> {
            log.warn("Account not found with ID: {}", id);
            return new AccountNotFoundException(String.format(ACCOUNT_NOT_FOUND_MESSAGE, id));
        });
        return accountMapper.toResponse(account);
    }

    @Override
    public AccountResponse create(AccountRequest request) {
        log.info("Creating new account for client ID: {}", request.getClientId());
        validateClientExists(request.getClientId());
        List<Account> duplicates = accountRepository.findByClientIdAndAccountTypeAndAccountNumber(request.getClientId(),
                request.getAccountType(), request.getAccountNumber());
        this.validateDuplicateOnCreate(duplicates);
        Account entity = accountMapper.toModel(request);
        entity.setInitialBalance(request.getInitialBalance());
        Account savedAccount = accountRepository.save(entity);
        log.info("Account created successfully with ID: {}", savedAccount.getId());

        if (request.getInitialBalance().compareTo(BigDecimal.ZERO) > 0) {
            MovementDto movementDto = new MovementDto(this, LocalDateTime.now(), MovementTypeEnum.INITIAL_DEPOSIT,
                    request.getInitialBalance(), savedAccount.getId(), request.getInitialBalance());
            applicationEventPublisher.publishEvent(movementDto);
        }

        return accountMapper.toResponse(savedAccount);
    }

    @Override
    public AccountResponse update(Long id, AccountRequest request) {
        Account entity = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(String.format(ACCOUNT_NOT_FOUND_MESSAGE, id)));


        if (request.getClientId() != null && !Objects.equals(request.getClientId(), entity.getClientId())) {
            validateClientExists(request.getClientId());
        }

        List<Account> duplicates = accountRepository.findByClientIdAndAccountTypeAndAccountNumber(
                request.getClientId() != null ? request.getClientId() : entity.getClientId(),
                request.getAccountType() != null ? request.getAccountType() : entity.getAccountType(),
                request.getAccountNumber() != null ? request.getAccountNumber() : entity.getAccountNumber());
        this.validateDuplicateOnUpdate(duplicates, id);

        accountMapper.updateModel(request, entity);
        entity.setUpdatedAt(LocalDateTime.now());

        Account saved = accountRepository.save(entity);
        return accountMapper.toResponse(saved);
    }

    @Override
    public AccountResponse partialUpdate(Long id, AccountPartialUpdateRequest patch) {
        Account entity = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(String.format(ACCOUNT_NOT_FOUND_MESSAGE, id)));


        accountMapper.partialUpdate(entity, patch);


        if (entity.getClientId() == null) {
            throw new IllegalArgumentException("clientId must not be null");
        }
        validateClientExists(entity.getClientId());


        if (entity.getAccountType() != null && entity.getAccountNumber() != null) {
            List<Account> duplicates =
                    accountRepository.findByClientIdAndAccountTypeAndAccountNumber(entity.getClientId(),
                            entity.getAccountType(), entity.getAccountNumber());
            this.validateDuplicateOnUpdate(duplicates, id);
        }

        entity.setUpdatedAt(LocalDateTime.now());
        Account saved = accountRepository.save(entity);
        return accountMapper.toResponse(saved);
    }

    @Override
    public void delete(Long id) {
        Account entity = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(String.format(ACCOUNT_NOT_FOUND_MESSAGE, id)));
        accountRepository.delete(entity);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public Account showById(Long accountId) {
        return accountRepository.findById(accountId).orElseThrow(() -> {
            log.warn("Account not found with ID: {}", accountId);
            return new AccountNotFoundException(String.format(ACCOUNT_NOT_FOUND_MESSAGE, accountId));
        });
    }


    private void validateClientExists(Long clientId) {
        try {
            clientHttp.show(clientId);
            log.debug("Client validation successful for ID: {}", clientId);
        } catch (ClientNotFoundException | FeignException.NotFound e) {
            log.error("Client not found with ID: {}", clientId);
            throw new ClientNotFoundException(String.format(CLIENT_NOT_FOUND_MESSAGE, clientId));
        } catch (FeignException e) {
            log.error("Error occurred while fetching client with ID {}: {}", clientId, e.getMessage());
            throw new RuntimeException("Service unavailable: Unable to validate client", e);
        }
    }

    private void validateDuplicateOnCreate(List<Account> existingAccounts) {
        if (!existingAccounts.isEmpty()) {
            throw new DuplicateAccountException("An account with this number and type already exists for the client.");
        }
    }

    private void validateDuplicateOnUpdate(List<Account> existingAccounts, Long currentAccountId) {
        boolean duplicateExists = existingAccounts.stream().anyMatch(a -> !a.getId().equals(currentAccountId));
        if (duplicateExists) {
            throw new DuplicateAccountException(
                    "Another account with the same number and type already exists for this client.");
        }
    }
}
