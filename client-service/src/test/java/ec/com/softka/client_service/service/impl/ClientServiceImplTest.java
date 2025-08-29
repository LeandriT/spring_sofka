package ec.com.softka.client_service.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import ec.com.softka.client_service.dto.request.ClientPartialUpdate;
import ec.com.softka.client_service.dto.request.ClientRequest;
import ec.com.softka.client_service.dto.response.ClientResponse;
import ec.com.softka.client_service.exception.ClientNotFoundException;
import ec.com.softka.client_service.mapper.ClientMapper;
import ec.com.softka.client_service.model.Client;
import ec.com.softka.client_service.repository.ClientRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class ClientServiceImplTest {

    @Mock
    private ClientRepository repository;
    @Mock
    private ClientMapper mapper;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ClientServiceImpl service;

    private Client entity;
    private ClientResponse response;
    private ClientRequest request;

    @BeforeEach
    void setUp() {
        entity = new Client();
        entity.setId(1L);
        entity.setDni("0102030405");
        entity.setName("John");
        entity.setActive(true);

        response = new ClientResponse();
        response.setDni(entity.getDni());
        response.setName(entity.getName());
        response.setActive(entity.isActive());

        request = new ClientRequest();
        request.setDni(entity.getDni());
        request.setName("John");
        request.setPassword("plain");
        request.setActive(true);
    }


    @Test
    @DisplayName("index: devuelve página mapeada")
    void index_ok() {
        //arrange
        when(repository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(entity), PageRequest.of(0, 20), 1));
        when(mapper.toResponse(entity)).thenReturn(response);
        //act
        Page<ClientResponse> page = service.index(PageRequest.of(0, 20));
        //assert
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().getFirst().getDni()).isEqualTo("0102030405");
        verify(repository).findAll(any(PageRequest.class));
        verify(mapper).toResponse(entity);
    }


    @Test
    @DisplayName("show: 200 cuando existe")
    void show_ok() {
        //arrange
        when(repository.findById(1)).thenReturn(Optional.of(entity));
        when(mapper.toResponse(entity)).thenReturn(response);
        //act
        ClientResponse res = service.show(1);
        //assert
        assertThat(res.getDni()).isEqualTo("0102030405");
        verify(repository).findById(1);
        verify(mapper).toResponse(entity);
    }

    @Test
    @DisplayName("show: 404 cuando no existe")
    void show_notFound() {
        //arrange
        when(repository.findById(99)).thenReturn(Optional.empty());
        //act
        assertThatThrownBy(() -> service.show(99))
                .isInstanceOf(ClientNotFoundException.class)
                .hasMessageContaining("99");
        //assert
        verify(repository).findById(99);
        verifyNoInteractions(mapper);
    }


    @Test
    @DisplayName("create: encripta password, guarda y mapea")
    void create_ok() {
        //arrange
        when(repository.existsByDni(request.getDni())).thenReturn(false);
        when(mapper.toModel(request)).thenReturn(new Client());
        when(passwordEncoder.encode("plain")).thenReturn("ENC");
        ArgumentCaptor<Client> entityCaptor = ArgumentCaptor.forClass(Client.class);
        when(repository.save(any(Client.class))).thenAnswer(inv -> {
            Client c = inv.getArgument(0);
            c.setId(123L);
            return c;
        });
        when(mapper.toResponse(any(Client.class))).thenReturn(response);
        //act
        ClientResponse res = service.create(request);
        //assert
        assertThat(res).isNotNull();
        verify(repository).existsByDni("0102030405");
        verify(passwordEncoder).encode("plain");
        verify(repository).save(entityCaptor.capture());
        Client saved = entityCaptor.getValue();
        assertThat(saved.getPassword()).isEqualTo("ENC");
        verify(mapper).toResponse(saved);
    }

    @Test
    @DisplayName("create: lanza excepción si DNI ya existe")
    void create_duplicateDni() {
        //arrange
        when(repository.existsByDni(request.getDni())).thenReturn(true);
        //act
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(ClientNotFoundException.class)
                .hasMessageContaining("DNI")
                .hasMessageContaining(request.getDni());
        //assert
        verify(repository).existsByDni("0102030405");
        verifyNoMoreInteractions(repository);
        verifyNoInteractions(passwordEncoder, mapper);
    }

    @Test
    @DisplayName("update: mapea request→entity, setea updatedAt y guarda")
    void update_ok() {
        //arrange
        when(repository.findById(1)).thenReturn(Optional.of(entity));
        when(mapper.toResponse(entity)).thenReturn(response);
        ClientRequest req = new ClientRequest();
        req.setName("Jane");
        req.setPassword("new");
        req.setActive(false);
        //act
        ClientResponse res = service.update(1, req);
        //assert
        assertThat(res).isNotNull();
        assertThat(entity.getUpdatedAt()).isNotNull();
        verify(mapper).updateModel(req, entity);
        verify(repository).save(entity);
        verify(mapper).toResponse(entity);
    }

    @Test
    @DisplayName("update: 404 cuando no existe")
    void update_notFound() {
        //arrange
        when(repository.findById(77)).thenReturn(Optional.empty());
        //act
        assertThatThrownBy(() -> service.update(77, request))
                .isInstanceOf(ClientNotFoundException.class)
                .hasMessageContaining("77");
        //assert
        verify(repository).findById(77);
        verifyNoInteractions(mapper);
    }

    @Test
    @DisplayName("partialUpdate: modifica active, setea updatedAt y mapea")
    void partialUpdate_ok() {
        //arrange
        when(repository.findById(1)).thenReturn(Optional.of(entity));
        when(mapper.toResponse(entity)).thenReturn(response);
        ClientPartialUpdate patch = new ClientPartialUpdate(false);
        //act
        ClientResponse res = service.partialUpdate(1, patch);
        //assert
        assertThat(res).isNotNull();
        assertThat(entity.isActive()).isFalse();
        assertThat(entity.getUpdatedAt()).isNotNull();

        verify(repository).findById(1);
        verify(mapper).toResponse(entity);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("partialUpdate: 404 cuando no existe")
    void partialUpdate_notFound() {
        //arrange
        when(repository.findById(55)).thenReturn(Optional.empty());
        //act
        assertThatThrownBy(() -> service.partialUpdate(55, new ClientPartialUpdate(true)))
                .isInstanceOf(ClientNotFoundException.class)
                .hasMessageContaining("55");
        //assert
        verify(repository).findById(55);
        verifyNoInteractions(mapper);
    }


    @Test
    @DisplayName("delete: elimina cuando existe")
    void delete_ok() {
        //arrange
        when(repository.findById(1)).thenReturn(Optional.of(entity));
        //act
        service.delete(1);
        //assert
        verify(repository).findById(1);
        verify(repository).delete(entity);
    }

    @Test
    @DisplayName("delete: 404 cuando no existe")
    void delete_notFound() {
        //arrange
        when(repository.findById(9)).thenReturn(Optional.empty());
        //act
        assertThatThrownBy(() -> service.delete(9))
                .isInstanceOf(ClientNotFoundException.class)
                .hasMessageContaining("9");
        //assert
        verify(repository).findById(9);
        verify(repository, never()).delete(any());
    }
}