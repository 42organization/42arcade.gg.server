package io.pp.arcade.domain.user.dto;

import lombok.Builder;
import lombok.Getter;

import javax.persistence.Entity;
import java.util.List;

@Builder
@Getter
public class tempDto {
    List<String> datas;
}
