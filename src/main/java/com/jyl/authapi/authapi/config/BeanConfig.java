package com.jyl.authapi.authapi.config;

import com.jyl.authapi.authapi.Security.JwtAuthenticationFilter;
import com.jyl.authapi.authapi.Service.InvitationCodeService;
import com.jyl.authapi.authapi.Service.RoleService;
import com.jyl.authapi.authapi.Service.TokenService;
import com.jyl.authapi.authapi.Service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class BeanConfig {


    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public TokenService tokenService() {
        return new TokenService();
    }

    @Bean
    public InvitationCodeService invitationCodeService() {
        return new InvitationCodeService();
    }

    @Bean
    public RoleService roleService() {
        return new RoleService();
    }

    @Bean
    public UserService userService() { return new UserService(); }
}
