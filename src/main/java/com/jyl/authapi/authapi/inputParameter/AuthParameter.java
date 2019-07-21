package com.jyl.authapi.authapi.inputParameter;

import javax.validation.constraints.NotNull;

public class AuthParameter {

    @NotNull(message = "username cannot be null")
    private String username;
    @NotNull(message = "pwd cannot be null")
    private String password;

}
