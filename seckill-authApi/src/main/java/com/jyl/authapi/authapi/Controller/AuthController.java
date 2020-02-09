package com.jyl.authapi.authapi.Controller;

import com.jyl.authapi.authapi.Service.InvitationCodeService;
import com.jyl.authapi.authapi.Service.RoleService;
import com.jyl.authapi.authapi.Service.TokenService;
import com.jyl.authapi.authapi.Service.UserService;
import com.jyl.authapi.authapi.Utility.AuthApiUtil;
import com.jyl.authapi.authapi.resource.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private  final static Logger logger = LogManager.getLogger(AuthController.class);

    private final TokenService tokenService;

    private final InvitationCodeService invitationCodeService;

    private final RoleService roleService;

    private final UserService userService;

    public AuthController(TokenService tokenService, InvitationCodeService invitationCodeService,
                          RoleService roleService, UserService userService) {
        this.tokenService = tokenService;
        this.invitationCodeService = invitationCodeService;
        this.roleService = roleService;
        this.userService = userService;
    }

    @GetMapping("/token/verify")
    public String verifyToken(@RequestHeader("Authorization") String token, HttpServletRequest request) {
        if(token.isEmpty() || !token.substring(0, 6).equalsIgnoreCase("Bearer")) {
            logger.error("Empty token passed in, incoming request source ip: "+ request.getRemoteAddr() + ". and x-forwarded-for http header, list of proxy ips: "+ request.getHeader("X-FORWARDED-FOR"));
            return AuthApiUtil.convertToJson(new ApiResponse(false, "Empty token", HttpStatus.BAD_REQUEST));
        }
        String result = tokenService.verifyToken(token.substring(6));
        if(result.isEmpty()) {
            logger.error("Invalid token passed in: " + token + " incoming request source ip: "+ request.getRemoteAddr() + ". and x-forwarded-for http header, list of proxy ips: "+ request.getHeader("X-FORWARDED-FOR"));
            return AuthApiUtil.convertToJson(new ApiResponse(false, "Invalid token", HttpStatus.BAD_REQUEST));
        }
        return result;
    }

    @PostMapping("/signin")
    public ResponseEntity<String> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        ResponseEntity<String> result = null;
        try {
            JwtAuthenticationResponse authenticateResult = userService.signIn(loginRequest);
            if(authenticateResult != null && authenticateResult.getAccessToken() != null && !authenticateResult.getAccessToken().isEmpty())
                result = new ResponseEntity<>(AuthApiUtil.convertToJson(authenticateResult), authenticateResult.getHttpStatus());

        } catch (Exception e) {
            result = new ResponseEntity<>(AuthApiUtil.convertToJson(new ApiResponse(false, "Unable to authenticate user, error: "+e.getMessage(), HttpStatus.UNAUTHORIZED)), HttpStatus.UNAUTHORIZED);
        }
        return result;
    }

    @PostMapping("/signup")
    public ResponseEntity<String> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        try {
            ApiResponse result = userService.registerUser(signUpRequest);
            return (result.getSuccess() ? new ResponseEntity<>(AuthApiUtil.convertToJson(result), HttpStatus.OK) : new ResponseEntity<>(AuthApiUtil.convertToJson(result), HttpStatus.BAD_REQUEST));
        } catch(Exception e ) {
            return new ResponseEntity<>(AuthApiUtil.convertToJson(new ApiResponse(false, "Exception thrown - check the logs", HttpStatus.INTERNAL_SERVER_ERROR)), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/user")
    public String resetPwd(@Valid @RequestBody ResetPwdRequest request) {
        ApiResponse result = new ApiResponse();

        try {
            result = userService.resetPwd(request);
        } catch (Exception e) {
            logger.error("reset pwd service error: " + e.getMessage());
        }
        return AuthApiUtil.convertToJson(result);
    }

    @DeleteMapping("/user/{userId}/{username}")
    public String deleteUser(@PathVariable("userId") Long user_dbId, @PathVariable("username") String username) {
        try {
            ApiResponse result = userService.deleteUser(user_dbId, username);
            return AuthApiUtil.convertToJson(result);

        } catch(Exception e) {
            return AuthApiUtil.convertToJson(new ApiResponse(false, "Delete user service exception thrown - check the logs, error: "+ e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR));
        }

    }

    @DeleteMapping("/role/{roleType}")
    public String deleteRole(@PathVariable("roleType") String roleType) {
        try {
            ApiResponse result = roleService.deleteRole(roleType);
            return AuthApiUtil.convertToJson(result);
        } catch (DataAccessException e) {
            return AuthApiUtil.convertToJson(new ApiResponse(false, "Delete role exception thrown - check the logs, error: "+ e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    @PostMapping("/role")
    public String createNewRole(@Valid @RequestBody NewRoleRequest requestParam) {
        try {
            ApiResponse result = roleService.createNewRole(requestParam);
            return AuthApiUtil.convertToJson(result);
        } catch (DataAccessException e) {
            return AuthApiUtil.convertToJson(new ApiResponse(false, "Delete role exception thrown - check the logs, error: "+ e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR));
        }
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
        }
        if(!result.isEmpty()) {
            return AuthApiUtil.convertToJson(new ApiResponse(true, "new code is created", HttpStatus.CREATED, result));
        }
        return AuthApiUtil.convertToJson(new ApiResponse(false, "new code is unable to be created", HttpStatus.BAD_REQUEST, result));
    }


}
