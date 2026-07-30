package com.smartbill.config;

import com.smartbill.entity.Role;
import com.smartbill.entity.User;
import com.smartbill.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.findByEmail("admin@smartbill.com").isEmpty()) {
            User admin = new User(
                    "admin@smartbill.com",
                    passwordEncoder.encode("password123"),
                    Role.ADMIN
            );
            userRepository.save(admin);
            System.out.println("Default Admin User created: admin@smartbill.com / password123");
        }
    }
}
