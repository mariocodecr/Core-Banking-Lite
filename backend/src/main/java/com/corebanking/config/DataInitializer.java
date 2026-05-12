package com.corebanking.config;

import com.corebanking.modules.user.entity.Role;
import com.corebanking.modules.user.entity.User;
import com.corebanking.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        List<User> users = List.of(
            User.create("admin@corebanking.com",    passwordEncoder.encode("Admin1234!"),    "System Administrator", Role.ADMIN),
            User.create("auditor@corebanking.com",  passwordEncoder.encode("Auditor1234!"),  "Financial Auditor",    Role.AUDITOR),
            User.create("advisor@corebanking.com",  passwordEncoder.encode("Advisor1234!"),  "Banking Advisor",      Role.ADVISOR),
            User.create("client@corebanking.com",   passwordEncoder.encode("Client1234!"),   "Test Client",          Role.CLIENT)
        );

        userRepository.saveAll(users);

        log.info("=== Dev users created ===");
        log.info("  admin@corebanking.com    / Admin1234!   [ADMIN]");
        log.info("  auditor@corebanking.com  / Auditor1234! [AUDITOR]");
        log.info("  advisor@corebanking.com  / Advisor1234! [ADVISOR]");
        log.info("  client@corebanking.com   / Client1234!  [CLIENT]");
        log.info("=========================");
    }
}
