package com.jyl.authapi.authapi.Service;

import com.jyl.authapi.authapi.model.Role;
import com.jyl.authapi.authapi.repository.RoleRepository;
import com.jyl.authapi.authapi.resource.ApiResponse;
import com.jyl.authapi.authapi.resource.NewRoleRequest;
import com.jyl.authapi.authapi.test.service.CodeServiceTest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class RoleService {

    private  final static Logger logger = LogManager.getLogger(RoleService.class);

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public ApiResponse createNewRole(NewRoleRequest requestParam) throws DataAccessException{
        String result = "";
        if(roleRepository.existsByRoleName(requestParam.getRoleName())) {
            return new ApiResponse(false, " role already exists.", HttpStatus.BAD_REQUEST);
        }
        Role role = new Role();
        role.setRoleName(requestParam.getRoleName());
        Role dbResult = null;
        try {
            dbResult = roleRepository.save(role);
        } catch (DataAccessException e) {
            logger.error("RoleService createNewRole() error: " + e.getMessage());
            throw e;
        }
        if(dbResult != null) return new ApiResponse(true, "new Role is created", HttpStatus.CREATED);
        return new ApiResponse(false, "New role is not created", HttpStatus.BAD_REQUEST);
    }

    public ApiResponse deleteRole(String roleType) throws DataAccessException, IllegalArgumentException{
        if(!roleRepository.existsByRoleName(roleType)) {
            return new ApiResponse(false, " Role does not exist and get out of my server!",
                    HttpStatus.NOT_FOUND);
        }
        Role dbRole = new Role();
        try {
            dbRole = roleRepository.findByRoleName(roleType);
            roleRepository.delete(dbRole);
        } catch (DataAccessException e) {
            logger.error("RoleService deleteRole() error: " + e.getMessage());
            throw e;
        }  catch (IllegalArgumentException e) {
            logger.error("RoleService deleteRole() error: " + e.getMessage());
            throw e;
        }
        return new ApiResponse(true, "role is removed successfully", HttpStatus.OK);
    }

}
