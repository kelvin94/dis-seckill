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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.security.SecureRandom;
import java.util.*;

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

    @GetMapping("/testingDB")
    public String testingDB() {
        String result = null;

        InvitationCode code = new InvitationCode();
        code.setCode("ramdon bits 1");


        InvitationCode code2 = new InvitationCode();
        code2.setCode("ramdon bits 2");



        Role role = new Role();
//        code.setRole(role);
//        code2.setRole(role);


        List<InvitationCode> invitationCodeList = new ArrayList<InvitationCode>();
//        invitationCodeRepository.save(code);
//        invitationCodeRepository.save(code2);
        invitationCodeList.addAll(Arrays.asList(code, code2));
        role.setInvitationCodes(invitationCodeList);
        role.setRoleName("testing");

        roleRepository.save(role);

        logger.debug("##### showing rolename field in role: " + role.getRoleName());
        logger.debug("\n");
        logger.debug("##### showing invitation code from rolename " + role.getInvitationCodes());
        return result;
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

        String jwt = tokenProvider.generateToken(authentication);
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
        /*
        TODO: 如果request body里面有invitation code，先睇头8bit，分辨是哪种用户
                有哪几种用户：
                    - 11111111: admin
                    - 11111110: 屋企人
                    - 11111101: 朋友
                    - else: 叁唔识七人
         */

        // Creating user's account
        User user = new User(signUpRequest.getName(), signUpRequest.getUsername(),
                signUpRequest.getEmail(), signUpRequest.getPassword());

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        Role userRole = roleRepository.findById(null)
                .orElseThrow(() -> new AppException("User Role not set."));

        user.setRoles(Collections.singleton(userRole));
        logger.debug("#####finishing setting roles.");
        User result = userRepository.save(user);
        logger.debug("#####finishing saving user.");

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/api/users/{username}")
                .buildAndExpand(result.getUsername()).toUri();

        return ResponseEntity.created(location).body(new ApiResponse(true, "User registered successfully"));
    }

    @DeleteMapping("/role/{roleType}")
    public ResponseEntity<?> deleteRole(@PathVariable("roleType") String roleType) {
//        if(roleType.equalsIgnoreCase(AuthApiUtil.ROLE_ADMIN)) {
//            return new ResponseEntity(new ApiResponse(false, " Don't try this."),
//                    HttpStatus.UNAUTHORIZED);
//        }
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


        InvitationCode dbCode = invitationCodeRepository.findByCode(code);
        logger.debug("delete code end point, find the code in db first "+ dbCode);
        invitationCodeRepository.delete(dbCode);

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
