package com.corebanking.integration;

import com.corebanking.modules.customer.entity.Customer;
import com.corebanking.modules.user.entity.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("TransferController integration tests")
class TransferControllerIT extends BaseIntegrationTest {

    private static final String ADVISOR_EMAIL = "transfer.advisor@test.com";
    private static final String PASSWORD       = "pass1234";

    private String  token;
    private String  origenId;
    private String  destinoId;

    @BeforeEach
    void setup() throws Exception {
        seedUser(ADVISOR_EMAIL, PASSWORD, Role.ADVISOR);
        token = loginAndGetToken(ADVISOR_EMAIL, PASSWORD);

        Customer c1 = seedCustomer("TransferOrigen",  "66666661");
        Customer c2 = seedCustomer("TransferDestino", "66666662");

        origenId  = createAccount(c1.getId().toString(), "5000.00");
        destinoId = createAccount(c2.getId().toString(), "1000.00");
    }

    @Test
    @DisplayName("POST /transfers — valid transfer returns 201 with referencia")
    void transfer_valid_returns201() throws Exception {
        String key = UUID.randomUUID().toString();

        mockMvc.perform(post("/v1/transfers")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content(transferBody(origenId, destinoId, "500.00", key)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("COMPLETADA"))
                .andExpect(jsonPath("$.referencia", startsWith("TRF-")))
                .andExpect(jsonPath("$.monto").value(500.0));
    }

    @Test
    @DisplayName("POST /transfers — duplicate idempotency key returns same transfer without re-executing")
    void transfer_duplicateKey_returnsSameTransfer() throws Exception {
        String key = UUID.randomUUID().toString();

        // First call
        String first = mockMvc.perform(post("/v1/transfers")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content(transferBody(origenId, destinoId, "200.00", key)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String firstRef = objectMapper.readTree(first).path("referencia").asText();

        // Second call with same key — must return the same referencia
        mockMvc.perform(post("/v1/transfers")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content(transferBody(origenId, destinoId, "200.00", key)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.referencia").value(firstRef));
    }

    @Test
    @DisplayName("POST /transfers — same origin and destination returns 422")
    void transfer_sameAccount_returns422() throws Exception {
        mockMvc.perform(post("/v1/transfers")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content(transferBody(origenId, origenId, "100.00", UUID.randomUUID().toString())))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errorCode").value("CBL-020"));
    }

    @Test
    @DisplayName("POST /transfers — insufficient balance returns 422 with CBL-022")
    void transfer_insufficientBalance_returns422() throws Exception {
        mockMvc.perform(post("/v1/transfers")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content(transferBody(origenId, destinoId, "999999.00", UUID.randomUUID().toString())))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errorCode").value("CBL-022"));
    }

    @Test
    @DisplayName("GET /transfers — returns paginated transfer list")
    void getTransfers_returnsList() throws Exception {
        mockMvc.perform(get("/v1/transfers")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private String createAccount(String customerId, String saldo) throws Exception {
        // Use admin token for account creation (ADVISOR role may not have permission)
        String adminToken = loginAndGetToken(ADVISOR_EMAIL, PASSWORD);

        String body = """
                {
                  "customerId": "%s",
                  "tipo": "AHORROS",
                  "moneda": "PEN",
                  "saldoInicial": %s
                }
                """.formatted(customerId, saldo);

        String response = mockMvc.perform(post("/v1/accounts")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).path("id").asText();
    }

    private String transferBody(String origen, String destino, String monto, String key) {
        return """
                {
                  "cuentaOrigenId": "%s",
                  "cuentaDestinoId": "%s",
                  "monto": %s,
                  "descripcion": "Test transfer",
                  "idempotencyKey": "%s"
                }
                """.formatted(origen, destino, monto, key);
    }
}
