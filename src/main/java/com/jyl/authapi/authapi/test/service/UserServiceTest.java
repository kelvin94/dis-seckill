package com.jyl.authapi.authapi.test.service;

import com.jyl.authapi.authapi.Service.UserService;
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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.swing.text.html.Option;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(SecurityContextHolder.class)
@PowerMockIgnore({"javax.management.*", "javax.script.*"})
public class UserServiceTest {
    private  final static Logger logger = LogManager.getLogger(UserServiceTest.class);
    private LoginRequest loginRequest = new LoginRequest();
    private SignUpRequest signUpRequest = new SignUpRequest();
    private String jwtToken = "somejwttoken";
    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;
    @Mock
    private InvitationCodeRepository invitationCodeRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Before
    public void setUp() {
        loginRequest.setUsernameOrEmail("jyljyl");
        loginRequest.setPassword("passswor");
        signUpRequest.setUsername("jyljyl");
        signUpRequest.setEmail("jyljyl@email.com");
        signUpRequest.setPassword("passswor");
        signUpRequest.setInvitationCode("someinvitationcode");
    }

    @Test
    public void testSignIn_happyCase() {
        PowerMockito.mockStatic(SecurityContextHolder.class);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken("foo", "bar", AuthorityUtils
                .commaSeparatedStringToAuthorityList("admin"));
        when(authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsernameOrEmail(),
                        loginRequest.getPassword()
                )
        )).thenReturn(authentication);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);

        when(SecurityContextHolder.getContext()).thenReturn(securityContext);
        doNothing().when(securityContext).setAuthentication(authentication);
//        doNothing().when(SecurityContextHolder.getContext()).setAuthentication(authentication);
        when(tokenProvider.generateToken(authentication,  AuthorityUtils
                .commaSeparatedStringToAuthorityList("admin"))).thenReturn(jwtToken);
        JwtAuthenticationResponse actual = userService.signIn(loginRequest);
        assertNotNull(actual);
        assertTrue(actual.getAccessToken().equalsIgnoreCase(jwtToken));

        Collection<? extends GrantedAuthority> expectedAuthority = AuthorityUtils
                .commaSeparatedStringToAuthorityList("admin");
        verify(tokenProvider, times(1)).generateToken(authentication, expectedAuthority);
    }

    @Test
    public void testSignIn_givenAccountHasNoAuthority() {
        PowerMockito.mockStatic(SecurityContextHolder.class);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken("foo", "bar", AuthorityUtils
                .commaSeparatedStringToAuthorityList(null));
        when(authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsernameOrEmail(),
                        loginRequest.getPassword()
                )
        )).thenReturn(authentication);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);

        when(SecurityContextHolder.getContext()).thenReturn(securityContext);
        doNothing().when(securityContext).setAuthentication(authentication);
        when(tokenProvider.generateToken(authentication,  AuthorityUtils
                .commaSeparatedStringToAuthorityList(null))).thenReturn(null);
        JwtAuthenticationResponse actual = userService.signIn(loginRequest);
        assertNotNull(actual);
        assertNull(actual.getAccessToken());
        assertTrue(actual.getHttpStatus().toString().equalsIgnoreCase(HttpStatus.NOT_FOUND.toString()));
    }

    @Test(expected = RuntimeException.class)
    public void testSignIn_PasswordWrong() {
        PowerMockito.mockStatic(SecurityContextHolder.class);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken("foo", "bar", AuthorityUtils
                .commaSeparatedStringToAuthorityList("admin"));
        when(authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsernameOrEmail(),
                        loginRequest.getPassword()
                )
        )).thenThrow(new RuntimeException());
        userService.signIn(loginRequest);
    }

    @Test
    public void testRegisterUser_happy() {
        String username = signUpRequest.getUsername();
        String pwd = signUpRequest.getPassword();
        String email = signUpRequest.getEmail();
        String code = signUpRequest.getInvitationCode();
        Role role = new Role();
        role.setRoleName(AuthApiUtil.ROLE_ADMIN);
        InvitationCode mockedCode = new InvitationCode();
        mockedCode.setCode(code);
        mockedCode.setRole(role);
        role.setInvitationCodes(Arrays.asList(mockedCode));

        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.existsByEmail(email)).thenReturn(false);
        Optional<InvitationCode> findRoleByCode = Optional.of(mockedCode);
        when(invitationCodeRepository.findByCode(code)).thenReturn(findRoleByCode);
        User user = new User(signUpRequest.getName(), signUpRequest.getUsername(),
                signUpRequest.getEmail(), pwd);
        String encodedPwd = "someencodedstring";
        when(passwordEncoder.encode(pwd)).thenReturn(encodedPwd);
        user.setPassword(encodedPwd);
        user.setRole(role);


        when(userRepository.save(user)).thenReturn(user);

        ApiResponse actual = userService.registerUser(signUpRequest);
        assertNotNull(actual);
        assertTrue(actual.getSuccess());
        assertTrue(actual.getHttpStatus().toString().equalsIgnoreCase(HttpStatus.CREATED.toString()));
        verify(passwordEncoder, times(1)).encode(pwd);
        verify(userRepository, times(1)).save(user);
        verify(roleRepository, times(1)).save(role);
    }

    @Test
    public void testRegisterUser_NoInvitationCodeMatched() {
        String username = signUpRequest.getUsername();
        String pwd = signUpRequest.getPassword();
        String email = signUpRequest.getEmail();
        String code = signUpRequest.getInvitationCode();
        Role role = new Role();
        role.setRoleName(AuthApiUtil.ROLE_ADMIN);
        InvitationCode mockedCode = new InvitationCode();
        mockedCode.setCode(code);
        mockedCode.setRole(role);
        role.setInvitationCodes(Arrays.asList(mockedCode));
        User user = new User(signUpRequest.getName(), signUpRequest.getUsername(),
                signUpRequest.getEmail(), pwd);
        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.existsByEmail(email)).thenReturn(false);
        Optional<InvitationCode> findRoleByCode = Optional.<InvitationCode>empty();
        when(invitationCodeRepository.findByCode(code)).thenReturn(findRoleByCode);

        ApiResponse actual = userService.registerUser(signUpRequest);
        assertNotNull(actual);
        assertFalse(actual.getSuccess());
        assertTrue(actual.getHttpStatus().toString().equalsIgnoreCase(HttpStatus.NOT_FOUND.toString()));
        verify(passwordEncoder, times(1)).encode(pwd);
        verify(userRepository, times(0)).save(user);
    }

    @Test(expected = DataAccessException.class)
    public void testRegisterUser_FailedToSaveUser() {
        String username = signUpRequest.getUsername();
        String pwd = signUpRequest.getPassword();
        String email = signUpRequest.getEmail();
        String code = signUpRequest.getInvitationCode();
        Role role = new Role();
        role.setRoleName(AuthApiUtil.ROLE_ADMIN);
        InvitationCode mockedCode = new InvitationCode();
        mockedCode.setCode(code);
        mockedCode.setRole(role);
        role.setInvitationCodes(Arrays.asList(mockedCode));

        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.existsByEmail(email)).thenReturn(false);
        Optional<InvitationCode> findRoleByCode = Optional.of(mockedCode);
        when(invitationCodeRepository.findByCode(code)).thenReturn(findRoleByCode);
        User user = new User(signUpRequest.getName(), signUpRequest.getUsername(),
                signUpRequest.getEmail(), pwd);
        String encodedPwd = "someencodedstring";
        when(passwordEncoder.encode(pwd)).thenReturn(encodedPwd);
        user.setPassword(encodedPwd);
        user.setRole(role);
        when(userRepository.save(user)).thenThrow(new DataAccessException("...") {});

        ApiResponse actual = userService.registerUser(signUpRequest);
    }

    @Test
    public void testRegisterUser_SameUsernameExists() {
        String username = signUpRequest.getUsername();
        String pwd = signUpRequest.getPassword();
        String email = signUpRequest.getEmail();
        String code = signUpRequest.getInvitationCode();
        when(userRepository.existsByUsername(username)).thenReturn(true);
        ApiResponse actual = userService.registerUser(signUpRequest);
        assertNotNull(actual);
        assertFalse(actual.getSuccess());
        assertTrue(actual.getHttpStatus().toString().equalsIgnoreCase(HttpStatus.BAD_REQUEST.toString()));
    }

    @Test
    public void testRegisterUser_SameEmailExists() {
        String username = signUpRequest.getUsername();
        String pwd = signUpRequest.getPassword();
        String email = signUpRequest.getEmail();
        String code = signUpRequest.getInvitationCode();
        when(userRepository.existsByEmail(email)).thenReturn(true);
        ApiResponse actual = userService.registerUser(signUpRequest);
        assertNotNull(actual);
        assertFalse(actual.getSuccess());
        assertTrue(actual.getHttpStatus().toString().equalsIgnoreCase(HttpStatus.BAD_REQUEST.toString()));
    }

}
