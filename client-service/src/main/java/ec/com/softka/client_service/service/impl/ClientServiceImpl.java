package ec.com.softka.client_service.service.impl;

import ec.com.softka.client_service.dto.request.ClientPartialUpdate;
import ec.com.softka.client_service.dto.request.ClientRequest;
import ec.com.softka.client_service.dto.response.ClientResponse;
import ec.com.softka.client_service.exception.ClientNotFoundException;
import ec.com.softka.client_service.mapper.ClientMapper;
import ec.com.softka.client_service.model.Client;
import ec.com.softka.client_service.repository.ClientRepository;
import ec.com.softka.client_service.service.ClientService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final ClientRepository repository;
    private final ClientMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private static final String CLIENT_NOT_FOUND_MESSAGE = "Client with ID %d does not exist";
    private static final String DNI_ALREADY_EXISTS_MESSAGE = "Client with DNI %s already exists";

    @Override
    @Transactional(readOnly = true)
    public Page<ClientResponse> index(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ClientResponse show(Integer id) {
        String message = String.format(CLIENT_NOT_FOUND_MESSAGE, id);
        Client entity = repository.findById(id)
                .orElseThrow(() -> new ClientNotFoundException(message));
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public ClientResponse create(ClientRequest request) {
        this.validate(request);
        Client entity = mapper.toModel(request);
        String encryptedPassword = passwordEncoder.encode(request.getPassword());
        entity.setPassword(encryptedPassword);
        repository.save(entity);
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public ClientResponse update(Integer id, ClientRequest request) {
        String message = String.format(CLIENT_NOT_FOUND_MESSAGE, id);
        Client entity = repository.findById(id).orElseThrow(() -> new ClientNotFoundException(message));
        mapper.updateModel(request, entity);
        entity.setUpdatedAt(LocalDateTime.now());
        repository.save(entity);
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public ClientResponse partialUpdate(Integer id, ClientPartialUpdate request) {
        String message = String.format(CLIENT_NOT_FOUND_MESSAGE, id);
        Client entity = repository.findById(id).orElseThrow(() -> new ClientNotFoundException(message));
        entity.setActive(request.isActive());
        entity.setUpdatedAt(LocalDateTime.now());
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        String message = String.format(CLIENT_NOT_FOUND_MESSAGE, id);
        Client entity = repository.findById(id).orElseThrow(() -> new ClientNotFoundException(message));
        repository.delete(entity);
    }


    void validateIdentificationExists(ClientRequest request) {
        boolean exists = repository.existsByDni(request.getDni());
        if (exists) {
            String message = String.format(DNI_ALREADY_EXISTS_MESSAGE, request.getDni());
            throw new ClientNotFoundException(message);
        }
    }

    void validate(ClientRequest request) {
        this.validateIdentificationExists(request);
    }
}