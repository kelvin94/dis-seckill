package com.jyl.authapi.authapi.model;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Fetch;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="ROLE_ID", nullable=false)
    private Role role;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private User user;

    @Override
    public String toString() {
        return "InvitationCode{" +
                "code='" + code + '\'' +
                '}';
    }
}
