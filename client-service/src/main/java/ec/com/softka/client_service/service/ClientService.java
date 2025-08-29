package ec.com.softka.client_service.service;

import ec.com.softka.client_service.dto.request.ClientPartialUpdate;
import ec.com.softka.client_service.dto.request.ClientRequest;
import ec.com.softka.client_service.dto.response.ClientResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ClientService {
    Page<ClientResponse> index(Pageable pageable);

    ClientResponse show(Integer id);

    ClientResponse create(ClientRequest request);

    ClientResponse update(Integer id, ClientRequest request);

    ClientResponse partialUpdate(Integer id, ClientPartialUpdate request);

    void delete(Integer id);
}
