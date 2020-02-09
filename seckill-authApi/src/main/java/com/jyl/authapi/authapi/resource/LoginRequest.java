package com.jyl.authapi.authapi.resource;

import javax.validation.constraints.NotBlank;

/*
Input parameters that are expected when outsider makes login request
 */
public class LoginRequest {
    @NotBlank(message = "username or email cannot be blank")
    private String usernameOrEmail;

    @NotBlank(message = "password cannot be blank")
    private String password;

    public String getUsernameOrEmail() {
        return usernameOrEmail;
    }

    public void setUsernameOrEmail(String usernameOrEmail) {
        this.usernameOrEmail = usernameOrEmail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
