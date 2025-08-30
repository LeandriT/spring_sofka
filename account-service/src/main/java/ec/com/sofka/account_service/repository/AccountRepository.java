package ec.com.sofka.account_service.repository;

import ec.com.sofka.account_service.model.Account;
import ec.com.sofka.account_service.model.enums.AccountTypeEnum;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByClientIdAndAccountTypeAndAccountNumber(Long clientId, AccountTypeEnum type, String number);

    List<Account> findByClientId(Long clientId);
}
