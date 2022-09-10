package io.pp.arcade.domain.admin.dto;

import io.pp.arcade.global.type.RoleType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Builder
public class AdminCheckerDto implements Serializable {
    private RoleType roleType;
    private String intraId;
}
