package com.jyl.authapi.authapi.resource;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
public class NewRoleRequest {
    @NotNull(message = "roleName cannot be null")
    private String roleName;
}
