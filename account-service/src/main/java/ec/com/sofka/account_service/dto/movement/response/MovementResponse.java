package ec.com.sofka.account_service.dto.movement.response;

import ec.com.sofka.account_service.dto.account.response.AccountResponse;
import ec.com.sofka.account_service.model.enums.MovementTypeEnum;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MovementResponse {
    private Long id;
    private LocalDateTime date;
    private MovementTypeEnum movementType;
    private BigDecimal amount;
    private BigDecimal balance;
    private AccountResponse account;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
