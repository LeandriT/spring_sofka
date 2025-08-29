package ec.com.softka.client_service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "clients")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SQLDelete(sql = "UPDATE \"clients\" SET \"deleted\" = true, \"deleted_at\" = CURRENT_TIMESTAMP WHERE \"id\" = ?")
@SQLRestriction("\"deleted\" = false")
public class Client extends Person {
    @Column(nullable = false)
    private String password;
    private boolean isActive;
}
