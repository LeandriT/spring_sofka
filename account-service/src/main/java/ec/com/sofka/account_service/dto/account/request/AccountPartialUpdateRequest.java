package ec.com.sofka.account_service.dto.account.request;

import ec.com.sofka.account_service.model.enums.AccountTypeEnum;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class AccountPartialUpdateRequest {
    private String accountNumber;
    private AccountTypeEnum accountType;
    @Min(1)
    private BigDecimal initialBalance;
    private Boolean status;
    private Long clientId;
}
