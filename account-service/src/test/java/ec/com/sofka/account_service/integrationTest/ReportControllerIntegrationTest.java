package ec.com.sofka.account_service.integrationTest;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ec.com.sofka.account_service.client.ClientHttp;
import ec.com.sofka.account_service.client.dto.ClientDto;
import ec.com.sofka.account_service.repository.AccountRepository;
import ec.com.sofka.account_service.repository.MovementRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ReportControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    AccountRepository accountRepository;
    @Autowired
    MovementRepository movementRepository;
    @MockBean
    ClientHttp clientHttp;

    @Test
    void statement_ok() throws Exception {
        ClientDto dto = new ClientDto();
        dto.setName("Cliente Demo");
        given(clientHttp.show(1L)).willReturn(dto);
        // seed minimal (crear cuenta y movimiento) ...
        mockMvc.perform(get("/api/reportes")
                        .param("cliente", "1")
                        .param("desde", "2024-08-01")
                        .param("hasta", "2024-08-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}