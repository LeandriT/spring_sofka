package ec.com.sofka.account_service.dto.account.request;

import ec.com.sofka.account_service.dto.retentions.OnCreate;
import ec.com.sofka.account_service.model.enums.AccountTypeEnum;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AccountRequest {
    @NotNull(message = "account number cannot be null", groups = {OnCreate.class})
    private String accountNumber;
    @NotNull(message = "account type cannot be null", groups = {OnCreate.class})
    private AccountTypeEnum accountType;
    @NotNull(message = "initial balance cannot be null", groups = {OnCreate.class})
    @Min(value = 1, message = "initial balance must be greater than 0", groups = {OnCreate.class})
    private BigDecimal initialBalance;
    @NotNull(message = "status cannot be null", groups = {OnCreate.class})
    private Boolean active;
    @NotNull(message = "client_id cannot be null", groups = {OnCreate.class})
    private Long clientId;
}