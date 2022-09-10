package io.pp.arcade.domain.feedback.dto;

import io.pp.arcade.global.type.FeedbackType;
import lombok.Getter;

@Getter
public class FeedbackFindRequestDto {
    private FeedbackType category;
    private Boolean isSolved;
}
