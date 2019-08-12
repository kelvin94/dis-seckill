package com.jyl.authapi.authapi.model;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@NoArgsConstructor
@Entity(name = "InvitationCode")
@Table(name = "InvitationCode")
@Data
public class InvitationCode {


    @Id
    @GeneratedValue
    private Long id;


    @Column(name = "code")
    private String code;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name="ROLE_ID", nullable=false)
    private Role role;

    @Override
    public String toString() {
        return "InvitationCode{" +
                "code='" + code + '\'' +
                '}';
    }
}
