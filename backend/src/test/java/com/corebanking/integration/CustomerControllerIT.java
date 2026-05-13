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

@DisplayName("CustomerController integration tests")
class CustomerControllerIT extends BaseIntegrationTest {

    private static final String ADMIN_EMAIL = "customer.admin@test.com";
    private static final String PASSWORD    = "pass1234";

    private String token;

    @BeforeEach
    void setup() throws Exception {
        seedUser(ADMIN_EMAIL, PASSWORD, Role.ADMIN);
        token = loginAndGetToken(ADMIN_EMAIL, PASSWORD);
    }

    @Test
    @DisplayName("POST /customers — valid payload creates customer and returns 201")
    void createCustomer_validPayload_returns201() throws Exception {
        String body = """
                {
                  "tipoDocumento": "DNI",
                  "numeroDocumento": "87654321",
                  "nombres": "Carlos",
                  "apellidos": "Ramirez",
                  "email": "carlos.ramirez@test.com",
                  "fechaNacimiento": "1985-06-15"
                }
                """;

        mockMvc.perform(post("/api/v1/customers")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombres").value("Carlos"))
                .andExpect(jsonPath("$.estado").value("ACTIVO"))
                .andExpect(header().exists("Location"));
    }

    @Test
    @DisplayName("POST /customers — duplicate document returns 409")
    void createCustomer_duplicateDocument_returns409() throws Exception {
        seedCustomer("Duplicado", "11111111");

        String body = """
                {
                  "tipoDocumento": "DNI",
                  "numeroDocumento": "11111111",
                  "nombres": "Otro",
                  "apellidos": "Cliente",
                  "fechaNacimiento": "1990-01-01"
                }
                """;

        mockMvc.perform(post("/api/v1/customers")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("CBL-021"));
    }

    @Test
    @DisplayName("GET /customers — returns paginated list")
    void getCustomers_returnsPagedList() throws Exception {
        seedCustomer("Lista", "22222222");

        mockMvc.perform(get("/api/v1/customers")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", not(empty())))
                .andExpect(jsonPath("$.totalElements", greaterThan(0)));
    }

    @Test
    @DisplayName("GET /customers/{id} — existing id returns customer")
    void getCustomerById_existingId_returnsCustomer() throws Exception {
        Customer saved = seedCustomer("GetById", "33333333");

        mockMvc.perform(get("/api/v1/customers/" + saved.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId().toString()));
    }

    @Test
    @DisplayName("GET /customers/{id} — unknown id returns 404")
    void getCustomerById_unknownId_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/customers/" + UUID.randomUUID())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("CBL-003"));
    }

    @Test
    @DisplayName("DELETE /customers/{id} — soft deletes and returns 204")
    void deleteCustomer_existingId_returns204() throws Exception {
        Customer saved = seedCustomer("ParaBorrar", "44444444");

        mockMvc.perform(delete("/api/v1/customers/" + saved.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /customers — unauthenticated request returns 401")
    void getCustomers_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/customers"))
                .andExpect(status().isUnauthorized());
    }
}
