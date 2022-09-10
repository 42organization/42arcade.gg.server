package io.pp.arcade.domain.feedback.dto;

import io.pp.arcade.global.type.FeedbackType;
import lombok.Getter;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

@Getter
@ToString
public class FeedbackRequestDto {
    private FeedbackType category;
    @Length(max = 600)
    private String content;
}
