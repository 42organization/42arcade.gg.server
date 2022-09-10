package io.pp.arcade.domain.feedback;

import io.pp.arcade.domain.user.User;
import io.pp.arcade.global.type.FeedbackType;
import io.pp.arcade.global.util.BaseTimeEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.bind.DefaultValue;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Entity
@Getter
@NoArgsConstructor
public class Feedback extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "intra_id")
    private User user;

    @NotNull
    @Column(name = "category")
    private FeedbackType category;

    @NotBlank
    @NotNull
    @Column(name = "content", length = 600)
    private String content;

    @Setter
    @NotNull
    @Column(name = "is_solved")
    private Boolean isSolved;

    @Builder
    public Feedback(User user, FeedbackType category, String content) {
        this.user = user;
        this.category = category;
        this.content = content;
        this.isSolved = false;
    }
}
