package ec.com.sofka.account_service.integrationTest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ec.com.sofka.account_service.model.Account;
import ec.com.sofka.account_service.model.Movement;
import ec.com.sofka.account_service.model.enums.AccountTypeEnum;
import ec.com.sofka.account_service.model.enums.MovementTypeEnum;
import ec.com.sofka.account_service.repository.AccountRepository;
import ec.com.sofka.account_service.repository.MovementRepository;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import org.hamcrest.Matchers;
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
class MovementControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    MovementRepository movementRepository;

    Account account;

    @BeforeEach
    void setup() {
        movementRepository.deleteAll();
        accountRepository.deleteAll();

        account = new Account();
        account.setClientId(1L);

        account.setAccountNumber("ACC-" + UUID.randomUUID());
        account.setAccountType(AccountTypeEnum.SAVINGS);
        account.setInitialBalance(new BigDecimal("1000.00"));
        account.setActive(true);
        account.setDeleted(false);
        account = accountRepository.save(account);
    }


    @Test
    void index_ShouldReturnOk() throws Exception {
        Movement movement = new Movement();
        movement.setAccount(account);
        movement.setMovementType(MovementTypeEnum.DEPOSIT);
        movement.setAmount(new BigDecimal("100.00"));
        movement.setBalance(new BigDecimal("1100.00"));
        movement.setDate(LocalDateTime.now());
        movementRepository.save(movement);

        mockMvc.perform(get("/api/movements")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void byAccount_ShouldReturnListForAccount() throws Exception {
        Movement movement = new Movement();
        movement.setAccount(account);
        movement.setMovementType(MovementTypeEnum.DEPOSIT);
        movement.setAmount(new BigDecimal("100.00"));
        movement.setBalance(new BigDecimal("1100.00"));
        movement.setDate(LocalDateTime.now());

        Movement movement1 = new Movement();
        movement1.setAccount(account);
        movement1.setMovementType(MovementTypeEnum.WITHDRAWAL);
        movement1.setAmount(new BigDecimal("50.00"));
        movement1.setBalance(new BigDecimal("1050.00"));
        movement1.setDate(LocalDateTime.now());

        movementRepository.saveAll(Set.of(movement, movement1));

        mockMvc.perform(get("/api/movements/account/{accountId}", account.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", Matchers.hasSize(Matchers.greaterThanOrEqualTo(2))));
    }

    @Test
    void show_ShouldReturnMovementAnd404WhenNotFound() throws Exception {
        Movement movement = new Movement();
        movement.setAccount(account);
        movement.setMovementType(MovementTypeEnum.DEPOSIT);
        movement.setAmount(new BigDecimal("120.00"));
        movement.setBalance(new BigDecimal("1120.00"));
        movement.setDate(LocalDateTime.now());
        movement = movementRepository.save(movement);

        mockMvc.perform(get("/api/movements/{id}", movement.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(movement.getId()));

        mockMvc.perform(get("/api/movements/{id}", 999999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }


    @Test
    void partialUpdate_ShouldChangeOnlyDate() throws Exception {
        Movement movement = new Movement();
        movement.setAccount(account);
        movement.setMovementType(MovementTypeEnum.DEPOSIT);
        movement.setAmount(new BigDecimal("50.00"));
        movement.setBalance(new BigDecimal("1050.00"));
        movement.setDate(LocalDateTime.now());
        movement = movementRepository.save(movement);

        String newDate = "2025-08-01T10:15:00";

        String patch = """
                {
                  "date": "%s"
                }
                """.formatted(newDate);

        mockMvc.perform(patch("/api/movements/{id}", movement.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patch.getBytes(StandardCharsets.UTF_8)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value(newDate));
    }

    @Test
    void delete_ShouldReturn204_Then404OnShow() throws Exception {
        Movement movement = new Movement();
        movement.setAccount(account);
        movement.setMovementType(MovementTypeEnum.DEPOSIT);
        movement.setAmount(new BigDecimal("80.00"));
        movement.setBalance(new BigDecimal("1080.00"));
        movement.setDate(LocalDateTime.now());
        movement = movementRepository.save(movement);

        mockMvc.perform(delete("/api/movements/{id}", movement.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/movements/{id}", movement.getId()))
                .andExpect(status().isNotFound());
    }
}