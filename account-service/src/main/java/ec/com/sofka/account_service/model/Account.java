package ec.com.sofka.account_service.model;

import ec.com.sofka.account_service.model.enums.AccountTypeEnum;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "accounts")
@Entity
@SQLDelete(sql = "UPDATE \"accounts\" SET \"deleted\" = true, \"deleted_at\" = CURRENT_TIMESTAMP WHERE \"id\" = ?")
@SQLRestriction("\"deleted\" = false")
public class Account extends Base {
    private String accountNumber;
    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, length = 20)
    private AccountTypeEnum accountType;
    private BigDecimal initialBalance;
    private boolean isActive;

    @Column(name = "client_id")
    private Long clientId;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Movement> movements = new HashSet<>();
}
