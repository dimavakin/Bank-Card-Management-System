package com.bankcards.config;

import com.bankcards.entity.Role;
import com.bankcards.repository.UserRepository;
import com.bankcards.entity.User;
import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminUserInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdminUserInitializer.class);

    UserRepository userRepository;
    PasswordEncoder passwordEncoder;

    @PostConstruct
    public void createAdminUser() {
        if (userRepository.findByEmail("admin@example.com").isEmpty()) {
            User admin = new User();
            admin.setFirstName("admin");
            admin.setLastName("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEmail("admin@example.com");
            admin.setRoles(Set.of(Role.ROLE_ADMIN, Role.ROLE_USER));
            userRepository.save(admin);
            LOGGER.info("Admin user created successfully");
        }
    }
}