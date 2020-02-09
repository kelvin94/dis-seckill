package com.jyl.authapi.authapi.resource;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
public class NewCodeRequest {
    @NotBlank( message = "role type cannot be blank")
    private String roleType;
}
