package com.jyl.authapi.authapi.Controller;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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

    @GetMapping("/testingDB")
    public String testingDB() {
        String result = null;
        SecureRandom random = new SecureRandom();
        byte bytes[] = new byte[15]; // 120 bits are converted to 16 bytes;
        random.nextBytes(bytes); // nextBytes(byte[] bytes) method is used to generate random bytes and places them into a user-supplied byte array.
        logger.debug("ramdon 120 bits " + bytes);
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
        role.setInvitionCode(invitationCodeList);
        role.setRoleName("testing");

        roleRepository.save(role);

        logger.debug("##### showing rolename field in role: " + role.getRoleName());
        logger.debug("\n");
        logger.debug("##### showing invitation code from rolename " + role.getInvitionCode());
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
}
