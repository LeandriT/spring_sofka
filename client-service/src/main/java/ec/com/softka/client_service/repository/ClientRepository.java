package ec.com.softka.client_service.repository;

import ec.com.softka.client_service.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRepository extends JpaRepository<Client, Integer> {
    boolean existsByDni(String dni);
}
