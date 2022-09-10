package io.pp.arcade.domain.admin.dto.update;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FeedbackUpdateDto {
    private Integer feedbackId;
    private Boolean isSolved;
}
