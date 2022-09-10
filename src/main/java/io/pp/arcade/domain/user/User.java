package io.pp.arcade.domain.user;

import io.pp.arcade.global.type.RoleType;
import io.pp.arcade.global.util.BaseTimeEntity;
import io.pp.arcade.global.type.RacketType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Entity
@Getter
@NoArgsConstructor
public class User extends BaseTimeEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @Setter
    @Column(name = "intra_id")
    private String intraId;

    @Setter
    @Column(name = "e_mail")
    private String eMail;

    @Setter
    @Column(name = "image_uri")
    private String imageUri;

    @Setter
    @Column(name = "racket_type")
    private RacketType racketType;

    @Setter
    @NotNull
    @Column(name = "status_message")
    private String statusMessage;

    @Setter
    @NotNull
    @Column(name = "ppp")
    private Integer ppp;

    @NotNull
    @Setter
    @Column(name = "role_type")
    private RoleType roleType;

    @Builder
    public User(String intraId, String eMail, String imageUri, RacketType racketType, String statusMessage, Integer ppp, RoleType roleType) {
        this.intraId = intraId;
        this.eMail = eMail;
        this.imageUri = imageUri;
        this.racketType = racketType;
        this.statusMessage = statusMessage;
        this.roleType = roleType;
        this.ppp = ppp;
    }
}
