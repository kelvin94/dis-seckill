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
        @Value("${app.adminpassword}")
        private String pwd;
        @Autowired
        private UserRepository userRepository;
        @Autowired
        private RoleRepository roleRepository;
        @Autowired
        private PasswordEncoder passwordEncoder;

        public static void main(String[] args) {

            SpringApplication.run(AuthapiApplication.class, args);
        }

    @Bean
    InitializingBean sendDatabase() {
        return () -> {
            Role role = null;
            if(!roleRepository.existsByRoleName("admin")) {
                role = roleRepository.save(new Role("admin"));
            }
            if(role != null) {
                System.out.println("### role "+ role);
                if(!userRepository.existsByUsername("ggininder"))
                    userRepository.save(new User("ggininder", "ggininder", "kelvinlingz@gmail.com", passwordEncoder.encode(pwd), role));

            }
        };
    }

}
