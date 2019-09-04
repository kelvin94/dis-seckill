package com.jyl.authapi.authapi.Controller;

import com.jyl.authapi.authapi.Service.InvitationCodeService;
import com.jyl.authapi.authapi.Service.RoleService;
import com.jyl.authapi.authapi.Service.TokenService;
import com.jyl.authapi.authapi.Service.UserService;
import com.jyl.authapi.authapi.Utility.AuthApiUtil;
import com.jyl.authapi.authapi.exception.AppException;
import com.jyl.authapi.authapi.model.InvitationCode;
import com.jyl.authapi.authapi.model.Role;
import com.jyl.authapi.authapi.model.User;
import com.jyl.authapi.authapi.repository.InvitationCodeRepository;
import com.jyl.authapi.authapi.repository.RoleRepository;
import com.jyl.authapi.authapi.repository.UserRepository;
import com.jyl.authapi.authapi.resource.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.persistence.EntityNotFoundException;
import javax.swing.text.html.Option;
import javax.validation.Valid;
import java.net.URI;
import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private  final static Logger logger = LogManager.getLogger(AuthController.class);

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    InvitationCodeRepository invitationCodeRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JwtTokenProvider tokenProvider;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private InvitationCodeService invitationCodeService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private UserService userService;

    @PostMapping("/token/verify")
    public String verifyToken(@Valid @RequestBody TokenVerificationReq tokenVerificationReq) {
        String result = tokenService.verifyToken(tokenVerificationReq);
        if(result.isEmpty()) {
            return AuthApiUtil.convertToJson(new ApiResponse(false, "Invalid token"));
        }
        return result;
    }

    @PostMapping("/signin")
    public String authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        String result = "";
        try {
            JwtAuthenticationResponse authenticateResult = userService.signIn(loginRequest);
            if(authenticateResult != null && authenticateResult.getAccessToken() != null && !authenticateResult.getAccessToken().isEmpty())
                result = AuthApiUtil.convertToJson(authenticateResult);

        } catch (Exception e) {
            result = AuthApiUtil.convertToJson(new ApiResponse(false, "Unable to authenticate user, error: "+e.getMessage(), HttpStatus.BAD_REQUEST));
        }
        return result;
    }

    @PostMapping("/signup")
    public String registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        ApiResponse result = new ApiResponse();

        try {
            result = userService.registerUser(signUpRequest);
        } catch(Exception e ){
            return AuthApiUtil.convertToJson(new ApiResponse(false, "Exception thrown - check the logs", HttpStatus.INTERNAL_SERVER_ERROR));
        }
        return AuthApiUtil.convertToJson(result);
    }

    @DeleteMapping("/user/{userId}/{username}")
    public String deleteUser(@PathVariable("userId") Long user_dbId, @PathVariable("username") String username) {
        ApiResponse result = new ApiResponse();
        try {
            result = userService.deleteUser(user_dbId, username);

        } catch(Exception e) {
            return AuthApiUtil.convertToJson(new ApiResponse(false, "Exception thrown - check the logs", HttpStatus.INTERNAL_SERVER_ERROR));
        }

        return AuthApiUtil.convertToJson(result);
    }

    @DeleteMapping("/role/{roleType}")
    public String deleteRole(@PathVariable("roleType") String roleType) {
        ApiResponse result = new ApiResponse();
        try {
            result = roleService.deleteRole(roleType);

        } catch (DataAccessException e) {
            result = new ApiResponse(false, "Failed to delete role, error: "+ e.getMessage());
            throw e;
        }
        return AuthApiUtil.convertToJson(result);
    }

    @PostMapping("/role")
    public String createNewRole(@Valid @RequestBody NewRoleRequest requestParam) {
        ApiResponse result = new ApiResponse();
        try {
            result = roleService.createNewRole(requestParam);
        } catch (DataAccessException e) {
            throw e;
        }
        return AuthApiUtil.convertToJson(result);
    }

    @DeleteMapping("/code/{code}")
    public String deleteCode(@PathVariable("code") String code) {
        Boolean result = false;
        if(code != null && !code.isEmpty() && code.length() > 6) {
            result = invitationCodeService.deleteCode(code);
        }

        if(result) {
            return AuthApiUtil.convertToJson(new ApiResponse(true, "code is deleted", HttpStatus.OK ));
        }
        return AuthApiUtil.convertToJson(new ApiResponse(false, "code failed to be deleted", HttpStatus.BAD_REQUEST, code));
    }

    @PostMapping("/code")
    public String createNewCode(@Valid @RequestBody NewCodeRequest requestParam) throws Exception {
        String result = "";
        try {
            // if success, result is String form of invitation code
            // if fail, result is a ApiReponse() instance with error message
            result = invitationCodeService.createCode(requestParam);
        } catch (DataAccessException e ) {
            logger.error("InvitationCodeService unable to save a code: "+ result);
            logger.error(e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("InvitationCodeService unable to save a code: "+ result);
            logger.error(e.getMessage());
            throw e;
        }
        if(!result.isEmpty() && result != null) {
            return AuthApiUtil.convertToJson(new ApiResponse(true, "new code is created", HttpStatus.CREATED, result));
        }
        return AuthApiUtil.convertToJson(new ApiResponse(false, "new code is unable to be created", HttpStatus.BAD_REQUEST, result));
    }


}
