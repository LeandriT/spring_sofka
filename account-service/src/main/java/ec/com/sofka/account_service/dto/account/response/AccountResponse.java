package ec.com.sofka.account_service.dto.account.response;

import ec.com.sofka.account_service.model.enums.AccountTypeEnum;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountResponse {
    private Long id;
    private String accountNumber;
    private AccountTypeEnum accountType;
    private BigDecimal initialBalance;
    private Boolean active;
    private Long clientId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
