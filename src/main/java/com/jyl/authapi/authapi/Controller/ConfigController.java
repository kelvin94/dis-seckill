package com.jyl.authapi.authapi.Controller;

import com.jyl.authapi.authapi.Service.SecurityService;
import com.jyl.authapi.authapi.Service.UserRoleInfoService;
import com.jyl.authapi.authapi.Service.UserService;
import com.jyl.authapi.authapi.inputParameter.AuthParameter;
import com.jyl.authapi.authapi.validator.UserValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RestController;

/*
    Note: ConfigController only handles bean creation, add validator to inputParams
 */
//@Configuration
public class ConfigController {
    private static final Logger logger = LogManager.getLogger(ConfigController.class);

    /*
        TODO: add validator
         */
//    @Autowired
//    private UserValidator userValidator;
//
//    /*
//    TODO: add initBinder to inputParameter
//     */
//    @InitBinder
//    public void authParamDataBinding(WebDataBinder binder) {
//        logger.debug("ConfigController pass request body to validator");
//        binder.addValidators(userValidator);
//    }




//    @Bean
//    public BCryptPasswordEncoder bCryptPasswordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//
//    @Bean
//    public SecurityService securityService() {
//        return new SecurityService();
//    }
//
//    @Bean
//    public AuthParameter authParameter() { return new AuthParameter();}
}
