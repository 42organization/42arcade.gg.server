package io.pp.arcade.global.util;

import io.pp.arcade.domain.noti.NotiService;
import io.pp.arcade.domain.noti.dto.NotiAddDto;
import io.pp.arcade.domain.noti.dto.NotiCanceledDto;
import io.pp.arcade.domain.noti.dto.NotiCanceledTypeDto;
import io.pp.arcade.domain.slot.dto.SlotDto;
import io.pp.arcade.domain.user.dto.UserDto;
import io.pp.arcade.global.type.GameType;
import io.pp.arcade.global.type.NotiType;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import java.time.LocalDateTime;

@Component
@AllArgsConstructor
public class NotiGenerater {
    private final NotiService notiService;

    public void addMatchNotisBySlot(SlotDto slot) throws MessagingException {
        Integer maxSlotHeadCount = GameType.SINGLE.equals(slot.getType()) ? 2 : 4;
        Boolean isMatched = slot.getHeadCount().equals(maxSlotHeadCount) ? true : false;
        Boolean isImminent = isMatched && slot.getTime().isBefore(LocalDateTime.now().plusMinutes(5)) ? true : false;
        if (isImminent == true) {
            addNoti(slot.getTeam1().getUser1(), slot, NotiType.IMMINENT);
            addNoti(slot.getTeam1().getUser2(), slot, NotiType.IMMINENT);
            addNoti(slot.getTeam2().getUser1(), slot, NotiType.IMMINENT);
            addNoti(slot.getTeam2().getUser2(), slot, NotiType.IMMINENT);
        } else if (isMatched == true) {
            addNoti(slot.getTeam1().getUser1(), slot, NotiType.MATCHED);
            addNoti(slot.getTeam1().getUser2(), slot, NotiType.MATCHED);
            addNoti(slot.getTeam2().getUser1(), slot, NotiType.MATCHED);
            addNoti(slot.getTeam2().getUser2(), slot, NotiType.MATCHED);
        }
    }

    public void addCancelNotisBySlot(NotiCanceledTypeDto canceledTypeDto) throws MessagingException {
        SlotDto slot = canceledTypeDto.getSlotDto();
        NotiType notiType = canceledTypeDto.getNotiType();
        addNoti(slot.getTeam1().getUser1(), slot, notiType);
        addNoti(slot.getTeam1().getUser2(), slot, notiType);
        addNoti(slot.getTeam2().getUser1(), slot, notiType);
        addNoti(slot.getTeam2().getUser2(), slot, notiType);
    }

    private void addNoti(UserDto user, SlotDto slot, NotiType type) throws MessagingException {
        if (user != null) {
            NotiAddDto notiAddDto = NotiAddDto.builder()
                    .user(user)
                    .slot(slot)
                    .type(type)
                    .build();
            notiService.addNoti(notiAddDto);
        }
    }
}
