package com.pitang.booster_c1m1.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pitang.booster_c1m1.domain.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  boolean existsByEmail(String email);
  boolean existsByEmailAndIdNot(String email, Long id);
  Page<User> findByNameContainingIgnoreCase(String name, Pageable pageable);
}