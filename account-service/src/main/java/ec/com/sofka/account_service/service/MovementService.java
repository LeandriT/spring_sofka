package ec.com.sofka.account_service.service;

import ec.com.sofka.account_service.dto.movement.request.MovementPartialUpdateRequest;
import ec.com.sofka.account_service.dto.movement.request.MovementRequest;
import ec.com.sofka.account_service.dto.movement.response.MovementResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MovementService {


    Page<MovementResponse> index(Pageable pageable);

    Page<MovementResponse> byAccount(Long accountId, Pageable pageable);

    MovementResponse show(Long id);

    MovementResponse create(MovementRequest request);

    MovementResponse update(Long id, MovementRequest request);

    MovementResponse partialUpdate(Long id, MovementPartialUpdateRequest patch);

    void delete(Long id);
}
