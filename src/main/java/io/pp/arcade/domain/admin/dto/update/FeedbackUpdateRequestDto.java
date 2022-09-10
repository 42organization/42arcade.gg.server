package io.pp.arcade.domain.admin.dto.update;

import lombok.Getter;

@Getter
public class FeedbackUpdateRequestDto {
    private Integer feedbackId;
    private Boolean isSolved;
}
