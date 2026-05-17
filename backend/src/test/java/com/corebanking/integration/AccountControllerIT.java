package com.corebanking.integration;

import com.corebanking.modules.account.repository.AccountRepository;
import com.corebanking.modules.customer.entity.Customer;
import com.corebanking.modules.user.entity.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.Matchers.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("AccountController integration tests")
class AccountControllerIT extends BaseIntegrationTest {

    private static final String ADMIN_EMAIL = "account.admin@test.com";
    private static final String PASSWORD    = "pass1234";

    @Autowired private AccountRepository accountRepository;

    private String  token;
    private Customer customer;

    @BeforeEach
    void setup() throws Exception {
        seedUser(ADMIN_EMAIL, PASSWORD, Role.ADMIN);
        token    = loginAndGetToken(ADMIN_EMAIL, PASSWORD);
        customer = seedCustomer("AccountCustomer", "55555555");
    }

    @Test
    @DisplayName("POST /accounts — creates account and returns 201 with Location header")
    void createAccount_validRequest_returns201() throws Exception {
        String body = """
                {
                  "customerId": "%s",
                  "tipo": "AHORROS",
                  "moneda": "PEN",
                  "saldoInicial": 1000.00
                }
                """.formatted(customer.getId());

        mockMvc.perform(post("/v1/accounts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("AHORROS"))
                .andExpect(jsonPath("$.estado").value("ACTIVA"))
                .andExpect(jsonPath("$.saldo").value(1000.0))
                .andExpect(header().exists("Location"));
    }

    @Test
    @DisplayName("GET /accounts — returns paginated list")
    void getAccounts_returnsPagedList() throws Exception {
        createAccountViaApi("AHORROS", "500.00");

        mockMvc.perform(get("/v1/accounts")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", not(empty())))
                .andExpect(jsonPath("$.totalElements", greaterThan(0)));
    }

    @Test
    @DisplayName("PATCH /accounts/{id}/freeze — active account becomes CONGELADA")
    void freezeAccount_activeAccount_becomesCongelada() throws Exception {
        String accountId = createAccountViaApi("AHORROS", "200.00");

        mockMvc.perform(patch("/v1/accounts/" + accountId + "/freeze")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CONGELADA"));
    }

    @Test
    @DisplayName("PATCH /accounts/{id}/unfreeze — frozen account becomes ACTIVA")
    void unfreezeAccount_frozenAccount_becomesActiva() throws Exception {
        String accountId = createAccountViaApi("AHORROS", "300.00");

        // Freeze first
        mockMvc.perform(patch("/v1/accounts/" + accountId + "/freeze")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // Then unfreeze
        mockMvc.perform(patch("/v1/accounts/" + accountId + "/unfreeze")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ACTIVA"));
    }

    @Test
    @DisplayName("PATCH /accounts/{id}/close — account with zero balance closes")
    void closeAccount_zeroBalance_becomesCerrada() throws Exception {
        String accountId = createAccountViaApi("CORRIENTE", "0");

        mockMvc.perform(patch("/v1/accounts/" + accountId + "/close")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CERRADA"));
    }

    @Test
    @DisplayName("PATCH /accounts/{id}/close — account with balance returns 422")
    void closeAccount_withBalance_returns422() throws Exception {
        String accountId = createAccountViaApi("AHORROS", "500.00");

        mockMvc.perform(patch("/v1/accounts/" + accountId + "/close")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errorCode").value("CBL-020"));
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private String createAccountViaApi(String tipo, String saldo) throws Exception {
        String body = """
                {
                  "customerId": "%s",
                  "tipo": "%s",
                  "moneda": "PEN",
                  "saldoInicial": %s
                }
                """.formatted(customer.getId(), tipo, saldo);

        String response = mockMvc.perform(post("/v1/accounts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).path("id").asText();
    }
}
