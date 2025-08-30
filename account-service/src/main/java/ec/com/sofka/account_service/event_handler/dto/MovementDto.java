package ec.com.sofka.account_service.event_handler.dto;


import ec.com.sofka.account_service.model.enums.MovementTypeEnum;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;

@Getter
@ToString
public class MovementDto extends ApplicationEvent implements Serializable {

    private final LocalDateTime date;
    private final MovementTypeEnum movementType;
    private final BigDecimal amount;
    private final Long accountId;
    private final BigDecimal balance;


    public MovementDto(Object source, LocalDateTime date, MovementTypeEnum movementType,
                       BigDecimal amount, Long accountId, BigDecimal balance) {
        super(source);
        this.date = date;
        this.movementType = movementType;
        this.amount = amount;
        this.accountId = accountId;
        this.balance = balance;
    }
}