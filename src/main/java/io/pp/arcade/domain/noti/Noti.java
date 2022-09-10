package io.pp.arcade.domain.noti;

import io.pp.arcade.domain.slot.Slot;
import io.pp.arcade.domain.user.User;
import io.pp.arcade.global.type.NotiType;
import io.pp.arcade.global.util.BaseTimeEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Getter
@Entity
@NoArgsConstructor
public class Noti extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "intra_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "slot_id")
    private Slot slot;

    @NotNull
    @Column(name = "noti_type")
    private NotiType type;

    @Column(name = "message")
    private String message;

    @Setter
    @NotNull
    @Column(name = "is_checked")
    private Boolean isChecked;

    @Builder
    public Noti(User user, Slot slot, NotiType type, String message, Boolean isChecked) {
        this.user = user;
        this.slot = slot;
        this.type = type;
        this.message = message;
        this.isChecked = isChecked;
    }

    public void update(User user, Slot slot, NotiType type, String message, Boolean isChecked) {
        this.user = user;
        this.slot = slot;
        this.type = type;
        this.message = message;
        this.isChecked = isChecked;
    }
}
