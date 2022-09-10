package io.pp.arcade.domain.currentmatch;

import io.pp.arcade.TestInitiator;
import io.pp.arcade.domain.currentmatch.dto.*;
import io.pp.arcade.domain.game.Game;
import io.pp.arcade.domain.game.GameRepository;
import io.pp.arcade.domain.game.dto.GameDto;
import io.pp.arcade.domain.slot.Slot;
import io.pp.arcade.domain.slot.SlotRepository;
import io.pp.arcade.domain.slot.dto.SlotDto;
import io.pp.arcade.domain.team.Team;
import io.pp.arcade.domain.team.TeamRepository;
import io.pp.arcade.domain.user.User;
import io.pp.arcade.domain.user.UserRepository;
import io.pp.arcade.domain.user.dto.UserDto;
import io.pp.arcade.global.exception.BusinessException;
import io.pp.arcade.global.type.GameType;
import io.pp.arcade.global.type.StatusType;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.transaction.Transactional;

import java.util.Collections;

@SpringBootTest
class CurrentMatchServiceTest {
    @Autowired
    CurrentMatchRepository currentMatchRepository;
    @Autowired
    CurrentMatchService currentMatchService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    GameRepository gameRepository;
    @Autowired
    SlotRepository slotRepository;
    @Autowired
    TeamRepository teamRepository;
    @Autowired
    TestInitiator initiator;

    User user1;
    User user2;
    User user3;
    User user4;
    Slot slot;
    Team team1;
    Team team2;
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
                .team1(team1)
                .team2(team2)
                .slot(slot)
                .season(1)
                .status(StatusType.LIVE)
                .time(slot.getTime())
                .type(GameType.SINGLE)
                .build());
    }

    @Test
    @Transactional
    void addCurrentMatch() {
        CurrentMatchAddDto addDto = CurrentMatchAddDto.builder()
                .slot(SlotDto.from(slot))
                .userId(user1.getId())
                .build();
        currentMatchService.addCurrentMatch(addDto);
        CurrentMatch match = currentMatchRepository.findByUser(user1).orElseThrow(() -> new BusinessException("E0001"));

        Assertions.assertThat(match.getUser()).isEqualTo(user1);
    }

    @Test
    @Transactional
    void modifyCurrentMatch() {
        CurrentMatch currentMatch = currentMatchRepository.save(CurrentMatch.builder()
                .game(game)
                .slot(slot)
                .user(user1)
                .build());
        CurrentMatchModifyDto modifyDto = CurrentMatchModifyDto.builder()
                .gameDto(GameDto.from(game))
                .userId(user1.getId())
                .isMatched(true)
                .matchImminent(false)
                .build();

        currentMatchService.modifyCurrentMatch(modifyDto);
        currentMatch = currentMatchRepository.findById(currentMatch.getId()).orElseThrow(() -> new BusinessException("E0001"));
        Assertions.assertThat(currentMatch.getIsMatched()).isEqualTo(true);
        Assertions.assertThat(currentMatch.getMatchImminent()).isEqualTo(false);
    }

    @Test
    @Transactional
    void saveGameInCurrentMatch() {
        CurrentMatch currentMatch = currentMatchRepository.save(CurrentMatch.builder()
                .slot(slot)
                .user(user1)
                .build());
        CurrentMatchSaveGameDto saveGameDto = CurrentMatchSaveGameDto.builder()
                .gameId(game.getId())
                .userId(user1.getId())
                .build();

        currentMatchService.saveGameInCurrentMatch(saveGameDto);
        currentMatch = currentMatchRepository.findById(currentMatch.getId()).orElseThrow(() -> new BusinessException("E0001"));
        Assertions.assertThat(currentMatch.getGame()).isEqualTo(game);
    }

    @Test
    @Transactional
    void findCurrentMatchByUserId() {
        CurrentMatch currentMatch = currentMatchRepository.save(CurrentMatch.builder()
                .slot(slot)
                .user(user1)
                .build());

        CurrentMatchDto dto1 = currentMatchService.findCurrentMatchByUser(UserDto.from(user1));
        CurrentMatchDto dto2 = currentMatchService.findCurrentMatchByUser(UserDto.from(user2));

        Assertions.assertThat(dto1).isNotEqualTo(null);
        Assertions.assertThat(dto2).isEqualTo(null);
    }

    @Test
    @Transactional
    void removeCurrentMatch() {
        CurrentMatch currentMatch = currentMatchRepository.save(CurrentMatch.builder()
                .slot(slot)
                .user(user1)
                .build());

        CurrentMatchRemoveDto removeDto = CurrentMatchRemoveDto.builder()
                .userId(user1.getId()).build();

        Assertions.assertThat(currentMatchRepository.findAll()).isNotEqualTo(Collections.EMPTY_LIST);
        currentMatchService.removeCurrentMatch(removeDto);
        Assertions.assertThat(currentMatchRepository.findAll()).isEqualTo(Collections.EMPTY_LIST);
    }
}