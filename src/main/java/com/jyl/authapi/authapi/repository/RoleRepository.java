package com.jyl.authapi.authapi.repository;

import com.jyl.authapi.authapi.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
//    Optional<Role> findBy(String code);
    Role findByRoleName(String roleName);
    Optional<Role> findByInvitationCodes(String code);
    Boolean existsByRoleName(String roleName);
}
