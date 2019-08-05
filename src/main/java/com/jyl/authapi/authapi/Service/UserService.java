package com.jyl.authapi.authapi.Service;

import com.jyl.authapi.authapi.model.User;
import com.jyl.authapi.authapi.repository.RoleRepository;
import com.jyl.authapi.authapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;

/*

Main purpose of UserService:
    - CRUD action on user
 */

@Service
public class UserService {
//    @Autowired
//    private UserRepository userRepository;
//    @Autowired
//    private RoleRepository roleRepository;
//    @Autowired
//    private BCryptPasswordEncoder bCryptPasswordEncoder;
//
//    public void save(User user) {
//        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
//        user.setRoles(new HashSet<>(roleRepository.findAll()));
//    }
//    public User findByUsername(String username) {
//        return userRepository.findByUsername(username);
//    }
//
//    public String entryPoint(AuthParameter parameter) {
//        String response = "entryPoint - UserService";
//        return response;
//    }
}
