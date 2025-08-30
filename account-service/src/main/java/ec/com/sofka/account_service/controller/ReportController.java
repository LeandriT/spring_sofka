package ec.com.sofka.account_service.controller;


import ec.com.sofka.account_service.dto.report.StatementReportRow;
import ec.com.sofka.account_service.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @Operation(
            summary = "Reporte de Estado de Cuenta por rango de fechas y cliente",
            description = "Devuelve las cuentas del cliente y el detalle de movimientos en el rango",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Ok",
                            content = @Content(schema = @Schema(implementation = StatementReportRow.class)))
            }
    )
    @GetMapping
    public ResponseEntity<List<StatementReportRow>> statement(
            @Parameter(description = "ID del cliente", example = "1")
            @RequestParam("cliente") @NotNull Long clientId,
            @Parameter(description = "Fecha inicio (yyyy-MM-dd)", example = "2024-08-01")
            @RequestParam("desde") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "Fecha fin (yyyy-MM-dd)", example = "2024-08-31")
            @RequestParam("hasta") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(reportService.generateStatement(clientId, from, to));
    }
}