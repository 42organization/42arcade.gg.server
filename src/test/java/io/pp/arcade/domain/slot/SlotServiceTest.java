package io.pp.arcade.domain.slot;

import io.pp.arcade.TestInitiator;
import io.pp.arcade.domain.slot.dto.*;
import io.pp.arcade.domain.team.Team;
import io.pp.arcade.domain.team.TeamRepository;
import io.pp.arcade.domain.user.User;
import io.pp.arcade.domain.user.UserRepository;

import io.pp.arcade.global.exception.BusinessException;
import io.pp.arcade.global.type.GameType;
import io.pp.arcade.global.type.SlotStatusType;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@SpringBootTest
class SlotServiceTest {

    @Autowired
    SlotRepository slotRepository;
    @Autowired
    TeamRepository teamRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    SlotService slotService;
    @Autowired
    TestInitiator testInitiator;

    Slot slot;
    User user1;
    User user2;
    Slot[] slots;
    User[] users;
    Team[] teams;

    @BeforeEach
    void init() {
        testInitiator.letsgo();
        teams = testInitiator.teams;
        users = testInitiator.users;
        slots = testInitiator.slots;
    }

    @Test
    @Transactional
    void addSlot() throws InterruptedException {
        //given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime time = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), now.getHour(), now.getMinute(), now.getSecond());
        SlotAddDto dto = SlotAddDto.builder().tableId(1).time(time).build();

        //when
        slotService.addSlot(dto);
        Slot addedSlot = slotRepository.findByTime(time).orElseThrow();
        Team team1 = addedSlot.getTeam1();
        Team team2 = addedSlot.getTeam2();

        //then
        Assertions.assertThat(dto.getTime()).isEqualTo(addedSlot.getTime());
        Assertions.assertThat(team1).isNotEqualTo(null);
        Assertions.assertThat(team2).isNotEqualTo(null);
    }

    @Test
    @Transactional
    void addUserInSlot() {
        //given
        slot = slots[0];
        user1 = users[0];
        user2 = users[1];
        SlotAddUserDto dto = SlotAddUserDto.builder()
                .slotId(slot.getId())
                .type(GameType.SINGLE)
                .joinUserPpp(user1.getPpp())
                .build();

        //when
        slotService.addUserInSlot(dto);
        Slot s1 = slotRepository.findById(slot.getId()).orElseThrow(() -> new BusinessException("E0001"));

        //then
        Assertions.assertThat(s1.getHeadCount()).isEqualTo(1);
        Assertions.assertThat(s1.getGamePpp()).isEqualTo(user1.getPpp());
        Assertions.assertThat(s1.getType()).isEqualTo(GameType.SINGLE);

        //given
        SlotAddUserDto dto1 = SlotAddUserDto.builder()
                .slotId(slot.getId())
                .type(GameType.SINGLE)
                .joinUserPpp(user2.getPpp())
                .build();

        //when
        slotService.addUserInSlot(dto1);
        Slot s2 = slotRepository.findById(slot.getId()).orElseThrow(() -> new BusinessException("E0001"));

        //then
        Assertions.assertThat(s2.getHeadCount()).isEqualTo(2);
        Assertions.assertThat(s2.getGamePpp()).isEqualTo((user1.getPpp() + user2.getPpp()) / 2);
        Assertions.assertThat(s2.getType()).isEqualTo(GameType.SINGLE);
    }

    @Test
    @Transactional
    void removeUserInSlot() {
        // given
        Slot slot1 = slots[0];
        slot1.setHeadCount(2);
        slot1.setGamePpp(80);
        slot1.setType(GameType.SINGLE);
        SlotRemoveUserDto dto = SlotRemoveUserDto.builder()
                .slotId(slot1.getId())
                .exitUserPpp(60)
                .build();
        // when
        slotService.removeUserInSlot(dto);
        Slot removedUserSlot = slotRepository.findById(slot1.getId()).orElseThrow(() -> new BusinessException("E0001"));

        // then
        Assertions.assertThat(removedUserSlot.getGamePpp()).isEqualTo(100);
        Assertions.assertThat(removedUserSlot.getHeadCount()).isEqualTo(1);
        Assertions.assertThat(removedUserSlot.getType()).isEqualTo(GameType.SINGLE);

        // when
        slotService.removeUserInSlot(dto);
        Slot removedUserSlot2 = slotRepository.findById(slot1.getId()).orElseThrow(() -> new BusinessException("E0001"));

        // then
        Assertions.assertThat(removedUserSlot2.getGamePpp()).isEqualTo(null);
        Assertions.assertThat(removedUserSlot2.getHeadCount()).isEqualTo(0);
        Assertions.assertThat(removedUserSlot2.getType()).isEqualTo(null);
    }

    @Test
    @Transactional
    void findSlotById() {
        //when
        slot = slots[0];
        SlotDto slotDto = slotService.findSlotById(slot.getId());

        //then
        Assertions.assertThat(slotDto.getId()).isEqualTo(slot.getId());
    }

    @Test
    @Transactional
    void findSlotsStatus() {
        //given
        slots[0].setType(GameType.DOUBLE);
        slots[0].setHeadCount(3);
        slots[1].setType(GameType.SINGLE);
        slots[1].setHeadCount(2);
        slots[2].setType(GameType.SINGLE);
        slots[2].setHeadCount(1);
        user1 = users[0];

        //when
        SlotFindStatusDto dto = SlotFindStatusDto.builder()
                .type(GameType.SINGLE)
                .currentTime(LocalDateTime.of(1999,10,10,0,0,0))
                .userId(user1.getId())
                .build();
        List<SlotStatusDto> responseDtos = slotService.findSlotsStatus(dto);

        //then
        Integer openCount = 0;
        Integer closeCount = 0;
        for (SlotStatusDto resDto : responseDtos) {
            System.out.println(resDto.getStatus());
            if (resDto.getStatus().equals(SlotStatusType.OPEN.getCode().toLowerCase(Locale.ROOT))) {
                openCount++;
            } else if (resDto.getStatus().equals(SlotStatusType.CLOSE.getCode().toLowerCase(Locale.ROOT))) {
                closeCount++;
            }
        }
        Assertions.assertThat(openCount).isEqualTo(16);
        Assertions.assertThat(closeCount).isEqualTo(2);
    }
}