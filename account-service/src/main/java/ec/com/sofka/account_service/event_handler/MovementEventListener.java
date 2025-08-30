package ec.com.sofka.account_service.event_handler;

import ec.com.sofka.account_service.dto.movement.request.MovementRequest;
import ec.com.sofka.account_service.event_handler.dto.MovementDto;
import ec.com.sofka.account_service.service.MovementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MovementEventListener implements ApplicationListener<MovementDto> {
    private final MovementService service;

    @EventListener
    public void handleAccountBalance(MovementDto event) {
        log.info("Received movement event with data: {}", event.toString());
        MovementRequest request = MovementRequest.builder()
                .accountId(event.getAccountId())
                .amount(event.getAmount())
                .balance(event.getBalance())
                .movementType(event.getMovementType())
                .date(event.getDate())
                .build();

        service.create(request);
    }

    @Override
    public void onApplicationEvent(MovementDto event) {
        log.info("Received movement onApplicationEvent");
    }

    @Override
    public boolean supportsAsyncExecution() {
        return ApplicationListener.super.supportsAsyncExecution();
    }
}