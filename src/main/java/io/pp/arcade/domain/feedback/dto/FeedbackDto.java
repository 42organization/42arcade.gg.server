package io.pp.arcade.domain.feedback.dto;

import io.pp.arcade.domain.feedback.Feedback;
import io.pp.arcade.domain.user.dto.UserDto;
import io.pp.arcade.global.type.FeedbackType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FeedbackDto {
    private Integer id;
    private UserDto user;
    private FeedbackType category;
    private String content;
    private Boolean isSolved;

    public static FeedbackDto from(Feedback feedback) {
        return FeedbackDto.builder()
                .id(feedback.getId())
                .user(UserDto.from(feedback.getUser()))
                .category(feedback.getCategory())
                .content(feedback.getContent())
                .isSolved(feedback.getIsSolved())
                .build();
    }

    @Override
    public String toString() {
        return "FeedbackDto{" +
                "id=" + id +
                ", user=" + user +
                ", category=" + category +
                ", content='" + content + '\'' +
                ", isSolved=" + isSolved +
                '}';
    }
}
