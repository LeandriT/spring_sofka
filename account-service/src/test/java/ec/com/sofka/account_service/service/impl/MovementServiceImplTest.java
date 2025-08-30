package ec.com.sofka.account_service.service.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ec.com.sofka.account_service.dto.account.response.AccountResponse;
import ec.com.sofka.account_service.dto.movement.request.MovementPartialUpdateRequest;
import ec.com.sofka.account_service.dto.movement.request.MovementRequest;
import ec.com.sofka.account_service.dto.movement.response.MovementResponse;
import ec.com.sofka.account_service.exception.BalanceTypeSigNumUnavailableException;
import ec.com.sofka.account_service.exception.InsufficientFoundsException;
import ec.com.sofka.account_service.exception.MovementNotFoundException;
import ec.com.sofka.account_service.mapper.MovementMapper;
import ec.com.sofka.account_service.model.Account;
import ec.com.sofka.account_service.model.Movement;
import ec.com.sofka.account_service.model.enums.MovementTypeEnum;
import ec.com.sofka.account_service.repository.AccountRepository;
import ec.com.sofka.account_service.repository.MovementRepository;
import ec.com.sofka.account_service.service.AccountService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class MovementServiceImplTest {

    @Mock
    MovementRepository movementRepository;
    @Mock
    AccountRepository accountRepository;
    @Mock
    MovementMapper mapper;
    @Mock
    AccountService accountService;

    @InjectMocks
    MovementServiceImpl service;


    private Account acc(Long id, String init, Set<Movement> movementSet) {
        Account account = new Account();
        account.setId(id);
        account.setInitialBalance(new BigDecimal(init));
        account.setMovements(movementSet);
        account.setActive(true);
        return account;
    }

    private Movement buildMovement(Long id, Long accId, MovementTypeEnum t, String amount, String balance) {
        Movement movement = new Movement();
        Account account = new Account();
        account.setId(accId);
        movement.setId(id);
        movement.setAccount(account);
        movement.setMovementType(t);
        movement.setAmount(new BigDecimal(amount));
        movement.setBalance(new BigDecimal(balance));
        movement.setDate(LocalDateTime.now());
        movement.setCreatedAt(LocalDateTime.now());
        return movement;
    }

    private MovementResponse buildMovementResponse(Movement movement) {
        MovementResponse movementResponse = new MovementResponse();
        AccountResponse accountResponse = new AccountResponse();
        accountResponse.setId(movement.getAccount().getId());
        movementResponse.setId(movement.getId());
        movementResponse.setAccount(accountResponse);
        movementResponse.setMovementType(movement.getMovementType());
        movementResponse.setAmount(movement.getAmount());
        movementResponse.setBalance(movement.getBalance());
        movementResponse.setDate(movement.getDate());
        movementResponse.setCreatedAt(movement.getCreatedAt());
        movementResponse.setUpdatedAt(movement.getUpdatedAt());
        return movementResponse;
    }


    @Test
    @DisplayName("index/byAccount/show: mapeos correctos y 404 si no existe")
    void queries_ok_and_notFound() {
        Movement movement = buildMovement(1L, 1L, MovementTypeEnum.DEPOSIT, "100.00", "1100.00");
        Movement movement1 = buildMovement(2L, 1L, MovementTypeEnum.WITHDRAWAL, "50.00", "1050.00");

        Page<Movement> page = new PageImpl<>(List.of(movement, movement1), PageRequest.of(0, 20), 2);
        when(movementRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(mapper.toResponse(movement)).thenReturn(buildMovementResponse(movement));
        when(mapper.toResponse(movement1)).thenReturn(buildMovementResponse(movement1));

        Page<MovementResponse> idx = service.index(PageRequest.of(0, 20));
        assertEquals(2, idx.getContent().size());

        Page<Movement> pageAcc = new PageImpl<>(List.of(movement), PageRequest.of(0, 10), 1);
        when(movementRepository.findByAccountIdOrderByDateDesc(eq(1L), any(Pageable.class))).thenReturn(pageAcc);
        when(mapper.toResponse(movement)).thenReturn(buildMovementResponse(movement));

        Page<MovementResponse> byAcc = service.byAccount(1L, PageRequest.of(0, 10));
        assertEquals(1, byAcc.getContent().size());
        assertEquals(1L, byAcc.getContent().getFirst().getAccount().getId());

        when(movementRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(MovementNotFoundException.class, () -> service.show(99L));
    }


    @Test
    @DisplayName("create: initial_deposit suma al saldo")
    void create_initial_deposit_ok() {
        Account acc = acc(1L, "1000.00", Set.of());
        when(accountService.showById(1L)).thenReturn(acc);
        when(mapper.toModel(any(MovementRequest.class))).thenAnswer(inv -> new Movement());
        when(movementRepository.save(any(Movement.class))).thenAnswer(inv -> {
            Movement m = inv.getArgument(0);
            m.setId(77L);
            return m;
        });
        when(mapper.toResponse(any(Movement.class))).thenAnswer(inv -> buildMovementResponse(inv.getArgument(0)));

        MovementRequest req = new MovementRequest();
        req.setAccountId(1L);
        req.setMovementType(MovementTypeEnum.INITIAL_DEPOSIT);
        req.setAmount(new BigDecimal("300.00"));

        MovementResponse out = service.create(req);
        assertEquals(new BigDecimal("300.00"), out.getBalance());
    }

    @Test
    @DisplayName("create: depósito suma saldo; retiro valida fondos insuficientes")
    void create_cases() {

        Account account = acc(1L, "1000.00", Set.of());
        when(accountService.showById(1L)).thenReturn(account);

        MovementRequest movementRequest = new MovementRequest();
        movementRequest.setAccountId(1L);
        movementRequest.setMovementType(MovementTypeEnum.DEPOSIT);
        movementRequest.setAmount(new BigDecimal("150.00"));
        movementRequest.setDate(LocalDateTime.now());

        when(mapper.toModel(any(MovementRequest.class))).thenAnswer(inv -> {
            Movement mapped = new Movement();
            mapped.setDate(LocalDateTime.now());
            return mapped;
        });

        when(movementRepository.save(any(Movement.class))).thenAnswer(inv -> {
            Movement invArgument = inv.getArgument(0);
            invArgument.setId(10L);
            return invArgument;
        });
        when(mapper.toResponse(any(Movement.class))).thenAnswer(inv -> buildMovementResponse(inv.getArgument(0)));

        MovementResponse r1 = service.create(movementRequest);
        assertEquals(10L, r1.getId());
        assertEquals(new BigDecimal("150.00"), r1.getBalance());


        Movement last = buildMovement(10L, 1L, MovementTypeEnum.DEPOSIT, "150.00", "1150.00");
        account.setMovements(Set.of(last));
        when(accountService.showById(1L)).thenReturn(account);

        MovementRequest request = new MovementRequest();
        request.setAccountId(1L);
        request.setMovementType(MovementTypeEnum.WITHDRAWAL);
        request.setAmount(new BigDecimal("-2000.00"));

        assertThrows(InsufficientFoundsException.class, () -> service.create(request));
        verify(movementRepository, never())
                .save(argThat(m -> m.getMovementType() == MovementTypeEnum.WITHDRAWAL));
    }


    @Test
    @DisplayName("create: depósito negativo lanza BalanceTypeSigNumUnavailableException")
    void create_deposit_negative_amount_throws() {
        MovementRequest req = new MovementRequest();
        req.setAccountId(1L);
        req.setMovementType(MovementTypeEnum.DEPOSIT);
        req.setAmount(new BigDecimal("-1.00"));

        assertThrows(BalanceTypeSigNumUnavailableException.class, () -> service.create(req));
    }

    @Test
    @DisplayName("create: retiro positivo lanza BalanceTypeSigNumUnavailableException")
    void create_withdrawal_negative_amount_throws() {
        MovementRequest req = new MovementRequest();
        req.setAccountId(1L);
        req.setMovementType(MovementTypeEnum.WITHDRAWAL);
        req.setAmount(new BigDecimal("5.00"));

        assertThrows(BalanceTypeSigNumUnavailableException.class, () -> service.create(req));
    }


    @Test
    @DisplayName("update: recalcula y valida (reemplaza +100 por -200 => 800)")
    void update_recalculate_and_validate() {

        Account account = acc(1L, "1000.00", null);
        Movement existing = buildMovement(40L, 1L, MovementTypeEnum.DEPOSIT, "100.00", "1100.00");
        existing.setAccount(account);
        account.setMovements(Set.of(existing));

        when(movementRepository.findById(40L)).thenReturn(Optional.of(existing));

        MovementRequest movementRequest = new MovementRequest();
        movementRequest.setAccountId(1L);
        movementRequest.setMovementType(MovementTypeEnum.WITHDRAWAL);
        movementRequest.setAmount(new BigDecimal("200.00"));


        doAnswer(inv -> {
            MovementRequest src = inv.getArgument(0, MovementRequest.class);
            Movement target = inv.getArgument(1, Movement.class);
            target.setMovementType(src.getMovementType());
            target.setAmount(src.getAmount());
            return null;
        }).when(mapper).updateModel(any(MovementRequest.class), any(Movement.class));

        when(movementRepository.save(any(Movement.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(Movement.class))).thenAnswer(inv -> buildMovementResponse(inv.getArgument(0)));


        MovementResponse out = service.update(40L, movementRequest);

        assertEquals(MovementTypeEnum.WITHDRAWAL, out.getMovementType());
        assertEquals(new BigDecimal("200.00"), out.getAmount());
        assertEquals(new BigDecimal("800.00"), out.getBalance());

        verify(movementRepository).findById(40L);
        verify(mapper).updateModel(any(MovementRequest.class), any(Movement.class));
        verify(movementRepository).save(any(Movement.class));
    }

    @Test
    @DisplayName("update: cambia de cuenta y recalcula sobre la nueva cuenta")
    void update_switch_account_and_recalc() {

        Account accA = acc(1L, "1000.00", null);
        Movement existing = buildMovement(40L, 1L, MovementTypeEnum.DEPOSIT, "100.00", "1100.00");
        existing.setAccount(accA);
        accA.setMovements(Set.of(existing));

        Movement prevB = buildMovement(10L, 2L, MovementTypeEnum.DEPOSIT, "200.00", "700.00");
        Account accB = acc(2L, "500.00", Set.of(prevB));

        when(movementRepository.findById(40L)).thenReturn(Optional.of(existing));
        when(accountService.showById(2L)).thenReturn(accB);

        MovementRequest req = new MovementRequest();
        req.setAccountId(2L);
        req.setMovementType(MovementTypeEnum.WITHDRAWAL);
        req.setAmount(new BigDecimal("100.00"));

        doAnswer(inv -> {
            MovementRequest src = inv.getArgument(0, MovementRequest.class);
            Movement target = inv.getArgument(1, Movement.class);
            target.setMovementType(src.getMovementType());
            target.setAmount(src.getAmount());
            return null;
        }).when(mapper).updateModel(any(MovementRequest.class), any(Movement.class));

        when(movementRepository.save(any(Movement.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(Movement.class))).thenAnswer(inv -> buildMovementResponse(inv.getArgument(0)));

        MovementResponse out = service.update(40L, req);


        assertEquals(2L, out.getAccount().getId());
        assertEquals(new BigDecimal("600.00"), out.getBalance());
        verify(accountService).showById(2L);
    }

    @Test
    @DisplayName("update: retiro excede el saldo base excluyendo el propio movimiento => InsufficientFoundsException")
    void update_withdrawal_exceeds_base_throws() {
        Account acc = acc(1L, "1000.00", null);
        Movement existing = buildMovement(40L, 1L, MovementTypeEnum.DEPOSIT, "100.00", "1100.00");
        existing.setAccount(acc);
        acc.setMovements(Set.of(existing));
        when(movementRepository.findById(40L)).thenReturn(Optional.of(existing));

        MovementRequest req = new MovementRequest();
        req.setAccountId(1L);
        req.setMovementType(MovementTypeEnum.WITHDRAWAL);
        req.setAmount(new BigDecimal("1200.00"));

        assertThrows(InsufficientFoundsException.class, () -> service.update(40L, req));
    }

    @Test
    @DisplayName("update: retiro dentro del saldo base no lanza excepción")
    void update_withdrawal_within_base_ok() {
        Account acc = acc(1L, "1000.00", null);
        Movement existing = buildMovement(40L, 1L, MovementTypeEnum.DEPOSIT, "100.00", "1100.00");
        existing.setAccount(acc);
        acc.setMovements(Set.of(existing));
        when(movementRepository.findById(40L)).thenReturn(Optional.of(existing));

        MovementRequest req = new MovementRequest();
        req.setAccountId(1L);
        req.setMovementType(MovementTypeEnum.WITHDRAWAL);
        req.setAmount(new BigDecimal("300.00"));

        doAnswer(inv -> {
            MovementRequest src = inv.getArgument(0, MovementRequest.class);
            Movement target = inv.getArgument(1, Movement.class);
            target.setMovementType(src.getMovementType());
            target.setAmount(src.getAmount());
            return null;
        }).when(mapper).updateModel(any(MovementRequest.class), any(Movement.class));

        when(movementRepository.save(any(Movement.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(Movement.class))).thenAnswer(inv -> buildMovementResponse(inv.getArgument(0)));

        MovementResponse out = service.update(40L, req);
        assertEquals(new BigDecimal("700.00"), out.getBalance());
    }

    @Test
    @DisplayName("update: recalcula excluyendo el propio movimiento entre varios movimientos")
    void update_recalc_excluding_self_among_many() {
        Account acc = acc(1L, "1000.00", null);
        Movement m1 = buildMovement(10L, 1L, MovementTypeEnum.DEPOSIT, "200.00", "1200.00");
        Movement m2 = buildMovement(20L, 1L, MovementTypeEnum.WITHDRAWAL, "50.00", "1150.00");
        Movement self = buildMovement(30L, 1L, MovementTypeEnum.DEPOSIT, "100.00", "1250.00");
        m1.setAccount(acc);
        m2.setAccount(acc);
        self.setAccount(acc);
        acc.setMovements(Set.of(m1, m2, self));

        when(movementRepository.findById(30L)).thenReturn(Optional.of(self));

        MovementRequest req = new MovementRequest();
        req.setAccountId(1L);
        req.setMovementType(MovementTypeEnum.WITHDRAWAL);
        req.setAmount(new BigDecimal("150.00"));

        doAnswer(inv -> {
            MovementRequest src = inv.getArgument(0, MovementRequest.class);
            Movement target = inv.getArgument(1, Movement.class);
            target.setMovementType(src.getMovementType());
            target.setAmount(src.getAmount());
            return null;
        }).when(mapper).updateModel(any(MovementRequest.class), any(Movement.class));

        when(movementRepository.save(any(Movement.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(Movement.class))).thenAnswer(inv -> buildMovementResponse(inv.getArgument(0)));

        MovementResponse out = service.update(30L, req);
        assertEquals(new BigDecimal("1000.00"), out.getBalance());
    }


    @Test
    @DisplayName("partialUpdate: actualiza solo la fecha del movimiento")
    void partialUpdate_ok() {

        Movement existingMovement = buildMovement(50L, 2L, MovementTypeEnum.DEPOSIT, "100.00", "600.00");
        Account account = acc(2L, "500.00", Set.of(existingMovement));
        existingMovement.setAccount(account);


        when(movementRepository.findById(50L)).thenReturn(Optional.of(existingMovement));
        when(movementRepository.save(any(Movement.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.toResponse(any(Movement.class))).thenAnswer(
                invocation -> buildMovementResponse(invocation.getArgument(0)));


        MovementPartialUpdateRequest patch = new MovementPartialUpdateRequest();
        LocalDateTime newDate = LocalDateTime.now();
        patch.setDate(newDate);


        MovementResponse out = service.partialUpdate(50L, patch);


        assertNotNull(out);
        assertEquals(newDate, out.getDate());


        verify(movementRepository).save(existingMovement);
    }

    @Test
    @DisplayName("partialUpdate: 404 si no existe el movimiento")
    void partialUpdate_not_found() {
        when(movementRepository.findById(123L)).thenReturn(Optional.empty());
        MovementPartialUpdateRequest patch = new MovementPartialUpdateRequest();
        patch.setDate(LocalDateTime.now());
        assertThrows(MovementNotFoundException.class, () -> service.partialUpdate(123L, patch));
    }


    @Test
    @DisplayName("delete: ok y 404 si no existe")
    void delete_cases() {
        Movement existing = buildMovement(60L, 1L, MovementTypeEnum.DEPOSIT, "10.00", "1010.00");

        when(movementRepository.findById(60L)).thenReturn(Optional.of(existing));
        doNothing().when(movementRepository).delete(existing);

        assertDoesNotThrow(() -> service.delete(60L));
        verify(movementRepository, atLeastOnce()).delete(existing);

        when(movementRepository.findById(61L)).thenReturn(Optional.empty());
        assertThrows(MovementNotFoundException.class, () -> service.delete(61L));
    }
}