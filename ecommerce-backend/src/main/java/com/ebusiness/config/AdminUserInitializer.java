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
    boolean existed = userRepository.existsByEmail(adminEmail);
    User admin = userRepository.findByEmail(adminEmail).orElseGet(User::new);
    admin.setEmail(adminEmail);
    admin.setPasswordHash(passwordEncoder.encode(adminPassword));
    admin.setNickname("admin");
    admin.setRole("ADMIN");
    admin.setStatus(1);
    userRepository.save(admin);
    if (existed) {
      System.out.println("Reset admin user password: " + adminEmail);
    } else {
      System.out.println("Created admin user: " + adminEmail);
    }

    final String csrEmail = "csr@local";
    final String csrPassword = "000000";
    boolean csrExisted = userRepository.existsByEmail(csrEmail);
    User csr = userRepository.findByEmail(csrEmail).orElseGet(User::new);
    csr.setEmail(csrEmail);
    csr.setPasswordHash(passwordEncoder.encode(csrPassword));
    csr.setNickname("客服CSR");
    csr.setRole("CSR");
    csr.setStatus(1);
    userRepository.save(csr);
    if (csrExisted) {
      System.out.println("Reset CSR user password: " + csrEmail);
    } else {
      System.out.println("Created CSR user: " + csrEmail);
    }
  }
}
