package ec.com.sofka.account_service.dto.report;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatementReportRow {
    @JsonProperty("Fecha")
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDateTime date;

    @JsonProperty("Cliente")
    private String clientName;

    @JsonProperty("Numero Cuenta")
    private String accountNumber;

    @JsonProperty("Tipo")
    private String accountType;

    @JsonProperty("Saldo Inicial")
    private BigDecimal initialBalance;

    @JsonProperty("Estado")
    private boolean active;

    @JsonProperty("Movimiento")
    private BigDecimal movement;

    @JsonProperty("Saldo Disponible")
    private BigDecimal availableBalance;
}
