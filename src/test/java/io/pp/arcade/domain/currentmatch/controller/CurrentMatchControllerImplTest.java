package io.pp.arcade.domain.currentmatch.controller;

import io.pp.arcade.RestDocsConfiguration;
import io.pp.arcade.TestInitiator;
import io.pp.arcade.domain.currentmatch.CurrentMatch;
import io.pp.arcade.domain.currentmatch.CurrentMatchRepository;
import io.pp.arcade.domain.game.Game;
import io.pp.arcade.domain.game.GameRepository;
import io.pp.arcade.domain.slot.Slot;
import io.pp.arcade.domain.slot.SlotRepository;
import io.pp.arcade.domain.team.Team;
import io.pp.arcade.domain.team.TeamRepository;
import io.pp.arcade.domain.user.User;
import io.pp.arcade.domain.user.UserRepository;
import io.pp.arcade.global.type.GameType;
import io.pp.arcade.global.type.StatusType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import javax.transaction.Transactional;

import java.time.LocalDateTime;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Import(RestDocsConfiguration.class)
class CurrentMatchControllerImplTest {

    @Autowired
    private CurrentMatchRepository currentMatchRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private SlotRepository slotRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    TestInitiator initiator;

    User user;
    User user1;
    User user2;
    User user3;
    User user4;
    User user5;
    User user6;
    Slot[] slotList;

    @BeforeEach
    void init() {
        initiator.letsgo();
        user = initiator.users[0];
        user1 = initiator.users[1];
        user2 = initiator.users[2];
        user3 = initiator.users[3];
        user4 = initiator.users[4];
        user5 = initiator.users[5];
        user6 = initiator.users[6];
        slotList = initiator.slots;
    }

    @Transactional
    public void currentMatchSave(Game game, Slot slot, User user, boolean m, boolean ism) {
        currentMatchRepository.save(CurrentMatch.builder()
                .user(user)
                .slot(slot)
                .game(game)
                .matchImminent(m)
                .isMatched(ism)
                .build());
    }

    @Transactional
    public void saveTeam(Team team) {
        teamRepository.save(team);
    }

    ;

    @Transactional
    public Slot saveSlot(Slot slot) {
        return slotRepository.save(slot);
    }

    @Transactional
    public Game saveGame(Game game) {
        return gameRepository.save(game);
    }


    @Test
    @Transactional
    void currentMatchFind() throws Exception {
        /*
         * 현재 유저 매칭 테이블 탐색
         * - 해당 유저가 예약된 경기가 없을 경우
         * - 해당 유저가 예약된 경기가 있으며 5분전이 아닐 경우
         * - 해당 유저가 예약된 경기가 있으며 5분전일 때, 매치가 성사되지 않을 경우
         * - 해당 유저가 예약된 경기가 있으며 5분전일 때, 매치가 성사된 경우
         * - 해당 유저가 예약된 경기가 있으며 경기가 시작된 경우 +
         * - 시간이 지나도 매칭이 안되어 클로즈된 슬롯에 매칭 테이블 요청 보내는 경우 +
         * - 유효하지 않은 토큰으로 매칭 테이블 요청을 보내는 경우 +
         * */

        // 해당 유저가 예약된 경기가 없을 경우
        // 유저 : user
        mockMvc.perform(get("/pingpong/match/current").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + initiator.tokens[0].getAccessToken()))
                .andExpect(status().isOk())
                .andDo(document("current-match-info-none"));

        // 해당 유저가 예약된 경기가 있으며 5분전이 아닐 경우
        // 유저 : user1
        // 슬롯 : slotList.get(0)
        Team team1 = Team.builder().teamPpp(100).headCount(1).user1(user1).user2(null).score(0).build();
        Team team2 = Team.builder().teamPpp(100).headCount(0).user1(null).user2(null).score(0).build();
        saveTeam(team1);
        saveTeam(team2);
        Slot slot = Slot.builder().tableId(1).team1(team1).team2(team2).headCount(4).time(LocalDateTime.now().plusDays(1)).build();
        slot = saveSlot(slot);
        currentMatchSave(null, slot, user1, false, false);
        mockMvc.perform(get("/pingpong/match/current").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + initiator.tokens[1].getAccessToken()))
                .andExpect(status().isOk())
                .andDo(document("current-match-info-standby-not-Imminent"));

        // 해당 유저가 예약된 경기가 있으며 5분전일 때, 매치가 성사되지 않을 경우
        // 유저 : user2
        // 슬롯 : slotList.get(1)
        team1 = Team.builder().teamPpp(100).headCount(1).user1(user2).user2(null).score(0).build();
        team2 = Team.builder().teamPpp(100).headCount(0).user1(null).user2(null).score(0).build();
        saveTeam(team1);
        saveTeam(team2);
        slot = Slot.builder().tableId(1).team1(team1).team2(team2).headCount(4).time(LocalDateTime.now().plusDays(1)).build();
        slot = saveSlot(slot);
        currentMatchSave(null, slot, user2, true, false);
        mockMvc.perform(get("/pingpong/match/current").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + initiator.tokens[2].getAccessToken()))
                .andExpect(status().isOk())
                .andDo(document("current-match-info-standby-Imminent"));

        // 해당 유저가 예약된 경기가 있으며 5분전일 때, 매치가 성사된 경우
        // 유저 : user3
        // 슬롯 : slot
        team1 = Team.builder().teamPpp(100).headCount(2).user1(user3).user2(user4).score(0).build();
        team2 = Team.builder().teamPpp(100).headCount(2).user1(user5).user2(user6).score(0).build();
        saveTeam(team1);
        saveTeam(team2);
        slot = Slot.builder().tableId(1).team1(team1).team2(team2).headCount(4).time(LocalDateTime.now().plusDays(1)).build();
        slot = saveSlot(slot);

        currentMatchSave(null, slot, user3, true, true);
        mockMvc.perform(get("/pingpong/match/current").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + initiator.tokens[3].getAccessToken()))
                .andExpect(status().isOk())
                .andDo(document("current-match-info-matching-Imminent"));

        // 해당 유저가 예약된 경기가 있으며 경기가 시작된 경우
        // 유저 : user4
        // 슬롯 : slot
        team1 = Team.builder().teamPpp(100).headCount(2).user1(user3).user2(user4).score(0).build();
        team2 = Team.builder().teamPpp(100).headCount(2).user1(user5).user2(user6).score(0).build();
        saveTeam(team1);
        saveTeam(team2);
        slot = Slot.builder().tableId(1).team1(team1).team2(team2).headCount(4).time(LocalDateTime.now().plusDays(1)).build();
        slot = saveSlot(slot);
        Game game = Game.builder().team1(team1).team2(team2).type(GameType.DOUBLE).season(1).slot(slot).time(slot.getTime()).status(StatusType.LIVE).build();
        game = saveGame(game);
        currentMatchSave(game, slot, user4, true, true);
        mockMvc.perform(get("/pingpong/match/current").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + initiator.tokens[4].getAccessToken()))
                .andExpect(status().isOk())
                .andDo(document("current-match-info-gaming"));

        // 유효하지 않은 토큰으로 매칭 테이블 요청을 보내는 경우
        // 유저 : none
        // 슬롯 : slot
        team1 = Team.builder().teamPpp(100).headCount(2).user1(user3).user2(user4).score(0).build();
        team2 = Team.builder().teamPpp(100).headCount(2).user1(user5).user2(user6).score(0).build();
        saveTeam(team1);
        saveTeam(team2);
        slot = Slot.builder().tableId(1).team1(team1).team2(team2).headCount(4).time(LocalDateTime.now().plusDays(1)).build();
        slot = saveSlot(slot);
        Game game1 = Game.builder().team1(team1).team2(team2).type(GameType.DOUBLE).season(1).slot(slot).time(slot.getTime()).status(StatusType.LIVE).build();
        game = saveGame(game1);
        currentMatchSave(game1, slot, user4, true, true);
        mockMvc.perform(get("/pingpong/match/current").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andDo(document("current-match-info-unauthorized-request"));
    }
}