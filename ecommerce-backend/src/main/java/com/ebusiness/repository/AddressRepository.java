package com.ebusiness.repository;

import com.ebusiness.entity.Address;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {
  List<Address> findByUserIdOrderByIsDefaultDescIdAsc(Long userId);

  Optional<Address> findByIdAndUserId(Long id, Long userId);

  void deleteByIdAndUserId(Long id, Long userId);
}
