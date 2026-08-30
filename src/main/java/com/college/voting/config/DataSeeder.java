package com.college.voting.config;

import com.college.voting.entity.Admin;
import com.college.voting.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.admin-email:admin@college.edu}")
    private String seedAdminEmail;

    @Value("${app.seed.admin-password:Admin@12345}")
    private String seedAdminPassword;

    @Override
    public void run(String... args) {
        if (adminRepository.existsByEmail(seedAdminEmail)) {
            return;
        }
        Admin admin = Admin.builder()
                .fullName("System Administrator")
                .email(seedAdminEmail)
                .password(passwordEncoder.encode(seedAdminPassword))
                .build();
        adminRepository.save(admin);
        System.out.println("Seeded default admin -> email: " + seedAdminEmail + " password: " + seedAdminPassword);
    }
}
