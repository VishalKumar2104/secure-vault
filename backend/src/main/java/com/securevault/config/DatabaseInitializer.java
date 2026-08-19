package com.securevault.config;

import com.securevault.entity.AccountStatus;
import com.securevault.entity.Role;
import com.securevault.entity.User;
import com.securevault.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    public DatabaseInitializer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Initialize Dev Admin Account
        if (!userRepository.existsByEmail("admin@securevault.com")) {
            User admin = User.builder()
                    .fullName("Vault Administrator")
                    .email("admin@securevault.com")
                    .role(Role.ADMIN)
                    .accountStatus(AccountStatus.ACTIVE)
                    .build();
            userRepository.save(admin);
            System.out.println("--- Vault Admin account registered: admin@securevault.com (Role: ADMIN) ---");
        }

        // Initialize Dev Test User Account
        if (!userRepository.existsByEmail("user@securevault.com")) {
            User user = User.builder()
                    .fullName("John Doe")
                    .email("user@securevault.com")
                    .role(Role.USER)
                    .accountStatus(AccountStatus.ACTIVE)
                    .build();
            userRepository.save(user);
            System.out.println("--- Test User account registered: user@securevault.com (Role: USER) ---");
        }
    }
}
