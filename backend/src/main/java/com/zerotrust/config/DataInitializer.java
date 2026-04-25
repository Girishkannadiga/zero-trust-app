package com.zerotrust.config;

import com.zerotrust.model.Role;
import com.zerotrust.model.User;
import com.zerotrust.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        createUserIfAbsent("admin@zerotrust.com", "Admin123!", "Admin User",   Role.ROLE_ADMIN, true);
        createUserIfAbsent("user@zerotrust.com",  "User123!",  "Regular User", Role.ROLE_USER,  false);

        // Ensure admin always has MFA enabled (even if already created without it)
        userRepository.findByEmail("admin@zerotrust.com").ifPresent(user -> {
            if (!user.isMfaEnabled()) {
                user.setMfaEnabled(true);
                userRepository.save(user);
                log.info("Admin MFA enabled for demo");
            }
        });
    }

    private void createUserIfAbsent(String email, String rawPassword, String name,
                                    Role role, boolean mfaEnabled) {
        if (userRepository.findByEmail(email).isPresent()) return;

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .name(name)
                .role(role)
                .enabled(true)
                .mfaEnabled(mfaEnabled)
                .accountLocked(false)
                .failedLoginAttempts(0)
                .build();

        userRepository.save(user);
        log.info("Created default user: {} (password: {}, mfa: {})", email, rawPassword, mfaEnabled);
    }
}
