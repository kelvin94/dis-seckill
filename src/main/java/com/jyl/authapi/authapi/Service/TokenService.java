package com.jyl.authapi.authapi.Service;


import com.jyl.authapi.authapi.Utility.AuthApiUtil;
import com.jyl.authapi.authapi.resource.ApiResponse;
import com.jyl.authapi.authapi.resource.JwtTokenProvider;
import com.jyl.authapi.authapi.resource.TokenVerificationReq;
import com.jyl.authapi.authapi.test.service.TokenServiceTest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;

public class TokenService {
    private  final static Logger logger = LogManager.getLogger(TokenService.class);

    @Autowired
    private JwtTokenProvider tokenProvider;

    public String verifyToken(TokenVerificationReq tokenVerificationReq) {
        String result = "";
        String token = tokenVerificationReq.getToken();
        boolean isValidToken = tokenProvider.validateToken(token);
        if(isValidToken) {
            String roleName = tokenProvider.getUserRoleNameFromJWT(token);
            result = AuthApiUtil.convertToJson(new ApiResponse(true, roleName, HttpStatus.OK));
        }
        return result;
    }

}
