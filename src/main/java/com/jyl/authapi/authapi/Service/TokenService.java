package com.jyl.authapi.authapi.Service;

import com.jyl.authapi.authapi.Utility.AuthApiUtil;
import com.jyl.authapi.authapi.resource.ApiResponse;
import com.jyl.authapi.authapi.resource.JwtTokenProvider;
import com.jyl.authapi.authapi.resource.TokenVerificationReq;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class TokenService {
    private  final static Logger logger = LogManager.getLogger(TokenService.class);

    private final JwtTokenProvider tokenProvider;

    public TokenService(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

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
