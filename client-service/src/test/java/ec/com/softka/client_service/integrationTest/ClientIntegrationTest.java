package ec.com.softka.client_service.integrationTest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ec.com.softka.client_service.model.Client;
import ec.com.softka.client_service.repository.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
class ClientIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClientRepository clientRepository;
    @MockitoBean
    private PasswordEncoder passwordEncoder;
    private Long testClientId;

    @BeforeEach
    void setUp() {

        Client testClient = new Client();
        testClient.setDni("1234567890");
        testClient.setName("John Doe");
        testClient.setPassword("password123");
        testClient.setGender("M");
        testClient.setAge(30);
        testClient.setAddress("123 Main Street");
        testClient.setPhone("555-0123");
        testClient.setActive(true);

        Client savedClient = clientRepository.save(testClient);
        testClientId = savedClient.getId();
    }

    @Test
    void getClientById_ShouldReturnClient_WhenClientExists() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/clients/{id}", testClientId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testClientId))
                .andExpect(jsonPath("$.dni").value("1234567890"))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.gender").value("M"))
                .andExpect(jsonPath("$.age").value(30))
                .andExpect(jsonPath("$.address").value("123 Main Street"))
                .andExpect(jsonPath("$.phone").value("555-0123"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void getClientById_ShouldReturnNotFound_WhenClientDoesNotExist() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/clients/{id}", 99999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void getClientById_ShouldReturnBadRequest_WhenIdIsNotNumeric() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/clients/{id}", "abc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getClientById_ShouldHandleInactiveClient_WhenClientIsInactive() throws Exception {
        // Arrange
        Client inactiveClient = new Client();
        inactiveClient.setDni("0987654321");
        inactiveClient.setName("Jane Smith");
        inactiveClient.setPassword("password456");
        inactiveClient.setGender("F");
        inactiveClient.setAge(25);
        inactiveClient.setAddress("456 Oak Avenue");
        inactiveClient.setPhone("555-9876");
        inactiveClient.setActive(false);
        Client savedInactiveClient = clientRepository.save(inactiveClient);

        // Act & Assert
        mockMvc.perform(get("/api/clients/{id}", savedInactiveClient.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedInactiveClient.getId()))
                .andExpect(jsonPath("$.dni").value("0987654321"))
                .andExpect(jsonPath("$.name").value("Jane Smith"))
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void getClientById_ShouldHandleClientWithNullOptionalFields() throws Exception {

        Client minimalClient = new Client();
        minimalClient.setDni("1111111111");
        minimalClient.setName("Minimal Client");
        minimalClient.setPassword("password789");
        minimalClient.setGender("M");
        minimalClient.setAge(0);
        minimalClient.setAddress(null);
        minimalClient.setPhone(null);
        minimalClient.setActive(true);

        Client savedMinimalClient = clientRepository.save(minimalClient);

        // Act & Assert
        mockMvc.perform(get("/api/clients/{id}", savedMinimalClient.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedMinimalClient.getId()))
                .andExpect(jsonPath("$.dni").value("1111111111"))
                .andExpect(jsonPath("$.name").value("Minimal Client"))
                .andExpect(jsonPath("$.age").value(0))
                .andExpect(jsonPath("$.address").isEmpty())
                .andExpect(jsonPath("$.phone").isEmpty())
                .andExpect(jsonPath("$.active").value(true));
    }
}
