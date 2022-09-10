package io.pp.arcade.domain.admin.dto.delete;

import lombok.Builder;
import lombok.Getter;
import org.springframework.web.bind.annotation.GetMapping;

@Getter
@Builder
public class PChangeDeleteDto {
    private Integer pChangeId;
}
