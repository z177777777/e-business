package com.ebusiness.config;

import com.ebusiness.entity.User;
import com.ebusiness.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminUserInitializer implements CommandLineRunner {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public AdminUserInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public void run(String... args) throws Exception {
    final String adminEmail = "admin@local";
    final String adminPassword = "000000";
    if (!userRepository.existsByEmail(adminEmail)) {
      User admin = new User();
      admin.setEmail(adminEmail);
      admin.setPasswordHash(passwordEncoder.encode(adminPassword));
      admin.setNickname("admin");
      admin.setStatus(1);
      userRepository.save(admin);
      System.out.println("Created admin user: " + adminEmail);
    }
  }
}
