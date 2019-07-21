package com.jyl.authapi.authapi.repository;

import com.jyl.authapi.authapi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
}
