package ec.com.sofka.account_service.repository;

import ec.com.sofka.account_service.model.Movement;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovementRepository extends JpaRepository<Movement, Long> {

    Page<Movement> findByAccountIdOrderByDateDesc(Long accountId, Pageable pageable);

    List<Movement> findByAccountIdAndDateBetween(Long accountId, LocalDateTime from, LocalDateTime to);

    List<Movement> findByAccountIdAndDateBefore(Long accountId, LocalDateTime date);

    List<Movement> findByAccountIdAndDateBetweenOrderByDateAsc(Long id, LocalDateTime startOfDay,
                                                               LocalDateTime endOfDay);
}
