package com.jyl.authapi.authapi;

import com.jyl.authapi.authapi.Service.UserService;
import com.jyl.authapi.authapi.model.InvitationCode;
import com.jyl.authapi.authapi.model.Role;
import com.jyl.authapi.authapi.model.User;
import com.jyl.authapi.authapi.repository.InvitationCodeRepository;
import com.jyl.authapi.authapi.repository.RoleRepository;
import com.jyl.authapi.authapi.repository.UserRepository;
import com.jyl.authapi.authapi.resource.LoginRequest;
import com.jyl.authapi.authapi.resource.SignUpRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.parameters.P;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.when;

//@RunWith(MockitoJUnitRunner.class)
@RunWith(PowerMockRunner.class)
@PowerMockIgnore( {"javax.script.*","javax.management.*", "org.w3c.dom.*", "org.apache.log4j.*", "org.xml.sax.*",   "javax.xml.*"})
public class AuthapiApplicationTests
{
    private String code = "some_invit_code";
    private Role expected_role = new Role("admin");
    private String username = "some name";
    private String pwd = "some pwd";
    private String email = "someemail";
    private SignUpRequest signUpRequest = new SignUpRequest();
    private LoginRequest loginRequest = new LoginRequest();
    private InvitationCode inviteCode = new InvitationCode();
    private Role role = new Role("admin");
    private User expectedUser = new User();
    @Mock
    private InvitationCodeRepository invitationCodeRepository;
    @InjectMocks
    private UserService userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
//    @Mock
//    private SecurityContextHolder sch;


    @Before
    public void before() {
        signUpRequest.setUsername(username);
        signUpRequest.setEmail(email);
        signUpRequest.setPassword(pwd);
        signUpRequest.setInvitationCode(code);

        loginRequest.setUsernameOrEmail(username);
        loginRequest.setPassword(pwd);

        inviteCode.setCode(code);
        inviteCode.setRole(role);

        expectedUser.setUsername(username);
        expectedUser.setPassword(pwd);
    }
    @Test
    public void registerTest() {
        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(invitationCodeRepository.findByCode(code)).thenReturn(Optional.of(inviteCode));
        when(userRepository.save(expectedUser)).thenReturn(expectedUser);
        when(invitationCodeRepository.findByCode(code)).thenReturn(Optional.of(inviteCode));
        when(passwordEncoder.encode(pwd)).thenReturn(anyString());
        when(roleRepository.save(role)).thenReturn(role);

        userService.registerUser(signUpRequest);

        verify(userRepository, times(1)).existsByUsername(username);
        verify(userRepository, times(1)).existsByEmail(email);
        verify(invitationCodeRepository, times(2)).findByCode(code);
        verify(userRepository, times(1)).save(any(User.class));
        verify(passwordEncoder, times(1)).encode(pwd);
        verify(roleRepository, times(1)).save(role);
    }


}