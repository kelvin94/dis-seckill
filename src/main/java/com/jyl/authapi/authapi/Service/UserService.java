package com.jyl.authapi.authapi.Service;

import com.jyl.authapi.authapi.Utility.AuthApiUtil;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private  final static Logger logger = LogManager.getLogger(UserService.class);

    private final AuthenticationManager authenticationManager;

    private final UserRepository userRepository;

    private final InvitationCodeRepository invitationCodeRepository;

    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtTokenProvider tokenProvider;

    public UserService(AuthenticationManager authenticationManager, UserRepository userRepository,
                       InvitationCodeRepository invitationCodeRepository, RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder, JwtTokenProvider tokenProvider) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.invitationCodeRepository = invitationCodeRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    public JwtAuthenticationResponse signIn(LoginRequest loginRequest) throws AuthenticationException{
        Authentication authentication = null;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsernameOrEmail(),
                            loginRequest.getPassword()
                    )
            );

        } catch (RuntimeException e) {
            logger.error("fail to authenticate user : " + loginRequest.getUsernameOrEmail());
            logger.error("error: " + e.getMessage());
            throw e;
        }
        if(authentication != null) {
            SecurityContextHolder.getContext().setAuthentication(authentication);
            Collection<? extends GrantedAuthority> authority = authentication.getAuthorities();
            if(authority == null || authority.isEmpty())
                return new JwtAuthenticationResponse(null, null, HttpStatus.NOT_FOUND);
            String jwt = tokenProvider.generateToken(authentication, authority);
            return new JwtAuthenticationResponse(AuthApiUtil.TOKEN_TYPE, jwt, HttpStatus.OK);
        }
        return new JwtAuthenticationResponse(null, null, HttpStatus.BAD_REQUEST);

    }

    public ApiResponse registerUser(SignUpRequest signUpRequest) {
        if(userRepository.existsByUsername(signUpRequest.getUsername())) {
            return new ApiResponse(false, "Username is already taken!",
                    HttpStatus.BAD_REQUEST);
        }

        if(userRepository.existsByEmail(signUpRequest.getEmail())) {
            return new ApiResponse(false, "Email Address already in use!",
                    HttpStatus.BAD_REQUEST);
        }
        String code = signUpRequest.getInvitationCode();
        /*
             检查验证码。。。
         */
        Optional<InvitationCode> findRoleByCode = invitationCodeRepository.findByCode(code);
        Role role = new Role();

        if(findRoleByCode.isPresent())
           role = findRoleByCode.get().getRole();

        if( role != null) {
            // Creating user's account
            User user = new User(signUpRequest.getName(), signUpRequest.getUsername(),
                    signUpRequest.getEmail(), signUpRequest.getPassword());
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setRole(role);
            try {
                userRepository.save(user);

            } catch (DataAccessException e) {
                logger.error("UserService registerUser() username: " + user.getUsername()+ " userRepo.save() error: "+ e.getMessage());
                throw e;
            }

            Optional<InvitationCode> dbCode = invitationCodeRepository.findByCode(code);
            if( dbCode.isPresent() ) {
                List<InvitationCode> arrCode = new ArrayList<>();
                arrCode.add(dbCode.get());
                role.setInvitationCodes(arrCode);
            } else {
                // DB 沒有這個驗證碼，讓他滾
                return new ApiResponse(false, "code not found!",
                        HttpStatus.NOT_FOUND);
            }
            try {
                roleRepository.save(role);

            } catch (DataAccessException e ) {
                logger.error("UserService registerUser() username: " + user.getUsername() + " saving role.save(), error: " + e.getMessage());
                throw e;
            }
            return new ApiResponse(true, "User registered successfully", HttpStatus.CREATED);
        }
        return new ApiResponse(false, "cannot create new user!",
                HttpStatus.BAD_REQUEST);
    }

    public ApiResponse deleteUser(Long user_dbId, String username) {
        ApiResponse response = new ApiResponse();
        if(user_dbId == null || username == null) {
            return new ApiResponse(false, "empty input!",
                    HttpStatus.BAD_REQUEST);
        }

        if(!userRepository.existsByUsername(username) || !userRepository.existsById(user_dbId)) {
            logger.error("user not found, user id: " + user_dbId);
            return new ApiResponse(false, "user not found!",
                    HttpStatus.BAD_REQUEST);
        }

        Optional<User> user = userRepository.findById(user_dbId);
        if(user.isPresent()) {
            try {
                userRepository.delete(user.get());

            } catch (DataAccessException e) {
                logger.error("UserService deleteUser(), deleting user_dbId: "+ user_dbId);
                logger.error("UserService deleteUser(), deleting username: "+ username);
                throw e;
            }
            return new ApiResponse(true, "done delete!",
                    HttpStatus.OK);
        } else {
            logger.error("User not found! user id: " + user_dbId);
        }
        return response;
    }

    public ApiResponse resetPwd(ResetPwdRequest request) {
        ApiResponse response = new ApiResponse();
        if(!userRepository.existsByEmail(request.getEmail())) {
            logger.error("user not found, user Email: " + request.getEmail());
            return new ApiResponse(false, "user not found!",
                    HttpStatus.NOT_FOUND);
        }
        Optional<User> dbUser = userRepository.findByEmail(request.getEmail());
        if(dbUser.isPresent()) {

            boolean isMatchedPwd = passwordEncoder.matches(request.getOldPwd(), dbUser.get().getPassword());

            if(isMatchedPwd) {
                User user = dbUser.get();

                user.setPassword(passwordEncoder.encode(request.getNewPwd()));
                try {
                    userRepository.save(user);

                } catch (DataAccessException e) {
                    logger.error("UserService resetPwd(), user: "+ request.getEmail() + " processing threw exception, error" + e.getMessage());
                    throw e;
                }
                response.setSuccess(true);
                response.setHttpStatus(HttpStatus.OK);
                response.setMessage("Reset done");
            } else {
                logger.error("UserService resetPassword(), user: "+ dbUser.get().getEmail() + " old password does not match db");
                return new ApiResponse(false, "user: "+ dbUser.get().getEmail() + " old password does not match db",
                        HttpStatus.BAD_REQUEST);
            }
        }
        return response;

    }
}
