package com.jyl.authapi.authapi.repository;

import com.jyl.authapi.authapi.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
}
