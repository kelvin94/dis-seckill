package com.jyl.authapi.authapi.config;

import com.jyl.authapi.authapi.Security.JwtAuthenticationFilter;
import com.jyl.authapi.authapi.Service.*;
import com.jyl.authapi.authapi.repository.InvitationCodeRepository;
import com.jyl.authapi.authapi.repository.RoleRepository;
import com.jyl.authapi.authapi.repository.UserRepository;
import com.jyl.authapi.authapi.resource.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class BeanConfig {

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenProvider tokenProvider, CustomUserDetailsService customUserDetailsService) {
        return new JwtAuthenticationFilter(tokenProvider, customUserDetailsService);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

//    @Bean
    public TokenService tokenService(JwtTokenProvider tokenProvider) {
        return new TokenService(tokenProvider);
    }

    @Bean
    public InvitationCodeService invitationCodeService() {
        return new InvitationCodeService();
    }

//    @Bean
    public RoleService roleService(RoleRepository roleRepository) {
        return new RoleService(roleRepository);
    }

//    @Bean
    public UserService userService(AuthenticationManager authenticationManager, UserRepository userRepository,
                                   InvitationCodeRepository invitationCodeRepository, RoleRepository roleRepository,
                                   PasswordEncoder passwordEncoder, JwtTokenProvider tokenProvider) {
        return new UserService(authenticationManager, userRepository, invitationCodeRepository, roleRepository, passwordEncoder, tokenProvider);
    }
}
