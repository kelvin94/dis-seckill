package com.jyl.authapi.authapi.resource;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TokenVerificationReq {
    private String token;
}
