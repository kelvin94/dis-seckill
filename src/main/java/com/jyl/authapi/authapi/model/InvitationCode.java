package com.jyl.authapi.authapi.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Data
@NoArgsConstructor
public class InvitationCode {
    @Id
    @GeneratedValue
    private Long id;

    private String invitationCode;
}
