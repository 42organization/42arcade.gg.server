package io.pp.arcade.domain.game;

import io.pp.arcade.TestInitiator;
import io.pp.arcade.domain.game.dto.GameAddDto;
import io.pp.arcade.domain.game.dto.GameFindDto;
import io.pp.arcade.domain.game.dto.GameModifyStatusDto;
import io.pp.arcade.domain.game.dto.GameResultPageDto;
import io.pp.arcade.domain.slot.Slot;
import io.pp.arcade.domain.slot.SlotRepository;
import io.pp.arcade.domain.slot.dto.SlotDto;
import io.pp.arcade.domain.team.Team;
import io.pp.arcade.domain.team.TeamRepository;
import io.pp.arcade.domain.user.User;
import io.pp.arcade.domain.user.UserRepository;
import io.pp.arcade.global.exception.BusinessException;
import io.pp.arcade.global.type.GameType;
import io.pp.arcade.global.type.StatusType;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import javax.transaction.Transactional;

import java.util.Collections;

@SpringBootTest
class GameServiceTest {
    @Autowired
    GameRepository gameRepository;

    @Autowired
    GameService gameService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TeamRepository teamRepository;

    @Autowired
    SlotRepository slotRepository;

    @Autowired
    TestInitiator initiator;

    Slot slot;
    Team team1;
    User user1;
    User user2;
    Team team2;
    User user3;
    User user4;

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
    }

    @Test
    @Transactional
    void addGame() {
        //given
        slot.setType(GameType.SINGLE);
        GameAddDto addDto = GameAddDto.builder()
                .slotDto(SlotDto.from(slot))
                .build();

        //when
        gameService.addGame(addDto);

        //then
        Assertions.assertThat(gameRepository.findAll().get(0).getTeam1()).isEqualTo(team1);
        Assertions.assertThat(gameRepository.findAll().get(0).getTeam2()).isEqualTo(team2);
    }

    @Test
    @Transactional
    void modifyGameStatus() {
        //given
        slot.setType(GameType.SINGLE);

        Game game = gameRepository.save(Game.builder()
                .team1(team1)
                .team2(team2)
                .slot(slot)
                .type(slot.getType())
                .time(slot.getTime())
                .status(StatusType.LIVE)
                .season(1) //season 추가
                .build());

        GameModifyStatusDto modifyDto = GameModifyStatusDto.builder()
                .gameId(game.getId())
                .status(StatusType.END)
                .build();
        //when
        gameService.modifyGameStatus(modifyDto);
        Game game1 = gameRepository.findById(game.getId()).orElseThrow(() -> new BusinessException("E0001"));

        //then
        Assertions.assertThat(game1.getStatus()).isEqualTo(StatusType.END);
    }

    @Test
    @Transactional
    void findGamesAfterId() {
        slot.setType(GameType.SINGLE);

        Game game = gameRepository.save(Game.builder()
                .team1(team1)
                .team2(team2)
                .slot(slot)
                .type(slot.getType())
                .time(slot.getTime())
                .status(StatusType.END)
                .season(1) //season 추가
                .build());

        Game game2 = gameRepository.save(Game.builder()
                .team1(team1)
                .team2(team2)
                .slot(slot)
                .type(slot.getType())
                .time(slot.getTime())
                .status(StatusType.END)
                .season(1) //season 추가
                .build());

        Pageable pageable = PageRequest.of(0, 100);

        GameFindDto findDto = GameFindDto.builder()
                .id(game2.getId())
                .status(StatusType.END)
                .pageable(pageable)
                .build();

        //when
        GameResultPageDto pageDto = gameService.findGamesAfterId(findDto);

        //then
        Assertions.assertThat(pageDto.getGameList()).isNotEqualTo(Collections.EMPTY_LIST);
    }
}