package ec.com.sofka.account_service.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import ec.com.sofka.account_service.dto.account.response.AccountResponse;
import ec.com.sofka.account_service.dto.movement.request.MovementPartialUpdateRequest;
import ec.com.sofka.account_service.dto.movement.request.MovementRequest;
import ec.com.sofka.account_service.dto.movement.response.MovementResponse;
import ec.com.sofka.account_service.model.enums.MovementTypeEnum;
import ec.com.sofka.account_service.service.MovementService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;


@WebMvcTest(MovementController.class)
@AutoConfigureMockMvc(addFilters = false)
class MovementControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MovementService movementService;

    private MovementResponse movementResponse(Long id, Long accountId, MovementTypeEnum type,
                                              String amount, String balance) {
        MovementResponse movementResponse = new MovementResponse();
        AccountResponse accountResponse = new AccountResponse();
        accountResponse.setId(accountId);
        movementResponse.setId(id);
        movementResponse.setAccount(accountResponse);
        movementResponse.setMovementType(type);
        movementResponse.setAmount(new BigDecimal(amount));
        movementResponse.setBalance(new BigDecimal(balance));
        movementResponse.setDate(LocalDateTime.of(2025, 8, 3, 10, 5));
        movementResponse.setCreatedAt(LocalDateTime.now());
        movementResponse.setUpdatedAt(LocalDateTime.now());
        return movementResponse;
    }

    private MovementRequest movementRequest(Long accountId, MovementTypeEnum type, String amount) {
        MovementRequest movementRequest = new MovementRequest();
        movementRequest.setAccountId(accountId);
        movementRequest.setMovementType(type);
        movementRequest.setAmount(new BigDecimal(amount));
        movementRequest.setDate(LocalDateTime.now());
        return movementRequest;
    }


    @Test
    @DisplayName("GET /api/movements devuelve página con contenido")
    void index_ok() throws Exception {
        List<MovementResponse> content = List.of(
                movementResponse(1L, 1L, MovementTypeEnum.DEPOSIT, "100.00", "2100.00"),
                movementResponse(2L, 1L, MovementTypeEnum.WITHDRAWAL, "50.00", "2050.00")
        );
        Page<MovementResponse> page = new PageImpl<>(content, PageRequest.of(0, 20), content.size());
        given(movementService.index(any(Pageable.class))).willReturn(page);

        mvc.perform(get("/api/movements").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].movement_type", is("DEPOSITO")))
                .andExpect(jsonPath("$.content[1].movement_type", is("RETIRO")));
    }

    @Test
    @DisplayName("GET /api/movements/account/{accountId} pagina por cuenta")
    void byAccount_ok() throws Exception {
        List<MovementResponse> content = List.of(
                movementResponse(3L, 2L, MovementTypeEnum.DEPOSIT, "600.00", "700.00")
        );
        Page<MovementResponse> page = new PageImpl<>(content, PageRequest.of(0, 10), content.size());
        given(movementService.byAccount(eq(2L), any(Pageable.class))).willReturn(page);

        mvc.perform(get("/api/movements/account/{accountId}", 2L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].movement_type", is("DEPOSITO")));
    }

    @Test
    @DisplayName("GET /api/movements/{id} devuelve movimiento")
    void show_ok() throws Exception {
        MovementResponse movementResponse = movementResponse(10L, 1L, MovementTypeEnum.WITHDRAWAL, "75.00", "1325.00");
        given(movementService.show(10L)).willReturn(movementResponse);

        mvc.perform(get("/api/movements/{id}", 10L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(10)))
                .andExpect(jsonPath("$.movement_type", is("RETIRO")));
    }

    @Test
    @DisplayName("POST /api/movements crea y devuelve 201 con Location")
    void create_created() throws Exception {
        MovementRequest movementRequest = movementRequest(1L, MovementTypeEnum.DEPOSIT, "150.00");
        MovementResponse created = movementResponse(20L, 1L, MovementTypeEnum.DEPOSIT, "150.00", "2250.00");

        given(movementService.create(any(MovementRequest.class))).willReturn(created);

        mvc.perform(post("/api/movements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(movementRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/movements/20"))
                .andExpect(jsonPath("$.id", is(20)))
                .andExpect(jsonPath("$.movement_type", is("DEPOSITO")));
    }

    @Test
    @DisplayName("POST /api/movements/account/{id}/deposit crea atajo y devuelve 201")
    void deposit_created() throws Exception {
        MovementResponse created = movementResponse(30L, 5L, MovementTypeEnum.DEPOSIT, "300.00", "1300.00");
        given(movementService.create(any(MovementRequest.class))).willReturn(created);

        mvc.perform(post("/api/movements/account/{accountId}/deposit", 5L)
                        .param("amount", "300.00"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/movements/30"))
                .andExpect(jsonPath("$.movement_type", is("DEPOSITO")));
    }

    @Test
    @DisplayName("POST /api/movements/account/{id}/withdraw crea atajo y devuelve 201")
    void withdraw_created() throws Exception {
        MovementResponse created = movementResponse(31L, 5L, MovementTypeEnum.WITHDRAWAL, "200.00", "1100.00");
        given(movementService.create(any(MovementRequest.class))).willReturn(created);

        mvc.perform(post("/api/movements/account/{accountId}/withdraw", 5L)
                        .param("amount", "200.00"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/movements/31"))
                .andExpect(jsonPath("$.movement_type", is("RETIRO")));
    }

    @Test
    @DisplayName("PUT /api/movements/{id} actualiza y devuelve 200")
    void update_ok() throws Exception {
        MovementRequest q = movementRequest(1L, MovementTypeEnum.WITHDRAWAL, "50.00");
        MovementResponse updated = movementResponse(40L, 1L, MovementTypeEnum.WITHDRAWAL, "50.00", "1950.00");

        given(movementService.update(eq(40L), any(MovementRequest.class))).willReturn(updated);

        mvc.perform(put("/api/movements/{id}", 40L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(q)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(40)))
                .andExpect(jsonPath("$.movement_type", is("RETIRO")))
                .andExpect(jsonPath("$.balance", is(1950.00)));
    }

    @Test
    @DisplayName("PATCH /api/movements/{id} aplica cambios parciales y devuelve 200")
    void patch_ok() throws Exception {
        MovementPartialUpdateRequest patch = new MovementPartialUpdateRequest();


        MovementResponse after = movementResponse(50L, 2L, MovementTypeEnum.DEPOSIT, "999.99", "1999.99");
        given(movementService.partialUpdate(eq(50L), any(MovementPartialUpdateRequest.class))).willReturn(after);

        mvc.perform(patch("/api/movements/{id}", 50L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patch)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(50)))
                .andExpect(jsonPath("$.amount", is(999.99)))
                .andExpect(jsonPath("$.movement_type", is("DEPOSITO")));
    }

    @Test
    @DisplayName("DELETE /api/movements/{id} devuelve 204")
    void delete_noContent() throws Exception {
        doNothing().when(movementService).delete(60L);

        mvc.perform(delete("/api/movements/{id}", 60L))
                .andExpect(status().isNoContent());
    }
}