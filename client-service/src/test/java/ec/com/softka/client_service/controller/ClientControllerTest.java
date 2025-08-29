package ec.com.softka.client_service.controller;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import ec.com.softka.client_service.dto.request.ClientPartialUpdate;
import ec.com.softka.client_service.dto.request.ClientRequest;
import ec.com.softka.client_service.dto.response.ClientResponse;
import ec.com.softka.client_service.exception.ClientNotFoundException;
import ec.com.softka.client_service.exception.GlobalExceptionHandler;
import ec.com.softka.client_service.service.ClientService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = ClientController.class)
@Import(GlobalExceptionHandler.class)
class ClientControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    ClientService clientService;


    private ClientResponse sampleResponse() {
        ClientResponse r = new ClientResponse();
        r.setDni("0401590039");
        r.setName("John Doe");
        r.setActive(true);
        return r;
    }

    private ClientRequest validCreateRequest() {
        ClientRequest req = new ClientRequest();
        req.setName("John Doe");
        req.setDni("0401590039");
        req.setGender("M");
        req.setAge(30);
        req.setAddress("Av. Siempre Viva 123");
        req.setPhone("0999999999");
        req.setPassword("secret");
        req.setActive(true);
        return req;
    }


    @Test
    @DisplayName("GET /api/clients -> 200 con página de clientes")
    void index_ok() throws Exception {
        Mockito.when(clientService.index(any()))
                .thenReturn(new PageImpl<>(List.of(sampleResponse()), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/clients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].dni", is(this.sampleResponse().getDni())));
    }

    @Test
    @DisplayName("GET /api/clients/{id} -> 200 cuando existe")
    void show_ok() throws Exception {
        Mockito.when(clientService.show(1)).thenReturn(sampleResponse());

        mockMvc.perform(get("/api/clients/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dni", is(this.sampleResponse().getDni())))
                .andExpect(jsonPath("$.name", is(this.sampleResponse().getName())));
    }

    @Test
    @DisplayName("GET /api/clients/{id} -> 404 cuando no existe")
    void show_notFound() throws Exception {
        Mockito.when(clientService.show(99))
                .thenThrow(new ClientNotFoundException("Client with ID 99 does not exist"));

        mockMvc.perform(get("/api/clients/{id}", 99))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("does not exist")))
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    @DisplayName("POST /api/clients -> 201 con Location y body correcto")
    void create_ok() throws Exception {
        ClientRequest req = validCreateRequest();
        ClientResponse res = sampleResponse();

        Mockito.when(clientService.create(any(ClientRequest.class))).thenReturn(res);

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/clients/" + res.getDni()))
                .andExpect(jsonPath("$.dni", is(this.sampleResponse().getDni())));
    }

    @Test
    @DisplayName("POST /api/clients -> 400 por validación (grupos OnCreate)")
    void create_validationError() throws Exception {
        ClientRequest bad = validCreateRequest();
        bad.setDni("");

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", anyOf(is("Datos inválidos"), notNullValue())))
                .andExpect(jsonPath("$.details.dni", notNullValue()));
    }

    @Test
    @DisplayName("PUT /api/clients/{id} -> 200 cuando actualiza")
    void update_ok() throws Exception {
        ClientRequest req = validCreateRequest();
        ClientResponse res = sampleResponse();
        res.setName("Jane Doe");

        Mockito.when(clientService.update(eq(1), any(ClientRequest.class))).thenReturn(res);

        mockMvc.perform(put("/api/clients/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(res.getName())));
    }

    @Test
    @DisplayName("PUT /api/clients/{id} -> 404 si no existe")
    void update_notFound() throws Exception {
        Mockito.when(clientService.update(eq(77), any(ClientRequest.class)))
                .thenThrow(new ClientNotFoundException("Client with ID 77 does not exist"));

        mockMvc.perform(put("/api/clients/{id}", 77)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    @DisplayName("PATCH /api/clients/{id} -> 200 cuando actualiza parcialmente")
    void patch_ok() throws Exception {
        ClientPartialUpdate patch = new ClientPartialUpdate(true);
        ClientResponse res = sampleResponse();
        res.setActive(true);

        Mockito.when(clientService.partialUpdate(eq(5), any(ClientPartialUpdate.class)))
                .thenReturn(res);

        mockMvc.perform(patch("/api/clients/{id}", 5)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patch)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active", is(true)));
    }

    @Test
    @DisplayName("DELETE /api/clients/{id} -> 204")
    void delete_ok() throws Exception {
        Mockito.doNothing().when(clientService).delete(10);

        mockMvc.perform(delete("/api/clients/{id}", 10))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/clients/{id} -> 404 cuando no existe")
    void delete_notFound() throws Exception {
        Mockito.doThrow(new ClientNotFoundException("Client with ID 88 does not exist"))
                .when(clientService).delete(88);

        mockMvc.perform(delete("/api/clients/{id}", 88))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }
}