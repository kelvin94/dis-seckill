package com.jyl.authapi.authapi.model;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@Entity
@Table(name = "roles")
public class Role {
    @Getter
    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ROLE_ID")
    private Long id;

    @Getter
    @OneToMany(fetch = FetchType.LAZY,
                mappedBy = "role",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<User> user = new ArrayList<User>();
    @Getter
    @Setter
    @Column(name = "role_name")
    private String roleName;

    @Getter
    @OneToMany(fetch = FetchType.LAZY,
            mappedBy = "role",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<InvitationCode> invitationCodes = new ArrayList<InvitationCode>();

    public void setInvitationCodes(List<InvitationCode> invitationCodes) {
        this.invitationCodes.addAll(invitationCodes);
    }

    public void setUser(List<User> users) {
        this.user.addAll(users);
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

    @Override
    public String toString() {
        return "Role{" +
                "id=" + id +
                ", user=" + user +
                ", roleName='" + roleName + '\'' +
                ", invitationCodes=" + invitationCodes +
                '}';
    }


//    public RoleName getRoleName() {
//        return roleName;
//    }

//    public void setRoleName(RoleName roleName) {
//        this.roleName = roleName;
//    }
}
