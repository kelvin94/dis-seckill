package com.jyl.authapi.authapi.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@NoArgsConstructor
public class  RoleName {
    @Id
    @GeneratedValue
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private String role_name;

    @OneToMany(fetch = FetchType.LAZY,
            mappedBy = "roleName",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private String invitionCode;
}
