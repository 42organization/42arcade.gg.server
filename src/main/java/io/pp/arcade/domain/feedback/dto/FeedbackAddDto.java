package io.pp.arcade.domain.feedback.dto;

import io.pp.arcade.global.type.FeedbackType;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
public class FeedbackAddDto {
    private Integer userId;
    private FeedbackType category;
    private String content;

    @Override
    public String toString() {
        return "FeedbackAddDto{" +
                "userId=" + userId +
                ", category='" + category + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
