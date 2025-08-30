package ec.com.sofka.account_service.service.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ec.com.sofka.account_service.client.ClientHttp;
import ec.com.sofka.account_service.client.dto.ClientDto;
import ec.com.sofka.account_service.dto.account.request.AccountPartialUpdateRequest;
import ec.com.sofka.account_service.dto.account.request.AccountRequest;
import ec.com.sofka.account_service.dto.account.response.AccountResponse;
import ec.com.sofka.account_service.event_handler.dto.MovementDto;
import ec.com.sofka.account_service.exception.AccountNotFoundException;
import ec.com.sofka.account_service.exception.ClientNotFoundException;
import ec.com.sofka.account_service.exception.DuplicateAccountException;
import ec.com.sofka.account_service.mapper.AccountMapper;
import ec.com.sofka.account_service.model.Account;
import ec.com.sofka.account_service.model.enums.AccountTypeEnum;
import ec.com.sofka.account_service.model.enums.MovementTypeEnum;
import ec.com.sofka.account_service.repository.AccountRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    AccountRepository accountRepository;
    @Mock
    ClientHttp clientHttp;
    @Mock
    AccountMapper accountMapper;
    @Mock
    ApplicationEventPublisher publisher;

    @InjectMocks
    AccountServiceImpl service;

    @Captor
    ArgumentCaptor<MovementDto> movementCaptor;

    private Account entity(Long id, Long clientId, String number, AccountTypeEnum type, String initial) {
        Account account = new Account();
        account.setId(id);
        account.setClientId(clientId);
        account.setAccountNumber(number);
        account.setAccountType(type);
        account.setInitialBalance(new BigDecimal(initial));
        account.setDeleted(false);
        account.setCreatedAt(LocalDateTime.now());
        return account;
    }

    private AccountRequest req(Long clientId, String number, AccountTypeEnum type, String initial) {
        AccountRequest accountRequest = new AccountRequest();
        accountRequest.setClientId(clientId);
        accountRequest.setAccountNumber(number);
        accountRequest.setAccountType(type);
        accountRequest.setInitialBalance(new BigDecimal(initial));
        accountRequest.setActive(true);
        return accountRequest;
    }

    private AccountResponse resp(Account a) {
        AccountResponse accountResponse = new AccountResponse();
        accountResponse.setId(a.getId());
        accountResponse.setClientId(a.getClientId());
        accountResponse.setAccountNumber(a.getAccountNumber());
        accountResponse.setAccountType(a.getAccountType());
        accountResponse.setInitialBalance(a.getInitialBalance());
        accountResponse.setActive(true);
        accountResponse.setCreatedAt(a.getCreatedAt());
        accountResponse.setUpdatedAt(a.getUpdatedAt());
        return accountResponse;
    }

    @Test
    @DisplayName("index: devuelve página mapeada")
    void index_ok() {
        Account account = entity(1L, 1L, "478758", AccountTypeEnum.SAVINGS, "2000.00");
        Account account1 = entity(2L, 2L, "225487", AccountTypeEnum.CURRENT, "100.00");
        Page<Account> page = new PageImpl<>(List.of(account, account1), PageRequest.of(0, 20), 2);

        when(accountRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(accountMapper.toResponse(account)).thenReturn(resp(account));
        when(accountMapper.toResponse(account1)).thenReturn(resp(account1));

        Page<AccountResponse> out = service.index(PageRequest.of(0, 20));
        assertEquals(2, out.getContent().size());
        assertEquals("478758", out.getContent().get(0).getAccountNumber());
    }

    @Test
    @DisplayName("index: página vacía")
    void index_emptyPage_ok() {
        Page<Account> empty = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        when(accountRepository.findAll(any(Pageable.class))).thenReturn(empty);

        Page<AccountResponse> out = service.index(PageRequest.of(0, 20));
        assertEquals(0, out.getTotalElements());
        assertEquals(0, out.getContent().size());
    }

    @Test
    @DisplayName("show: encuentra y mapea; 404 si no existe")
    void show_cases() {
        Account account = entity(1L, 1L, "478758", AccountTypeEnum.SAVINGS, "2000.00");
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountMapper.toResponse(account)).thenReturn(resp(account));

        AccountResponse ok = service.show(1L);
        assertEquals(1L, ok.getId());

        when(accountRepository.findById(9L)).thenReturn(Optional.empty());
        assertThrows(AccountNotFoundException.class, () -> service.show(9L));
    }

    @Test
    @DisplayName("create: valida cliente, sin duplicados, guarda y publica evento de depósito inicial")
    void create_ok_publishesInitialDeposit() {
        AccountRequest in = req(1L, "585545", AccountTypeEnum.CURRENT, "1000.00");
        Account mapped = entity(null, 1L, "585545", AccountTypeEnum.CURRENT, "1000.00");
        Account saved = entity(5L, 1L, "585545", AccountTypeEnum.CURRENT, "1000.00");
        AccountResponse out = resp(saved);


        when(clientHttp.show(1L)).thenReturn(new ClientDto());
        when(accountRepository.findByClientIdAndAccountTypeAndAccountNumber(1L, AccountTypeEnum.CURRENT, "585545"))
                .thenReturn(List.of());
        when(accountMapper.toModel(in)).thenReturn(mapped);
        when(accountRepository.save(any(Account.class))).thenReturn(saved);
        when(accountMapper.toResponse(saved)).thenReturn(out);

        AccountResponse res = service.create(in);
        assertEquals(5L, res.getId());

        verify(publisher).publishEvent(movementCaptor.capture());
        MovementDto evt = movementCaptor.getValue();

        assertEquals(MovementTypeEnum.INITIAL_DEPOSIT, evt.getMovementType());
        assertEquals(new BigDecimal("1000.00"), evt.getAmount());
        assertEquals(5L, evt.getAccountId());
    }

    @Test
    @DisplayName("create: duplica → DuplicateAccountException; cliente no existe → ClientNotFoundException")
    void create_errors() {
        AccountRequest in = req(1L, "585545", AccountTypeEnum.CURRENT, "1000.00");

        when(clientHttp.show(1L)).thenReturn(new ClientDto());
        when(accountRepository.findByClientIdAndAccountTypeAndAccountNumber(1L, AccountTypeEnum.CURRENT, "585545"))
                .thenReturn(List.of(entity(7L, 1L, "585545", AccountTypeEnum.CURRENT, "10.00")));
        assertThrows(DuplicateAccountException.class, () -> service.create(in));
        verify(accountRepository, never()).save(any());


        AccountRequest in2 = req(9L, "X", AccountTypeEnum.SAVINGS, "0.00");
        doThrow(new ClientNotFoundException("nope")).when(clientHttp).show(9L);
        assertThrows(ClientNotFoundException.class, () -> service.create(in2));
    }

    @Test
    @DisplayName("create: en error (duplicado/cliente no existe) no publica evento")
    void create_errors_doNotPublishEvent() {

        AccountRequest in = req(1L, "585545", AccountTypeEnum.CURRENT, "1000.00");
        when(clientHttp.show(1L)).thenReturn(new ClientDto());
        when(accountRepository.findByClientIdAndAccountTypeAndAccountNumber(1L, AccountTypeEnum.CURRENT, "585545"))
                .thenReturn(List.of(entity(7L, 1L, "585545", AccountTypeEnum.CURRENT, "10.00")));

        assertThrows(DuplicateAccountException.class, () -> service.create(in));
        verify(publisher, never()).publishEvent(any());

        AccountRequest in2 = req(9L, "X", AccountTypeEnum.SAVINGS, "0.00");
        doThrow(new ClientNotFoundException("nope")).when(clientHttp).show(9L);
        assertThrows(ClientNotFoundException.class, () -> service.create(in2));
        verify(publisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("update: valida duplicado y guarda")
    void update_ok() {
        Account existing = entity(1L, 1L, "478758", AccountTypeEnum.SAVINGS, "2000.00");
        AccountRequest in = req(1L, "478758", AccountTypeEnum.SAVINGS, "2500.00");

        when(accountRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(accountRepository.findByClientIdAndAccountTypeAndAccountNumber(1L, AccountTypeEnum.SAVINGS, "478758"))
                .thenReturn(List.of(existing));


        doAnswer(inv -> {
            AccountRequest s = inv.getArgument(0, AccountRequest.class);
            Account t = inv.getArgument(1, Account.class);
            t.setInitialBalance(s.getInitialBalance());
            return null;
        }).when(accountMapper).updateModel(any(AccountRequest.class), any(Account.class));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));
        when(accountMapper.toResponse(any(Account.class))).thenAnswer(inv -> resp(inv.getArgument(0)));

        AccountResponse out = service.update(1L, in);
        assertEquals(new BigDecimal("2500.00"), out.getInitialBalance());
    }

    @Test
    @DisplayName("update: 404 o duplicado con otro registro")
    void update_errors() {
        when(accountRepository.findById(9L)).thenReturn(Optional.empty());
        assertThrows(AccountNotFoundException.class,
                () -> service.update(9L, req(1L, "X", AccountTypeEnum.SAVINGS, "1.00")));

        Account existing = entity(1L, 1L, "A", AccountTypeEnum.SAVINGS, "10.00");
        Account other = entity(2L, 1L, "A", AccountTypeEnum.SAVINGS, "20.00");
        when(accountRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(accountRepository.findByClientIdAndAccountTypeAndAccountNumber(1L, AccountTypeEnum.SAVINGS, "A"))
                .thenReturn(List.of(existing, other));

        assertThrows(DuplicateAccountException.class,
                () -> service.update(1L, req(1L, "A", AccountTypeEnum.SAVINGS, "30.00")));
    }

    @Test
    @DisplayName("partialUpdate: aplica patch, valida y guarda")
    void partialUpdate_ok() {
        Account existing = entity(1L, 1L, "478758", AccountTypeEnum.SAVINGS, "2000.00");
        AccountPartialUpdateRequest patch = new AccountPartialUpdateRequest();
        patch.setAccountNumber("999999");

        when(accountRepository.findById(1L)).thenReturn(Optional.of(existing));
        doAnswer(inv -> {
            Account t = inv.getArgument(0);
            AccountPartialUpdateRequest p = inv.getArgument(1);
            if (p.getAccountNumber() != null) {
                t.setAccountNumber(p.getAccountNumber());
            }
            return null;
        })
                .when(accountMapper).partialUpdate(any(Account.class), any(AccountPartialUpdateRequest.class));

        when(clientHttp.show(1L)).thenReturn(new ClientDto());
        when(accountRepository.findByClientIdAndAccountTypeAndAccountNumber(1L, AccountTypeEnum.SAVINGS, "999999"))
                .thenReturn(List.of(existing));

        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));
        when(accountMapper.toResponse(any(Account.class))).thenAnswer(inv -> resp(inv.getArgument(0)));

        AccountResponse out = service.partialUpdate(1L, patch);
        assertEquals("999999", out.getAccountNumber());
    }

    @Test
    @DisplayName("partialUpdate: 404 si la cuenta no existe")
    void partialUpdate_notFound() {
        when(accountRepository.findById(123L)).thenReturn(Optional.empty());
        AccountPartialUpdateRequest patch = new AccountPartialUpdateRequest();
        patch.setAccountNumber("999999");
        assertThrows(AccountNotFoundException.class, () -> service.partialUpdate(123L, patch));
    }

    @Test
    @DisplayName("partialUpdate: duplicado con otro registro ⇒ DuplicateAccountException")
    void partialUpdate_duplicateWithOther_throws() {
        Account existing = entity(1L, 1L, "A", AccountTypeEnum.SAVINGS, "2000.00");
        Account other = entity(2L, 1L, "A", AccountTypeEnum.SAVINGS, "500.00");
        when(accountRepository.findById(1L)).thenReturn(Optional.of(existing));

        AccountPartialUpdateRequest patch = new AccountPartialUpdateRequest();
        patch.setAccountNumber("A");


        when(accountRepository.findByClientIdAndAccountTypeAndAccountNumber(1L, AccountTypeEnum.SAVINGS, "A"))
                .thenReturn(List.of(existing, other));

        assertThrows(DuplicateAccountException.class, () -> service.partialUpdate(1L, patch));
        verify(accountRepository, never()).save(any());
    }

    @Test
    @DisplayName("delete: soft/hard delete; 404 si no existe")
    void delete_cases() {
        when(accountRepository.findById(9L)).thenReturn(Optional.empty());
        assertThrows(AccountNotFoundException.class, () -> service.delete(9L));

        Account existing = entity(1L, 1L, "478758", AccountTypeEnum.SAVINGS, "2000.00");
        when(accountRepository.findById(1L)).thenReturn(Optional.of(existing));

        assertDoesNotThrow(() -> service.delete(1L));
    }

    @Test
    @DisplayName("delete: soft delete marca deleted=true y guarda")
    void delete_softDelete_marksAndSaves() {
        Account existing = entity(1L, 1L, "478758", AccountTypeEnum.SAVINGS, "2000.00");
        existing.setDeleted(true);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(existing));


        assertDoesNotThrow(() -> service.delete(1L));

        assertTrue(existing.isDeleted());
    }

    @Test
    @DisplayName("showById: ok")
    void showById_ok() {
        Account a = entity(1L, 1L, "X", AccountTypeEnum.SAVINGS, "1.00");
        when(accountRepository.findById(1L)).thenReturn(Optional.of(a));
        Account got = service.showById(1L);
        assertEquals(1L, got.getId());
    }

    @Test
    @DisplayName("showById: 404 not found")
    void showById_404NotFound() {
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> service.showById(1L));
    }
}