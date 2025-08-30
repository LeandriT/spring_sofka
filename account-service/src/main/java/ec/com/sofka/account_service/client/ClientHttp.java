package ec.com.sofka.account_service.client;

import ec.com.sofka.account_service.client.dto.ClientDto;
import ec.com.sofka.account_service.exception.ClientNotFoundException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "client-http", url = "${app.ms.client-service.url}")
public interface ClientHttp {

    @GetMapping("/api/clients/{id}")
    @CircuitBreaker(name = "clientClientCircuit", fallbackMethod = "fallbackDataById")
    @Retry(name = "clientClientRetry")
    ClientDto show(@PathVariable("id") Long id);

    default ClientDto fallbackDataById(Throwable throwable) {
        throw new ClientNotFoundException("Client service is unavailable. Error: " + throwable.getMessage());
    }

}
