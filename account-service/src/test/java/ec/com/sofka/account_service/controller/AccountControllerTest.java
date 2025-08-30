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
import ec.com.sofka.account_service.dto.account.request.AccountPartialUpdateRequest;
import ec.com.sofka.account_service.dto.account.request.AccountRequest;
import ec.com.sofka.account_service.dto.account.response.AccountResponse;
import ec.com.sofka.account_service.model.enums.AccountTypeEnum;
import ec.com.sofka.account_service.service.AccountService;
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
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;


@WebMvcTest(AccountController.class)
@AutoConfigureMockMvc(addFilters = false)
class AccountControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AccountService accountService;


    private AccountResponse sampleResponse(Long id, Long clientId, String number, AccountTypeEnum type,
                                           BigDecimal initial) {
        AccountResponse accountResponse = new AccountResponse();
        accountResponse.setId(id);
        accountResponse.setClientId(clientId);
        accountResponse.setAccountNumber(number);
        accountResponse.setAccountType(type);
        accountResponse.setInitialBalance(initial);
        accountResponse.setActive(true);
        accountResponse.setCreatedAt(LocalDateTime.now());
        accountResponse.setUpdatedAt(LocalDateTime.now());
        return accountResponse;
    }

    private AccountRequest sampleRequest(Long clientId, String number, AccountTypeEnum type, BigDecimal initial) {
        AccountRequest req = new AccountRequest();
        req.setClientId(clientId);
        req.setAccountNumber(number);
        req.setAccountType(type);
        req.setInitialBalance(initial);
        req.setActive(true);
        return req;
    }


    @Test
    @DisplayName("GET /api/accounts devuelve página con contenido")
    void index_ok() throws Exception {
        List<AccountResponse> content = List.of(
                sampleResponse(1L, 1L, "478758", AccountTypeEnum.SAVINGS, new BigDecimal("2000.00")),
                sampleResponse(2L, 2L, "225487", AccountTypeEnum.CURRENT, new BigDecimal("100.00"))
        );
        Page<AccountResponse> page =
                new PageImpl<>(content, PageRequest.of(0, 20, Sort.by("id").ascending()), content.size());

        given(accountService.index(any(Pageable.class))).willReturn(page);

        mvc.perform(get("/api/accounts")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id", is(1)))
                .andExpect(jsonPath("$.content[0].account_number", is("478758")))
                .andExpect(jsonPath("$.content[0].account_type", is("AHORROS")))
                .andExpect(jsonPath("$.content[1].account_type", is("CORRIENTE")));
    }

    @Test
    @DisplayName("GET /api/accounts/{id} devuelve la cuenta")
    void show_ok() throws Exception {
        AccountResponse resp = sampleResponse(1L, 1L, "478758", AccountTypeEnum.SAVINGS, new BigDecimal("2000.00"));
        given(accountService.show(1L)).willReturn(resp);

        mvc.perform(get("/api/accounts/{id}", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.account_number", is("478758")))
                .andExpect(jsonPath("$.account_type", is("AHORROS")));
    }

    @Test
    @DisplayName("POST /api/accounts crea y devuelve 201 con Location")
    void create_created() throws Exception {
        AccountRequest req = sampleRequest(1L, "585545", AccountTypeEnum.CURRENT, new BigDecimal("1000.00"));
        AccountResponse created = sampleResponse(5L, 1L, "585545", AccountTypeEnum.CURRENT, new BigDecimal("1000.00"));

        given(accountService.create(any(AccountRequest.class))).willReturn(created);

        mvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/accounts/5"))
                .andExpect(jsonPath("$.id", is(5)))
                .andExpect(jsonPath("$.account_number", is("585545")))
                .andExpect(jsonPath("$.account_type", is("CORRIENTE")));
    }

    @Test
    @DisplayName("PUT /api/accounts/{id} actualiza y devuelve 200")
    void update_ok() throws Exception {
        AccountRequest req = sampleRequest(1L, "478758", AccountTypeEnum.SAVINGS, new BigDecimal("2500.00"));
        AccountResponse updated = sampleResponse(1L, 1L, "478758", AccountTypeEnum.SAVINGS, new BigDecimal("2500.00"));

        given(accountService.update(eq(1L), any(AccountRequest.class))).willReturn(updated);

        mvc.perform(put("/api/accounts/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.initial_balance", is(2500.00)))
                .andExpect(jsonPath("$.account_type", is("AHORROS")));
    }

    @Test
    @DisplayName("PATCH /api/accounts/{id} aplica cambios parciales y devuelve 200")
    void patch_ok() throws Exception {
        AccountPartialUpdateRequest patch = new AccountPartialUpdateRequest();
        patch.setAccountNumber("999999");
        patch.setStatus(Boolean.FALSE);

        AccountResponse after = sampleResponse(1L, 1L, "999999", AccountTypeEnum.SAVINGS, new BigDecimal("2000.00"));
        after.setActive(false);

        given(accountService.partialUpdate(eq(1L), any(AccountPartialUpdateRequest.class))).willReturn(after);

        mvc.perform(patch("/api/accounts/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patch)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.account_number", is("999999")))
                .andExpect(jsonPath("$.active", is(false)));
    }

    @Test
    @DisplayName("DELETE /api/accounts/{id} devuelve 204")
    void delete_noContent() throws Exception {
        doNothing().when(accountService).delete(1L);

        mvc.perform(delete("/api/accounts/{id}", 1L))
                .andExpect(status().isNoContent());
    }
}