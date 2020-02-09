package com.jyl.authapi.authapi.test.service;

import com.jyl.authapi.authapi.Controller.AuthController;
import com.jyl.authapi.authapi.Service.TokenService;
import com.jyl.authapi.authapi.resource.ApiResponse;
import com.jyl.authapi.authapi.resource.JwtTokenProvider;
//import com.jyl.authapi.authapi.resource.TokenVerificationReq;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TokenServiceTest {
    private  final static Logger logger = LogManager.getLogger(TokenServiceTest.class);

    @Mock
    private JwtTokenProvider tokenProvider;

    @InjectMocks
    private TokenService tokenService;

    private String token = "sometoken";
//    private TokenVerificationReq req = new TokenVerificationReq(token);


    @Test
    public void testVerifyToken_happycase() throws Exception {
        when(tokenProvider.validateToken(token)).thenReturn(true);
        when(tokenProvider.getUserRoleNameFromJWT(token)).thenReturn("friends");
        String expect = "{\"success\":true,\"message\":\"friends\",\"code\":null,\"httpStatus\":\"OK\"}";
        String actual = tokenService.verifyToken(token);
        assertNotNull(actual);
        assertFalse(actual.isEmpty());
        assertTrue(actual.equalsIgnoreCase(expect));
    }

    @Test
    public void testVerifyToken_sadCase() throws Exception {
        when(tokenProvider.validateToken(token)).thenReturn(false);
        String actual = tokenService.verifyToken(token);
        assertTrue(actual.isEmpty());
    }
}
