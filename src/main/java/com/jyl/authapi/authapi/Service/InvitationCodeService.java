package com.jyl.authapi.authapi.Service;

import com.jyl.authapi.authapi.Utility.AuthApiUtil;
import com.jyl.authapi.authapi.model.InvitationCode;
import com.jyl.authapi.authapi.model.Role;
import com.jyl.authapi.authapi.repository.InvitationCodeRepository;
import com.jyl.authapi.authapi.repository.RoleRepository;
import com.jyl.authapi.authapi.resource.ApiResponse;
import com.jyl.authapi.authapi.resource.NewCodeRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class InvitationCodeService {
    private  final static Logger logger = LogManager.getLogger(InvitationCodeService.class);

    private final InvitationCodeRepository invitationCodeRepository;

    private final RoleRepository roleRepository;

    public InvitationCodeService( InvitationCodeRepository invitationCodeRepository,
                                 RoleRepository roleRepository) {
        this.invitationCodeRepository = invitationCodeRepository;
        this.roleRepository = roleRepository;
    }

    public Boolean deleteCode(String code) {
        if(!invitationCodeRepository.existsByCode(code)) {
            return false;
        }
        Optional<InvitationCode> dbCode = invitationCodeRepository.findByCode(code);
        dbCode.ifPresent(invitationCodeRepository::delete);
        return true;
    }

    public String createCode(NewCodeRequest requestParam) {
        String roleType = requestParam.getRoleType();
        if(!roleRepository.existsByRoleName(roleType)) {
            return AuthApiUtil.convertToJson(new ApiResponse(false, roleType + " role type does not exist and get out of my server!",
                    HttpStatus.BAD_REQUEST));
        }
        String code = AuthApiUtil.generateRandomString(AuthApiUtil.randomInvitationCodeLength);
        logger.info("ramdon 120 bits " + code);

        InvitationCode dbobjCode = new InvitationCode();
        dbobjCode.setCode(code);
        Role role;
        switch(roleType.toLowerCase()) {
            case AuthApiUtil.ROLE_ADMIN:
                role = roleRepository.findByRoleName(AuthApiUtil.ROLE_ADMIN);
                dbobjCode.setRole(role);
                break;
            case AuthApiUtil.ROLE_FAMILY:
                role = roleRepository.findByRoleName(AuthApiUtil.ROLE_FAMILY);
                dbobjCode.setRole(role);
                break;
            case AuthApiUtil.ROLE_FRIEND:
                role = roleRepository.findByRoleName(AuthApiUtil.ROLE_FRIEND);
                dbobjCode.setRole(role);
                break;
            default:
                role = roleRepository.findByRoleName(AuthApiUtil.ROLE_OUTSIDER);
                dbobjCode.setRole(role);
                break;
        }
        InvitationCode dbResult = new InvitationCode();
        try {
            dbResult = invitationCodeRepository.save(dbobjCode);
        } catch (DataAccessException e ) {
            logger.error("InvitationCodeService unable to save a code: "+ code);
            logger.error(e.getMessage());
            throw e;
        }
        return dbResult.getCode();
    }
}
