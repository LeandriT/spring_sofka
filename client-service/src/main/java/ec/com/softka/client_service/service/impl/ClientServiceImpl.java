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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
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
        log.info("Executed index client");
        return repository.findAll(pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ClientResponse show(Integer id) {
        log.info("Starting show client by id, {}", id);
        String message = String.format(CLIENT_NOT_FOUND_MESSAGE, id);
        Client entity = repository.findById(id)
                .orElseThrow(() -> new ClientNotFoundException(message));
        log.info("End show client by id");
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public ClientResponse create(ClientRequest request) {
        log.info("Starting create client");
        this.validate(request);
        Client entity = mapper.toModel(request);
        String encryptedPassword = passwordEncoder.encode(request.getPassword());
        entity.setPassword(encryptedPassword);
        repository.save(entity);
        log.info("End create client");
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public ClientResponse update(Integer id, ClientRequest request) {
        log.info("Starting update client");
        String message = String.format(CLIENT_NOT_FOUND_MESSAGE, id);
        Client entity = repository.findById(id).orElseThrow(() -> new ClientNotFoundException(message));
        mapper.updateModel(request, entity);
        entity.setUpdatedAt(LocalDateTime.now());
        repository.save(entity);
        log.info("End update client");
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public ClientResponse partialUpdate(Integer id, ClientPartialUpdate request) {
        log.info("Starting partialUpdate client");
        String message = String.format(CLIENT_NOT_FOUND_MESSAGE, id);
        Client entity = repository.findById(id).orElseThrow(() -> new ClientNotFoundException(message));
        entity.setActive(request.isActive());
        entity.setUpdatedAt(LocalDateTime.now());
        log.info("End partialUpdate client");
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        log.info("Starting delete client, {}", id);
        String message = String.format(CLIENT_NOT_FOUND_MESSAGE, id);
        Client entity = repository.findById(id).orElseThrow(() -> new ClientNotFoundException(message));
        log.info("End delete client");
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