package com.jyl.authapi.authapi.repository;

import com.jyl.authapi.authapi.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByInvitionCode(String code);


}
