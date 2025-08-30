package ec.com.sofka.account_service.service;

import ec.com.sofka.account_service.dto.report.StatementReportRow;
import java.time.LocalDate;
import java.util.List;

public interface ReportService {
    List<StatementReportRow> generateStatement(Long clientId, LocalDate from, LocalDate to);
}
