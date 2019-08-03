package com.jyl.authapi.authapi.Service;

import com.jyl.authapi.authapi.model.Role;
import com.jyl.authapi.authapi.model.User;
import com.jyl.authapi.authapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
public class UserRoleInfoService {
    /*
    Main Purpose of UserRoleInfoService
        - CRUD on user's role

     */
//    @Autowired
//    private UserRepository userRepository;
//
//    @Transactional(readOnly = true)
//    public UserDetails loadUserByUsername(String username) {
//        User user = userRepository.findByUsername(username);
//        if( user == null) throw new UsernameNotFoundException(username);
//        Set<GrantedAuthority> grantedAuthorities = new HashSet<GrantedAuthority>();
//        for (Role role : user.getRoles()){
//            grantedAuthorities.add(new SimpleGrantedAuthority(role.getName()));
//        }
//        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), grantedAuthorities);
//
//    }
}
