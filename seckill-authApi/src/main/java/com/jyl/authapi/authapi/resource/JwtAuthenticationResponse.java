package com.jyl.authapi.authapi.resource;

import lombok.Data;
import org.springframework.http.HttpStatus;
@Data
public class JwtAuthenticationResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    private HttpStatus httpStatus;

    public JwtAuthenticationResponse(String tokenType, String accessToken, HttpStatus httpStatus) {
        this.tokenType = tokenType;
        this.accessToken = accessToken;
        this.httpStatus = httpStatus;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
}
