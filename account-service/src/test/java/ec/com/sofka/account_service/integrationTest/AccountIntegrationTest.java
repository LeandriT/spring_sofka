package ec.com.sofka.account_service.integrationTest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ec.com.sofka.account_service.model.Account;
import ec.com.sofka.account_service.model.enums.AccountTypeEnum;
import ec.com.sofka.account_service.repository.AccountRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AccountIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    private Long testAccountId;

    @BeforeEach
    void setUp() {

        Account testAccount = new Account();
        testAccount.setAccountNumber("ACC-10002");
        testAccount.setAccountType(AccountTypeEnum.SAVINGS);
        testAccount.setInitialBalance(BigDecimal.valueOf(100.0));
        testAccount.setClientId(2L);
        testAccount.setActive(true);

        Account savedAccount = accountRepository.save(testAccount);
        testAccountId = savedAccount.getId();
    }

    @Test
    void getAccountById_ShouldReturnAccount_WhenAccountExists() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/accounts/{id}", testAccountId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testAccountId))
                .andExpect(jsonPath("$.account_number").value("ACC-10002"))
                .andExpect(jsonPath("$.account_type").value("AHORROS"))
                .andExpect(jsonPath("$.initial_balance").value(BigDecimal.valueOf(100.0)))
                .andExpect(jsonPath("$.client_id").value(2))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void getAccountById_ShouldReturnNotFound_WhenAccountDoesNotExist() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/accounts/{id}", 99999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAccountById_ShouldReturnBadRequest_WhenIdIsNotNumeric() throws Exception {
        mockMvc.perform(get("/api/accounts/{id}", "abc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}