package com.jyl.authapi.authapi.Controller;

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
    static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    static SecureRandom rnd = new SecureRandom();
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

//    @PreAuthorize("hashRole('admin')")
//    @GetMapping("/testingAuthorization")
//    public String testingAuthorization() {
//        return "shit it works";
//    }

    @PostMapping("/token/verify")
    public ResponseEntity<?> testingDB(@Valid @RequestBody TokenVerificationReq tokenVerificationReq) {
        String token = tokenVerificationReq.getToken();
        boolean isValidToken = tokenProvider.validateToken(token);
        if(isValidToken) {
            String roleName = tokenProvider.getUserRoleNameFromJWT(token);
            return new ResponseEntity(new ApiResponse(true, roleName), HttpStatus.OK);
        }
        return new ResponseEntity(new ApiResponse(false, "no rolename found"), HttpStatus.UNAUTHORIZED);
    }

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsernameOrEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        Collection<? extends GrantedAuthority> authority = authentication.getAuthorities();
        if(authority == null || authority.isEmpty()) return new ResponseEntity(new ApiResponse(false, "No role is found"), HttpStatus.NOT_FOUND);
        String jwt = tokenProvider.generateToken(authentication, authority);
        return ResponseEntity.ok(new JwtAuthenticationResponse(jwt));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        if(userRepository.existsByUsername(signUpRequest.getUsername())) {
            return new ResponseEntity(new ApiResponse(false, "Username is already taken!"),
                    HttpStatus.BAD_REQUEST);
        }

        if(userRepository.existsByEmail(signUpRequest.getEmail())) {
            return new ResponseEntity(new ApiResponse(false, "Email Address already in use!"),
                    HttpStatus.BAD_REQUEST);
        }
        String code = signUpRequest.getInvitationCode();
        /*
             检查验证码。。。
         */
        Optional<InvitationCode> findRoleByCode = invitationCodeRepository.findByCode(code);
        Role role = findRoleByCode.get().getRole();
//        logger.debug("##### 根據code找到了role "+role);

        if( role != null) {
            // Creating user's account
            User user = new User(signUpRequest.getName(), signUpRequest.getUsername(),
                    signUpRequest.getEmail(), signUpRequest.getPassword());
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setRole(role);
            logger.debug("#####finishing setting roles.");

            User result = userRepository.save(user);
            logger.debug("#####finishing saving user. ");

            Optional<InvitationCode> dbCode = invitationCodeRepository.findByCode(code);
            if(dbCode.isPresent() || dbCode != null) {
                List<InvitationCode> arrCode = new ArrayList<InvitationCode>();
                arrCode.add(dbCode.get());
                role.setInvitationCodes(arrCode);
            } else {
                // DB 沒有這個驗證碼，讓他滾
                return new ResponseEntity(new ApiResponse(false, "code not found!"),
                        HttpStatus.BAD_REQUEST);
            }
            Role savedRoleResult = roleRepository.save(role);
            logger.debug("#####finishing updating role with new code. ");

            URI location = ServletUriComponentsBuilder
                    .fromCurrentContextPath().path("/api/users/{username}")
                    .buildAndExpand(result.getUsername()).toUri();

            return ResponseEntity.created(location).body(new ApiResponse(true, "User registered successfully"));
        }
        return new ResponseEntity(new ApiResponse(false, "cannot create new user!"),
                HttpStatus.BAD_REQUEST);
    }

    @DeleteMapping("/user/{userId}/{username}")
    public String deleteUser(@PathVariable("userId") Long user_dbId, @PathVariable("username") String username) {
        String response = null;
        if(user_dbId == null || username == null) {
            return new ResponseEntity(new ApiResponse(false, "empty input!"),
                    HttpStatus.BAD_REQUEST).toString();
        }

        if(!userRepository.existsByUsername(username) || !userRepository.existsById(user_dbId)) {
            logger.error("user not found, user id: " + user_dbId);
            return new ResponseEntity(new ApiResponse(false, "user not found!"),
                    HttpStatus.BAD_REQUEST).toString();
        }

        Optional<User> user = userRepository.findById(user_dbId);
        if(user.isPresent()) {
            userRepository.delete(user.get());
            return new ResponseEntity(new ApiResponse(true, "done delete!"),
                    HttpStatus.OK).toString();
        } else {
            logger.error("User not found! user id: " + user_dbId);
        }
        return response;
    }

    @DeleteMapping("/role/{roleType}")
    public ResponseEntity<?> deleteRole(@PathVariable("roleType") String roleType) {
        if(!roleRepository.existsByRoleName(roleType)) {
            return new ResponseEntity(new ApiResponse(false, " Role does not exist and get out of my server!"),
                    HttpStatus.BAD_REQUEST);
        }
        Role dbRole = roleRepository.findByRoleName(roleType);
        roleRepository.delete(dbRole);
        return ResponseEntity.status(201).body(new ApiResponse(true, "role is removed successfully"));

    }

    @PostMapping("/role")
    public ResponseEntity<?> createNewRole(@Valid @RequestBody NewRoleRequest requestParam) {
        Role role = new Role();
        role.setRoleName(requestParam.getRoleName());
        roleRepository.save(role);

        return ResponseEntity.status(201).body(new ApiResponse(true, "New role is generated successfully"));

    }

    @DeleteMapping("/code/{code}")
    public ResponseEntity<?> deleteCode(@PathVariable("code") String code) {
        if(!invitationCodeRepository.existsByCode(code)) {
            return new ResponseEntity(new ApiResponse(false, " Code does not exist and get out of my server!"),
                    HttpStatus.BAD_REQUEST);
        }


        Optional<InvitationCode> dbCode = invitationCodeRepository.findByCode(code);
        logger.debug("delete code end point, find the code in db first "+ dbCode.get());
        invitationCodeRepository.delete(dbCode.get());

        return ResponseEntity.status(201).body(new ApiResponse(true, "New code is generated successfully", code));
    }

    @PostMapping("/code")
    public ResponseEntity<?> createNewCode(@Valid @RequestBody NewCodeRequest requestParam) {
        String roleType = requestParam.getRoleType();
        if(!roleRepository.existsByRoleName(roleType)) {
            return new ResponseEntity(new ApiResponse(false, roleType + " role type does not exist and get out of my server!"),
                    HttpStatus.BAD_REQUEST);
        }
        String code = generateRandomString(128);
        logger.debug("ramdon 120 bits " + code);

        InvitationCode dbobjCode = new InvitationCode();
        dbobjCode.setCode(code);
        Role role = null;
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

        invitationCodeRepository.save(dbobjCode);
        return ResponseEntity.status(201).body(new ApiResponse(true, "New code is generated successfully", code));
    }

    private String generateRandomString( int len ){
        StringBuilder sb = new StringBuilder( len );
        for( int i = 0; i < len; i++ )
            sb.append( AB.charAt( rnd.nextInt(AB.length()) ) );
        return sb.toString();
    }
}
