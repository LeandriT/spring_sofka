package ec.com.sofka.account_service.model;

import ec.com.sofka.account_service.model.enums.MovementTypeEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "movements")
@Entity
@SQLDelete(sql = "UPDATE \"movements\" SET \"deleted\" = true, \"deleted_at\" = CURRENT_TIMESTAMP WHERE \"id\" = ?")
@SQLRestriction("\"deleted\" = false")
public class Movement extends Base {

    private LocalDateTime date;
    @Column(name = "movement_type")
    @Enumerated(EnumType.STRING)
    private MovementTypeEnum movementType;
    private BigDecimal amount;
    private BigDecimal balance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

}
