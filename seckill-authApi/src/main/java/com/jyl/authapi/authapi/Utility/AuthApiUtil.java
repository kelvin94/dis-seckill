package com.jyl.authapi.authapi.Utility;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jyl.authapi.authapi.resource.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;

public class AuthApiUtil {
    private static final Logger logger = LoggerFactory.getLogger(AuthApiUtil.class);

    static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    static SecureRandom rnd = new SecureRandom();

    public static final String ROLE_ADMIN = "admin";

    public static final String ROLE_FAMILY = "family";

    public static final String ROLE_FRIEND = "friend";

    public static final String ROLE_OUTSIDER = "outsider";

    public static final String TOKEN_TYPE = "Bearer";

    // JWT token defaults
    public static final String TOKEN_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";

    public static final int randomInvitationCodeLength = 120;

    public static String convertToJson(Object obj) {
        ObjectMapper Obj = new ObjectMapper();
        String jsonStr = "";
        try {
            jsonStr = Obj.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            logger.error("Convert obj to json string error: " +e.getMessage());
        }
        return jsonStr;
    }

    public static String generateRandomString( int len ){
        StringBuilder sb = new StringBuilder( len );
        for( int i = 0; i < len; i++ )
            sb.append( AB.charAt( rnd.nextInt(AB.length()) ) );
        return sb.toString();
    }
}
