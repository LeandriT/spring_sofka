package ec.com.sofka.account_service.service.impl;

import ec.com.sofka.account_service.client.ClientHttp;
import ec.com.sofka.account_service.client.dto.ClientDto;
import ec.com.sofka.account_service.dto.report.StatementReportRow;
import ec.com.sofka.account_service.model.Account;
import ec.com.sofka.account_service.model.Movement;
import ec.com.sofka.account_service.model.enums.MovementTypeEnum;
import ec.com.sofka.account_service.repository.AccountRepository;
import ec.com.sofka.account_service.repository.MovementRepository;
import ec.com.sofka.account_service.service.ReportService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final AccountRepository accountRepository;
    private final MovementRepository movementRepository;
    private final ClientHttp clientHttp;

    @Override
    @Transactional(readOnly = true)
    public List<StatementReportRow> generateStatement(Long clientId, LocalDate from, LocalDate to) {
        List<StatementReportRow> statementRows = new ArrayList<>();
        List<Account> accounts = accountRepository.findByClientId(clientId);
        ClientDto clientDto = clientHttp.show(clientId);
        String clientName = clientDto != null ? clientDto.getName() : "";
        for (Account account : accounts) {
            BigDecimal initialBalanceAccount = account.getInitialBalance();
            LocalDateTime startOfDay = from.atStartOfDay();
            LocalDateTime endOfDay = to.atStartOfDay().plusDays(1).minusNanos(1);

            List<Movement> movementsInPeriod =
                    movementRepository.findByAccountIdAndDateBetweenOrderByDateAsc(account.getId(), startOfDay,
                            endOfDay);
            LocalDateTime reportDate = movementsInPeriod.isEmpty()
                    ? endOfDay              // o puedes usar null si tu DTO lo permite
                    : movementsInPeriod.getLast().getDate();
            BigDecimal initialBalance = BigDecimal.ZERO;
            List<Movement> lastMovements =
                    movementRepository.findByAccountIdAndDateBefore(account.getId(), startOfDay);
            for (Movement priorMovement : lastMovements) {
                initialBalance = initialBalance.add(signed(priorMovement));
            }

            Movement lastMovement = movementsInPeriod.stream()
                    .reduce((first, second) -> second) // se queda con el último
                    .orElse(new Movement());
            BigDecimal lastMovementValue = this.signed(lastMovement);

            BigDecimal totalMovementInPeriod = movementsInPeriod.stream()
                    .map(this::signed)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal availableBalance = initialBalance.add(totalMovementInPeriod);
            StatementReportRow statementRow = StatementReportRow.builder()
                    .date(reportDate)
                    .clientName(clientName)
                    .accountNumber(account.getAccountNumber())
                    .accountType(account.getAccountType().getDisplayName())
                    .initialBalance(initialBalanceAccount)
                    .active(account.isActive())
                    .movement(lastMovementValue)
                    .availableBalance(availableBalance)
                    .build();
            statementRows.add(statementRow);
        }
        return statementRows.reversed();
    }

    private BigDecimal signed(Movement mv) {
        BigDecimal amount = Objects.nonNull(mv.getAmount()) ? mv.getAmount().abs() : BigDecimal.ZERO;
        return (mv.getMovementType() == MovementTypeEnum.DEPOSIT
                || mv.getMovementType() == MovementTypeEnum.INITIAL_DEPOSIT)
                ? amount
                : amount.negate();
    }
}