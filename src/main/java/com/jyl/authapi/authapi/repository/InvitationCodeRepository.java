package com.jyl.authapi.authapi.repository;

import com.jyl.authapi.authapi.model.InvitationCode;
import com.jyl.authapi.authapi.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InvitationCodeRepository extends JpaRepository<InvitationCode, Long> {
    InvitationCode findByCode(String code);
    Boolean existsByCode(String code);

    // Using JQL with parameters:
//    @Query("select u from invitation_code u where code = :code")
//    InvitationCode findByCodeReturnCode(@Param("code") String code);
}
