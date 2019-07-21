package com.jyl.authapi.authapi.Controller;

import com.jyl.authapi.authapi.Service.SecurityService;
import com.jyl.authapi.authapi.validator.UserValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RestController;

/*
    Note: ConfigController only handles bean creation, add validator to inputParams
 */
@RestController
public class ConfigController {
    /*
        TODO: add validator
         */
    @Autowired
    private UserValidator userValidator;

    /*
    TODO: add initBinder to inputParameter
     */
    @InitBinder
    public void authParamDataBinding(WebDataBinder binder) {
        binder.addValidators(userValidator);

    }




    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityService securityService() {
        return new SecurityService();
    }

}
