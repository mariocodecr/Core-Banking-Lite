package com.corebanking.integration;

import com.corebanking.modules.customer.entity.Customer;
import com.corebanking.modules.customer.entity.CustomerStatus;
import com.corebanking.modules.customer.entity.DocumentType;
import com.corebanking.modules.customer.repository.CustomerRepository;
import com.corebanking.modules.user.entity.Role;
import com.corebanking.modules.user.entity.User;
import com.corebanking.modules.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.LocalDate;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Base class for all integration tests.
 *
 * Uses a shared static PostgreSQL container (started once per JVM) to minimize
 * container startup overhead. Each test class inherits the container and Spring
 * context is reused via Spring's test context caching.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("integration-test")
public abstract class BaseIntegrationTest {

    // Singleton pattern: started once per JVM, stopped via shutdown hook.
    // Using @Container on a static field in a base class causes Testcontainers to
    // stop the container after each concrete test class, breaking subsequent classes.
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("corebanking_it")
            .withUsername("it_user")
            .withPassword("it_pass");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void overrideDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",      POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    }

    @Autowired protected MockMvc       mockMvc;
    @Autowired protected ObjectMapper  objectMapper;
    @Autowired protected UserRepository     userRepository;
    @Autowired protected CustomerRepository customerRepository;
    @Autowired protected PasswordEncoder    passwordEncoder;

    // ─── Seed helpers ─────────────────────────────────────────────────────────

    protected User seedUser(String email, String rawPassword, Role role) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            User u = User.create(email, passwordEncoder.encode(rawPassword), "Test " + role.name(), role);
            return userRepository.save(u);
        });
    }

    protected Customer seedCustomer(String nombres, String documento) {
        return customerRepository.findByNumeroDocumento(documento).orElseGet(() -> {
            Customer c = new Customer();
            c.setNombres(nombres);
            c.setApellidos("Integration");
            c.setEmail(nombres.toLowerCase() + "@test.com");
            c.setTipoDocumento(DocumentType.DNI);
            c.setNumeroDocumento(documento);
            c.setFechaNacimiento(LocalDate.of(1990, 1, 1));
            c.setEstado(CustomerStatus.ACTIVO);
            return customerRepository.save(c);
        });
    }

    // ─── Auth helper ──────────────────────────────────────────────────────────

    protected String loginAndGetToken(String email, String password) throws Exception {
        String body = """
                {"email": "%s", "password": "%s"}
                """.formatted(email, password);

        MvcResult result = mockMvc.perform(
                post("/api/v1/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(body)
        ).andReturn();

        String json = result.getResponse().getContentAsString();
        return objectMapper.readTree(json).path("accessToken").asText();
    }
}
