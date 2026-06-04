package com.ebusiness.repository;

import com.ebusiness.entity.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByEmail(String email);

  boolean existsByEmail(String email);

  long countByEmailNot(String email);

  List<User> findTop5ByEmailNotOrderByCreatedAtDesc(String email);
  long countByLastLoginAtBetween(LocalDateTime start, LocalDateTime end);

  List<User> findByRole(String role);
}
