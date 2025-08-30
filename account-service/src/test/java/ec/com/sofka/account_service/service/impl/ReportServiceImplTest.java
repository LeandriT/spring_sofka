package ec.com.sofka.account_service.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import ec.com.sofka.account_service.client.ClientHttp;
import ec.com.sofka.account_service.client.dto.ClientDto;
import ec.com.sofka.account_service.dto.report.StatementReportRow;
import ec.com.sofka.account_service.model.Account;
import ec.com.sofka.account_service.model.Movement;
import ec.com.sofka.account_service.model.enums.AccountTypeEnum;
import ec.com.sofka.account_service.model.enums.MovementTypeEnum;
import ec.com.sofka.account_service.repository.AccountRepository;
import ec.com.sofka.account_service.repository.MovementRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    @Mock
    AccountRepository accountRepository;

    @Mock
    MovementRepository movementRepository;

    @Mock
    ClientHttp clientHttp;

    @InjectMocks
    ReportServiceImpl service;


    private Account acc(Long id, Long clientId, String number, AccountTypeEnum type, String initial) {
        Account a = new Account();
        a.setId(id);
        a.setClientId(clientId);
        a.setAccountNumber(number);
        a.setAccountType(type);
        a.setInitialBalance(new BigDecimal(initial));
        a.setActive(true);
        return a;
    }

    private Movement mv(Long id, Account a, MovementTypeEnum type, String amount, LocalDateTime date) {
        Movement movement = new Movement();
        movement.setId(id);
        movement.setAccount(a);
        movement.setMovementType(type);
        movement.setAmount(new BigDecimal(amount));
        movement.setDate(date);
        return movement;
    }

    private static LocalDateTime at(int y, int M, int d, int h, int m) {
        return LocalDateTime.of(y, M, d, h, m);
    }


    @Test
    @DisplayName("generateStatement: una cuenta sin movimientos en periodo → total=0, available=initial, " +
            "fecha=endOfDay, clientName OK")
    void statement_singleAccount_noMovements() {
        Long clientId = 1L;
        LocalDate from = LocalDate.of(2024, 8, 1);
        LocalDate to = LocalDate.of(2024, 8, 31);
        LocalDateTime endOfDay = to.atStartOfDay().plusDays(1).minusNanos(1);

        Account a1 = acc(10L, clientId, "ACC-001", AccountTypeEnum.SAVINGS, "1000.00");

        when(accountRepository.findByClientId(clientId)).thenReturn(List.of(a1));


        when(movementRepository.findByAccountIdAndDateBefore(eq(10L), any(LocalDateTime.class)))
                .thenReturn(List.of());
        when(movementRepository.findByAccountIdAndDateBetweenOrderByDateAsc(eq(10L), any(LocalDateTime.class),
                any(LocalDateTime.class)))
                .thenReturn(List.of());

        ClientDto dto = new ClientDto();
        dto.setName("Cliente Demo");
        when(clientHttp.show(clientId)).thenReturn(dto);

        List<StatementReportRow> out = service.generateStatement(clientId, from, to);
        assertEquals(1, out.size());

        StatementReportRow row = out.get(0);
        assertNotNull(row);
        assertEquals("Cliente Demo", row.getClientName());
        assertEquals("ACC-001", row.getAccountNumber());

        assertEquals(new BigDecimal("0"), row.getMovement());

        assertEquals(new BigDecimal("0"), row.getAvailableBalance());

        assertEquals(endOfDay, row.getDate());
    }

    @Test
    @DisplayName("generateStatement: prior (+200, -50) y periodo (+100, -20) → initial=1150, total=+80, " +
            "available=1230, fecha=último movimiento, movement=valor del último")
    void statement_withPrior_andInRange() {
        Long clientId = 2L;
        LocalDate from = LocalDate.of(2024, 8, 1);
        LocalDate to = LocalDate.of(2024, 8, 31);

        Account a1 = acc(11L, clientId, "ACC-XYZ", AccountTypeEnum.CURRENT, "1000.00");

        when(accountRepository.findByClientId(clientId)).thenReturn(List.of(a1));


        List<Movement> prior = List.of(
                mv(1L, a1, MovementTypeEnum.DEPOSIT, "200.00", at(2024, 7, 15, 9, 0)),
                mv(2L, a1, MovementTypeEnum.WITHDRAWAL, "50.00", at(2024, 7, 20, 10, 0))
        );
        when(movementRepository.findByAccountIdAndDateBefore(eq(11L), any(LocalDateTime.class)))
                .thenReturn(prior);


        Movement in1 = mv(3L, a1, MovementTypeEnum.DEPOSIT, "100.00", at(2024, 8, 10, 12, 0));
        Movement in2 = mv(4L, a1, MovementTypeEnum.WITHDRAWAL, "20.00", at(2024, 8, 25, 18, 30));
        when(movementRepository.findByAccountIdAndDateBetweenOrderByDateAsc(eq(11L), any(LocalDateTime.class),
                any(LocalDateTime.class)))
                .thenReturn(List.of(in1, in2));

        ClientDto dto = new ClientDto();
        dto.setName("Marianela Montalvo");
        when(clientHttp.show(clientId)).thenReturn(dto);

        List<StatementReportRow> out = service.generateStatement(clientId, from, to);
        assertEquals(1, out.size());

        StatementReportRow row = out.get(0);

        assertEquals(new BigDecimal("1000.00"), row.getInitialBalance());
        assertEquals(new BigDecimal("-20.00"), row.getMovement());
        assertEquals(new BigDecimal("230.00"), row.getAvailableBalance());
        assertEquals(in2.getDate(), row.getDate());
        assertEquals("Marianela Montalvo", row.getClientName());
        assertEquals(a1.getAccountType().getDisplayName(), row.getAccountType());
    }

    @Test
    @DisplayName("generateStatement: múltiples cuentas → lista final se revierte (última cuenta primero)")
    void statement_multipleAccounts_reversedOrder() {
        Long clientId = 3L;
        LocalDate from = LocalDate.of(2024, 8, 1);
        LocalDate to = LocalDate.of(2024, 8, 31);

        Account a1 = acc(21L, clientId, "ACC-A", AccountTypeEnum.SAVINGS, "500.00");
        Account a2 = acc(22L, clientId, "ACC-B", AccountTypeEnum.CURRENT, "800.00");

        when(accountRepository.findByClientId(clientId)).thenReturn(List.of(a1, a2));


        when(movementRepository.findByAccountIdAndDateBefore(eq(21L), any(LocalDateTime.class)))
                .thenReturn(List.of());
        when(movementRepository.findByAccountIdAndDateBefore(eq(22L), any(LocalDateTime.class)))
                .thenReturn(List.of());
        when(movementRepository.findByAccountIdAndDateBetweenOrderByDateAsc(eq(21L), any(LocalDateTime.class),
                any(LocalDateTime.class)))
                .thenReturn(List.of());
        when(movementRepository.findByAccountIdAndDateBetweenOrderByDateAsc(eq(22L), any(LocalDateTime.class),
                any(LocalDateTime.class)))
                .thenReturn(List.of());

        ClientDto dto = new ClientDto();
        dto.setName("Cliente X");
        when(clientHttp.show(clientId)).thenReturn(dto);

        List<StatementReportRow> out = service.generateStatement(clientId, from, to);
        assertEquals(2, out.size());

        assertEquals("ACC-B", out.get(0).getAccountNumber());
        assertEquals("ACC-A", out.get(1).getAccountNumber());
    }

    @Test
    @DisplayName("generateStatement: clientHttp.show retorna null → clientName vacío")
    void statement_clientNameNull_becomesEmpty() {
        Long clientId = 4L;
        LocalDate from = LocalDate.of(2024, 8, 1);
        LocalDate to = LocalDate.of(2024, 8, 31);

        Account a1 = acc(31L, clientId, "ACC-NULL", AccountTypeEnum.SAVINGS, "100.00");
        when(accountRepository.findByClientId(clientId)).thenReturn(List.of(a1));

        when(clientHttp.show(clientId)).thenReturn(null);

        when(movementRepository.findByAccountIdAndDateBefore(eq(31L), any(LocalDateTime.class)))
                .thenReturn(List.of());
        when(movementRepository.findByAccountIdAndDateBetweenOrderByDateAsc(eq(31L), any(LocalDateTime.class),
                any(LocalDateTime.class)))
                .thenReturn(List.of());

        List<StatementReportRow> out = service.generateStatement(clientId, from, to);
        assertEquals(1, out.size());
        assertEquals("", out.get(0).getClientName());
    }
}