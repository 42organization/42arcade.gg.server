package io.pp.arcade.domain.pchange;

import io.pp.arcade.TestInitiator;
import io.pp.arcade.domain.game.Game;
import io.pp.arcade.domain.game.GameRepository;
import io.pp.arcade.domain.pchange.dto.PChangeAddDto;
import io.pp.arcade.domain.slot.Slot;
import io.pp.arcade.domain.slot.SlotRepository;
import io.pp.arcade.domain.team.Team;
import io.pp.arcade.domain.team.TeamRepository;
import io.pp.arcade.domain.user.User;
import io.pp.arcade.domain.user.UserRepository;
import io.pp.arcade.global.type.GameType;
import io.pp.arcade.global.type.StatusType;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.transaction.Transactional;
import java.time.LocalDateTime;

@SpringBootTest
class PChangeServiceTest {
    @Autowired
    PChangeRepository pChangeRepository;
    @Autowired
    PChangeService pChangeService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    GameRepository gameRepository;
    @Autowired
    TeamRepository teamRepository;
    @Autowired
    SlotRepository slotRepository;
    @Autowired
    TestInitiator initiator;

    User user1;
    User user2;
    User user3;
    User user4;
    Team team1;
    Team team2;
    Slot slot;
    Game game;

    @BeforeEach
    void init() {
        initiator.letsgo();
        user1 = initiator.users[0];
        user2 = initiator.users[1];
        user3 = initiator.users[2];
        user4 = initiator.users[3];

        slot = initiator.slots[0];

        team1 = slot.getTeam1();
        team2 = slot.getTeam2();

        game = gameRepository.save(Game.builder()
                .slot(slot)
                .team1(team1)
                .team2(team2)
                .time(slot.getTime())
                .type(GameType.SINGLE)
                .season(1)
                .status(StatusType.LIVE)
                .build());
    }

    @Test
    @Transactional
    void addPChange() {
        //given
        PChangeAddDto addDto = PChangeAddDto.builder()
                .gameId(game.getId())
                .userId(user1.getId())
                .pppChange(333)
                .pppResult(user1.getPpp() + 333)
                .build();

        //when
        pChangeService.addPChange(addDto);

        //then
        PChange pChange = pChangeRepository.findAll().get(0);
        Assertions.assertThat(pChange.getGame().getId()).isEqualTo(game.getId());
        Assertions.assertThat(pChange.getUser().getId()).isEqualTo(user1.getId());
        Assertions.assertThat(pChange.getPppChange()).isEqualTo(333);
        Assertions.assertThat(pChange.getPppResult()).isEqualTo(user1.getPpp() + 333);
    }

    @Test
    @Transactional
    void findPChange() {
    }
}