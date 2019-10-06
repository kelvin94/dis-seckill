package com.jyl.authapi.authapi;

import com.jyl.authapi.authapi.model.Role;
import com.jyl.authapi.authapi.model.User;
import com.jyl.authapi.authapi.repository.RoleRepository;
import com.jyl.authapi.authapi.repository.UserRepository;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class AuthapiApplication {

        @Autowired
        private UserRepository userRepository;
        @Autowired
        private RoleRepository roleRepository;
        @Autowired
        private PasswordEncoder passwordEncoder;

        public static void main(String[] args) {

            SpringApplication.run(AuthapiApplication.class, args);

        }
    @Value("${app.adminpassword}")
    private String pwd;
    @Value("${app.adminusername}")
    private String username;
    @Value("${app.pedestrianusername}")
    private String pedestrianUsername;
    @Value("${app.pedestrianpassword}")
    private String pedestrianPassword;
    @Bean
    InitializingBean sendDatabase() {
        return () -> {
            System.out.println("###${app.adminusername} " + username);
            System.out.println("###${app.adminpassword} " + pwd);
            System.out.println("###${app.pedestrianUsername} " + pedestrianUsername);
            System.out.println("###${app.pedestrianpassword} " + pedestrianPassword);

            Role role = null;
            if(!roleRepository.existsByRoleName("admin")) {
                role = roleRepository.save(new Role("admin"));
                if(!userRepository.existsByUsername(username))
                    userRepository.save(new User(username, username, "kelvinlingz@gmail.com", passwordEncoder.encode(pwd), role));
            }

            role = null;
            if(!roleRepository.existsByRoleName("pedestrian")) {
                role = roleRepository.save(new Role("pedestrian"));
                if(!userRepository.existsByUsername(pedestrianUsername))
                    userRepository.save(new User(pedestrianUsername, pedestrianUsername, "pedestrian@gmail.com", passwordEncoder.encode(pedestrianPassword), role));
            }
        };
    }

}
