package ec.com.sofka.account_service.controller;


import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ec.com.sofka.account_service.dto.report.StatementReportRow;
import ec.com.sofka.account_service.service.ReportService;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ReportController.class)
class ReportControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ReportService reportService;

    @Test
    @DisplayName("GET /api/reportes - OK con parámetros válidos (cliente, desde, hasta)")
    void statement_ok() throws Exception {
        // arrange
        List<StatementReportRow> stub = List.of();
        given(reportService.generateStatement(eq(1L), eq(LocalDate.parse("2024-08-01")),
                eq(LocalDate.parse("2024-08-31"))))
                .willReturn(stub);

        // act + assert
        mvc.perform(get("/api/reportes")
                        .param("cliente", "1")
                        .param("desde", "2024-08-01")
                        .param("hasta", "2024-08-31"))
                .andExpect(status().isOk());

        verify(reportService).generateStatement(1L, LocalDate.parse("2024-08-01"), LocalDate.parse("2024-08-31"));
    }

    @Test
    @DisplayName("GET /api/reportes - 400 si falta 'cliente'")
    void statement_missingCliente_badRequest() throws Exception {
        mvc.perform(get("/api/reportes")
                        // .param("cliente", "1") // falta a propósito
                        .param("desde", "2024-08-01")
                        .param("hasta", "2024-08-31"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/reportes - 400 si 'desde' tiene formato inválido")
    void statement_invalidDateFormat_badRequest() throws Exception {
        mvc.perform(get("/api/reportes")
                        .param("cliente", "1")
                        .param("desde", "2024-13-01") // mes inválido
                        .param("hasta", "2024-08-31"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/reportes - 400 si 'hasta' tiene formato inválido")
    void statement_invalidDateFormatHasta_badRequest() throws Exception {
        mvc.perform(get("/api/reportes")
                        .param("cliente", "1")
                        .param("desde", "2024-08-01")
                        .param("hasta", "2024-99-31")) // día/mes inválido
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/reportes - (opcional) 400 si desde > hasta (si el servicio valida este caso)")
    void statement_fromAfterTo_badRequest() throws Exception {
        given(reportService.generateStatement(eq(1L), eq(LocalDate.parse("2024-08-31")),
                eq(LocalDate.parse("2024-08-01"))))
                .willThrow(new IllegalArgumentException("Rango de fechas inválido"));

        mvc.perform(get("/api/reportes")
                        .param("cliente", "1")
                        .param("desde", "2024-08-31")
                        .param("hasta", "2024-08-01"))
                .andExpect(status().isBadRequest());
    }
}