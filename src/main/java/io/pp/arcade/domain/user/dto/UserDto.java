package io.pp.arcade.domain.user.dto;

import io.pp.arcade.domain.user.User;
import io.pp.arcade.global.type.RacketType;
import io.pp.arcade.global.type.RoleType;
import lombok.Builder;
import lombok.Getter;

import java.util.Objects;

@Getter
@Builder
public class UserDto {
    private Integer id;
    private String intraId;
    private String eMail;
    private String imageUri;
    private RacketType racketType;
    private String statusMessage;
    private RoleType roleType;
    private Integer ppp;

    public static UserDto from(User user) {
        UserDto userDto;
        if (user == null) {
            userDto = null;
        } else {
            userDto = UserDto.builder()
                    .id(user.getId())
                    .intraId(user.getIntraId())
                    .eMail(user.getEMail())
                    .imageUri(user.getImageUri())
                    .racketType(user.getRacketType())
                    .statusMessage(user.getStatusMessage())
                    .roleType(user.getRoleType())
                    .ppp(user.getPpp())
                    .build();
        }
        return userDto;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDto userDto = (UserDto) o;
        return Objects.equals(id, userDto.id) && Objects.equals(intraId, userDto.intraId) && Objects.equals(imageUri, userDto.imageUri) && racketType == userDto.racketType && Objects.equals(statusMessage, userDto.statusMessage) && Objects.equals(ppp, userDto.ppp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, intraId, imageUri, racketType, statusMessage, ppp);
    }

    @Override
    public String toString() {
        return "UserDto{" +
                "id=" + id +
                ", intraId='" + intraId + '\'' +
                ", eMail='" + eMail + '\'' +
                ", imageUri='" + imageUri + '\'' +
                ", racketType=" + racketType +
                ", statusMessage='" + statusMessage + '\'' +
                ", roleType=" + roleType +
                ", ppp=" + ppp +
                '}';
    }
}
