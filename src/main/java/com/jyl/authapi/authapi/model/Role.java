package com.jyl.authapi.authapi.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Data
@Entity
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ROLE_ID")
    private Long id;

//    @Enumerated(EnumType.STRING)
//    @NaturalId
//    @Column(length = 60)
//    private RoleName name;
//    @OneToOne(mappedBy = "role",
//            fetch = FetchType.LAZY,
//            optional = true,
//            cascade = CascadeType.ALL)
//    @JoinColumn(name = "ROLENAME_ID")
//    private RoleName roleName;
    @OneToMany(fetch = FetchType.LAZY,
                mappedBy = "role",
            cascade = CascadeType.ALL)
    private Set<User> user;

    @Column(name = "role_name")
    private String roleName;

    @Getter
    @Setter
    @OneToMany(fetch = FetchType.LAZY,
            mappedBy = "role",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<InvitationCode> invitationCodes = new ArrayList<InvitationCode>();



    public Role() {

    }

//    public Role(RoleName roleName) {
//        this.roleName = roleName;
//    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }



//    public RoleName getRoleName() {
//        return roleName;
//    }

//    public void setRoleName(RoleName roleName) {
//        this.roleName = roleName;
//    }
}
