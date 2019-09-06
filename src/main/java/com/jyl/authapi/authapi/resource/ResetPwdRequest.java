package com.jyl.authapi.authapi.resource;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
public class ResetPwdRequest {
    @NotBlank(message = "email cannt be blank")
    private String email;
    @NotBlank(message = "oldPwd cannt be blank")
    private String oldPwd;
    @NotBlank(message = "newPwd cannt be blank")
    private String newPwd;
}
