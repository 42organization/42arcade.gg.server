package io.pp.arcade.domain.game.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.pp.arcade.RestDocsConfiguration;
import io.pp.arcade.TestInitiator;
import io.pp.arcade.domain.currentmatch.CurrentMatch;
import io.pp.arcade.domain.currentmatch.CurrentMatchRepository;
import io.pp.arcade.domain.game.Game;
import io.pp.arcade.domain.game.GameRepository;
import io.pp.arcade.domain.pchange.PChange;
import io.pp.arcade.domain.pchange.PChangeRepository;
import io.pp.arcade.domain.slot.Slot;
import io.pp.arcade.domain.team.Team;
import io.pp.arcade.domain.user.User;
import io.pp.arcade.global.type.GameType;
import io.pp.arcade.global.type.StatusType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.transaction.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureRestDocs
@AutoConfigureMockMvc
@Import(RestDocsConfiguration.class)
class GameControllerTest {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private GameRepository gameRepository;
    @Autowired
    private CurrentMatchRepository currentMatchRepository;
    @Autowired
    TestInitiator initiator;
    private final int GAMESIZE = 200;
    @Autowired
    private PChangeRepository pChangeRepository;
    Game liveGame;
    Game waitGame;
    Game doubleGame;
    User[] users;
    Team[] teams;
    Game[] endGames;
    Slot[] slots;

    @BeforeEach
    void init() {
        initiator.letsgo();
        users = initiator.users;
        teams = initiator.teams;
        slots = initiator.slots;
        endGames = new Game[GAMESIZE - 2];

        for (int i = 0; i < GAMESIZE - 2; i++){
            endGames[i] = gameRepository.save(Game.builder().slot(slots[0]).team1(teams[0]).team2(teams[1]).type(GameType.SINGLE).time(slots[0].getTime()).season(1).status(StatusType.END).build());
        }
        // double, wait, live 순으로
        doubleGame = gameRepository.save(Game.builder().slot(slots[10]).team1(teams[4]).team2(teams[5]).type(GameType.DOUBLE).time(slots[10].getTime()).season(1).status(StatusType.WAIT).build());
        waitGame = gameRepository.save(Game.builder().slot(slots[0]).team1(teams[0]).team2(teams[1]).type(GameType.SINGLE).time(slots[0].getTime()).season(1).status(StatusType.WAIT).build());
        liveGame = gameRepository.save(Game.builder().slot(slots[0]).team1(teams[0]).team2(teams[1]).type(GameType.SINGLE).time(slots[0].getTime()).season(1).status(StatusType.LIVE).build());

        /* pChange 생성 */
        for (int i = 0; i < GAMESIZE - 2; i++){
            pChangeRepository.save(PChange.builder().game(endGames[i]).user(users[0]).pppChange(20).pppResult(1000).build());
            pChangeRepository.save(PChange.builder().game(endGames[i]).user(users[1]).pppChange(20).pppResult(1000).build());
        }
       /*
        * 게임 결과 정보 조회
        * -> 단식,복식 게임 생성
        * -> matchTable에 단식 복식 유저 등록
        * */
        currentMatchRepository.save(CurrentMatch.builder().matchImminent(true).isMatched(true)
                .game(endGames[0]).slot(endGames[0].getSlot()).user(users[0]).build());
        currentMatchRepository.save(CurrentMatch.builder().matchImminent(true).isMatched(true)
                .game(doubleGame).slot(doubleGame.getSlot()).user(users[4]).build());

    }


    @Transactional
    void addUserInTeam(Team team, User user, boolean is1){
        if (is1)
            team.setUser1(user);
        else
            team.setUser2(user);
        team.setTeamPpp(user.getPpp());
        team.setHeadCount(1);
    }

    @Transactional
    void addCurrentMatch(Game game, User user) {
        currentMatchRepository.save(CurrentMatch.builder()
                .matchImminent(true)
                .isMatched(true)
                .game(game)
                .slot(game.getSlot())
                .user(user)
                .build());
    }

    @Transactional
    Game saveGame(Slot slot, Team team1, Team team2) {
        Game game = Game.builder()
                .slot(slot)
                .team1(team1)
                .team2(team2)
                .type(GameType.DOUBLE)
                .time(slot.getTime())
                .season(1)
                .status(StatusType.LIVE)
                .build();
        gameRepository.save(game);
        return game;
    }


    @Test
    @Transactional
    @DisplayName("게임 결과 정보 - /games/result")
    void testGameUserInfo() throws  Exception {
        /*
         * 테스트 내역
         * - 진행 중인 게임의 결과가 없는 경우
         * - 단식 게임이 진행 중인 경우
         * - 복식 게임이 진행 중인 경우
         * - gameId 에 integer 가 아닌 값을 넣는 경우 (400)
         * - gameId 가 음수로 들어온 경우
         * - gameId 가 null인 경우
         * - gameId 가 String인 경우
         * - count 가 음수인 경우
         * - count 가 null인 경우
         * - count 가 String인 경우
         * - count 가 100개 이상인 경우
         * - gameId 가 100 인 경우 - 200,game 리스트 반환
         * - status 가 다른 값인 경우
         * - status 가 null인 경우
         * - score 가 null인 경우
         * - 한 쪽 스코어가 3 이상인 경우
         * - 양측 score 가 1:1로 입력된 경우
         * - score가 소수인 경우
         * - score가 음수인 경우
         * - 양쪽 score 의 합이 4 이상인 경우 >>>> 1:5가 아니라 2:2로 수정필요
         * - intraId 를 못 찾는 경우
         * - 유저의 경기기록이 하나도 없는 경우
         * - 유효하지 않은 토큰으로 매칭 테이블 요청을 보내는 경우
         * */


        /*
         * 사용자 - 진행중인 게임 결과가 없는 경우
         * -> 400
         * */
        mockMvc.perform(get("/pingpong/games/result").contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + initiator.tokens[3].getAccessToken()))
                .andExpect(status().isBadRequest())
                .andDo(document("game-find-result-4XXError-cause-no-exists"));
        /*
         * 사용자 - 단식 진행 중인 경우
         * myTeam [{hakim, imageUri}]
         * enemyTeam [{nheo, imageUri}]
         * -> 200
         * */
        mockMvc.perform(get("/pingpong/games/result").contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + initiator.tokens[0].getAccessToken()))
                .andExpect(jsonPath("$.myTeam[0].intraId").value(users[0].getIntraId()))
                .andExpect(jsonPath("$.myTeam[0].userImageUri").value(users[0].getImageUri()))
                .andExpect(jsonPath("$.enemyTeam[0].intraId").value(users[1].getIntraId()))
                .andExpect(jsonPath("$.enemyTeam[0].userImageUri").value(users[1].getImageUri()))
                .andExpect(status().isOk())
                .andDo(document("game-find-results-single"));
        /*
         * 사용자 - 복식 진행 중인 경우
         * jekim wochae jabae jihyukim
         * myTeam [{jekim, imageUri}, {wochae, }]
         * enemyTeam [{jabae, imageUri}, {jihyukim, }]
         * -> 200
         * */
        mockMvc.perform(get("/pingpong/games/result").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + initiator.tokens[4].getAccessToken()))
                .andExpect(jsonPath("$.myTeam[0].intraId").value(users[4].getIntraId()))
                .andExpect(jsonPath("$.myTeam[0].userImageUri").value(users[4].getImageUri()))
                .andExpect(jsonPath("$.myTeam[1].intraId").value(users[5].getIntraId()))
                .andExpect(jsonPath("$.myTeam[1].userImageUri").value(users[5].getImageUri()))
                .andExpect(jsonPath("$.enemyTeam[0].intraId").value(users[6].getIntraId()))
                .andExpect(jsonPath("$.enemyTeam[0].userImageUri").value(users[6].getImageUri()))
                .andExpect(jsonPath("$.enemyTeam[1].intraId").value(users[7].getIntraId()))
                .andExpect(jsonPath("$.enemyTeam[1].userImageUri").value(users[7].getImageUri()))
                .andExpect(status().isOk())
                .andDo(document("game-find-results-double"));
    }

    @Test
    @Transactional
    @DisplayName("최근 게임 기록 - /games")
    void gameUserInfo() throws Exception {
        /*
         * gameId != Integer (숫자가 아닌 경우)
         * -> RequestDto Binding Error
         * -> 400
         * */
        LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("gameId", "string");
        params.add("count", "20");
        params.add("status", StatusType.END.getCode());
        mockMvc.perform(get("/pingpong/games").contentType(MediaType.APPLICATION_JSON)
                .params(params)
                .header("Authorization", "Bearer " + initiator.tokens[0].getAccessToken()))
                .andExpect(status().isBadRequest())
                .andDo(document("game-find-results-4XXError-cause-gameId-is-not-Integer"));

        /*
         * gameId -> -1 (음수인 경우)
         * -> 최근 게임정보 반환
         * -> 200
         * */
        MultiValueMap<String,String> params2 = new LinkedMultiValueMap<>();
        params2.add("gameId", "-1");
        params2.add("count", "20");
        params2.add("status", StatusType.END.getCode());
        mockMvc.perform(get("/pingpong/games").contentType(MediaType.APPLICATION_JSON)
                .params(params2)
                .header("Authorization", "Bearer " + initiator.tokens[0].getAccessToken()))
//                .andExpect(jsonPath("$.games[0].gameId").value(endGames[endGames.length - 1].getId()))
                .andExpect(jsonPath("$.games.length()").value(20))
                .andExpect(status().isOk())
                .andDo(document("game-find-results-gameId-is-negative"));

        /*
         * gameId -> null
         * -> 최근 게임정보 반환
         * -> 200
         * */
        MultiValueMap<String,String> params3 = new LinkedMultiValueMap<>();
        params3.add("count", "20");
        params3.add("status", StatusType.END.getCode());
        mockMvc.perform(get("/pingpong/games").contentType(MediaType.APPLICATION_JSON)
                .params(params3)
                .header("Authorization", "Bearer " + initiator.tokens[0].getAccessToken()))
                .andExpect(jsonPath("$.games[0].gameId").value(endGames[endGames.length - 1].getId()))
                .andExpect(jsonPath("$.games.length()").value(20))
                .andExpect(status().isOk())
                .andDo(document("game-find-result-gameId-is-null"));

        /*
         * count -> string (숫자가 아닌 경우)
         * -> RequestDto Binding Error
         * -> 400
         * */
        MultiValueMap<String,String> params11 = new LinkedMultiValueMap<>();
        params11.add("count", "string");
        params11.add("status", StatusType.END.getCode());
        mockMvc.perform(get("/pingpong/games").contentType(MediaType.APPLICATION_JSON)
                        .params(params11)
                        .header("Authorization", "Bearer " + initiator.tokens[0].getAccessToken()))
                .andExpect(status().isBadRequest())
                .andDo(document("game-find-results-4XXError-cause-count-is-string"));

        /*
         * count -> -1 (음수인 경우)
         * -> 기본 20개의 게임정보 반환
         * -> 200
         * */
        MultiValueMap<String,String> params12 = new LinkedMultiValueMap<>();
        params12.add("count", "-1");
        params12.add("status", StatusType.END.getCode());
        mockMvc.perform(get("/pingpong/games").contentType(MediaType.APPLICATION_JSON)
                        .params(params12)
                        .header("Authorization", "Bearer " + initiator.tokens[0].getAccessToken()))
                .andExpect(jsonPath("$.games[0].gameId").value(endGames[endGames.length - 1].getId()))
                .andExpect(jsonPath("$.games.length()").value(20))
                .andExpect(status().isOk())
                .andDo(document("game-find-results-count-is-negative"));

        /*
         * count -> null
         * -> 기본 20개의 게임정보 반환
         * -> 200
         * */
        MultiValueMap<String,String> params4 = new LinkedMultiValueMap<>();
        params4.add("gameId", "12345678");
        params4.add("status", StatusType.END.getCode());
        mockMvc.perform(get("/pingpong/games").contentType(MediaType.APPLICATION_JSON)
                .params(params4)
                .header("Authorization", "Bearer " + initiator.tokens[0].getAccessToken()))
//                .andExpect(jsonPath("$.games[0].gameId").value(endGames[endGames.length - 1].getId()))
                .andExpect(jsonPath("$.games.length()").value(20))
                .andExpect(status().isOk())
                .andDo(document("game-find-results-count-is-null"));
        /*
         * count -> 1234 (100이상인 경우)
         * -> 100개의 게임정보 반환
         * -> 200
         * */
        MultiValueMap<String,String> params5 = new LinkedMultiValueMap<>();
        params5.add("count", "12345678");
        params5.add("status", StatusType.END.getCode());
        mockMvc.perform(get("/pingpong/games").contentType(MediaType.APPLICATION_JSON)
                .params(params5)
                .header("Authorization", "Bearer " + initiator.tokens[0].getAccessToken()))
                .andExpect(jsonPath("$.games[0].gameId").value(endGames[endGames.length - 1].getId()))
                .andExpect(jsonPath("$.games.length()").value(100))
                .andExpect(status().isOk())
                .andDo(document("game-find-results-count-is-bigger-than-100"));

        /*
         * status -> NOTHING (다른 값인 경우)
         * -> live, wait, end 모든 게임정보 반환
         * -> 200
         * */
        MultiValueMap<String,String> params6 = new LinkedMultiValueMap<>();
        params6.add("count", "20");
        params6.add("status", "NOTHING");
        mockMvc.perform(get("/pingpong/games").contentType(MediaType.APPLICATION_JSON)
                .params(params6)
                .header("Authorization", "Bearer " + initiator.tokens[0].getAccessToken()))
                .andExpect(jsonPath("$.games[0].gameId").value(liveGame.getId()))
                .andExpect(jsonPath("$.games[1].gameId").value(waitGame.getId()))
                .andExpect(jsonPath("$.games[2].gameId").value(doubleGame.getId()))
                .andExpect(jsonPath("$.games[3].gameId").value(endGames[endGames.length - 1].getId()))
                .andExpect(jsonPath("$.games.length()").value(20))
                .andExpect(status().isOk())
                .andDo(document("game-user-info-status-wrong"));

        /*
         * status -> null
         * -> live, wait, end 모든 게임정보 반환
         * -> 200
         * */
        MultiValueMap<String,String> params7 = new LinkedMultiValueMap<>();
        params7.add("count", "20");
        mockMvc.perform(get("/pingpong/games").contentType(MediaType.APPLICATION_JSON)
                .params(params7)
                .header("Authorization", "Bearer " + initiator.tokens[0].getAccessToken()))
                .andExpect(jsonPath("$.games[0].gameId").value(liveGame.getId()))
                .andExpect(jsonPath("$.games[1].gameId").value(waitGame.getId()))
                .andExpect(jsonPath("$.games[2].gameId").value(doubleGame.getId()))
                .andExpect(jsonPath("$.games[3].gameId").value(endGames[endGames.length - 1].getId()))
                .andExpect(jsonPath("$.games.length()").value(20))
                .andExpect(status().isOk())
                .andDo(document("game-user-info-status-is-null"));

        /*
         * GameId -> 1000
         * -> GameId 99번부터 리스트 반환
         * -> 200
         * */
        MultiValueMap<String,String> params8 = new LinkedMultiValueMap<>();
        params8.add("gameId", "12345678");
        params8.add("count", "20");
        params8.add("status", StatusType.END.getCode());
        mockMvc.perform(get("/pingpong/games").contentType(MediaType.APPLICATION_JSON)
                .params(params8)
                .header("Authorization", "Bearer " + initiator.tokens[0].getAccessToken()))
                .andExpect(jsonPath("$.games.length()").value(20))
                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.games[0].gameId").value(endGames[99 - 1].getId()))
                .andDo(document("game-find-results-find-id-1000"));
    }

    @Test
    @Transactional
    void gameResultByUserIdAndIndexAndCount() throws Exception {
        /*
         * IntraId 찾을 수 없는 경우
         * -> 400
         * */
        LinkedMultiValueMap<String, String> params2 = new LinkedMultiValueMap<>();
        params2.add("gameId", "1234");
        params2.add("count", "20");
        mockMvc.perform(get("/pingpong/users/{intraId}/games","NOTFOUND").contentType(MediaType.APPLICATION_JSON)
                        .params(params2)
                        .header("Authorization", "Bearer " + initiator.tokens[0].getAccessToken()))
                .andExpect(status().isBadRequest())
                .andDo(document("game-find-results-4xxError-cause-couldn't-find-intraId"));

        /*
         * 사용자 - 경기기록이 없는 경우
         * -> 200
         * */
        LinkedMultiValueMap<String, String> params3 = new LinkedMultiValueMap<>();
        params3.add("gameId", "1234");
        params3.add("count", "20");
        mockMvc.perform(get("/pingpong/users/{intraId}/games", users[2].getIntraId()).contentType(MediaType.APPLICATION_JSON)
                        .params(params3)
                        .header("Authorization", "Bearer " + initiator.tokens[0].getAccessToken()))
                .andExpect(jsonPath("$.games").isEmpty())
                .andExpect(jsonPath("$.lastGameId").value(0))
                .andExpect(status().isOk())
                .andDo(document("find-game-results-theres-no-game-record"));
    }

    @Test
    @Transactional
    @DisplayName("게임 결과 저장 - /games/result")
    void gameResultSave() throws Exception {

        Team team1 = slots[1].getTeam1();
        Team team2 = slots[1].getTeam2();
        addUserInTeam(team1, users[2], true);
        addUserInTeam(team2, users[3], true);
        Game game = saveGame(slots[1], team1, team2);
        addCurrentMatch(game, users[2]);
        addCurrentMatch(game, users[3]);
        Map<String, String> body1 = new HashMap<>();

//        - Score값이 null인 경우
        mockMvc.perform(post("/pingpong/games/result").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + initiator.tokens[2].getAccessToken()))
                .andExpect(status().isBadRequest())
                .andDo(document("game-result-save-Error-cause-score-is-null"));

//        - Score별 값이 3이상일 경우
        Map<String, String> body2 = new HashMap<>();
        body2.put("myTeamScore", "3");
        body2.put("enemyTeamScore", "1");
        mockMvc.perform(post("/pingpong/games/result").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body2))
                        .header("Authorization", "Bearer " + initiator.tokens[2].getAccessToken()))
                .andExpect(status().isBadRequest())
                .andDo(document("game-result-save-Error-cause-score-is-bigger-than-3"));

//        - Score값이 1:1 경우
        Map<String, String> body3 = new HashMap<>();
        body3.put("myTeamScore", "1");
        body3.put("enemyTeamScore", "1");
        mockMvc.perform(post("/pingpong/games/result").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body3))
                        .header("Authorization", "Bearer " + initiator.tokens[2].getAccessToken()))
                .andExpect(status().isBadRequest())
                .andDo(document("game-result-save-Error-cause-score-1:1"));

//        - Score합이 4이상일 경우
        Map<String, String> body4 = new HashMap<>();
        body4.put("myTeamScore", "2");
        body4.put("enemyTeamScore", "2");
        mockMvc.perform(post("/pingpong/games/result").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body4))
                        .header("Authorization", "Bearer " + initiator.tokens[2].getAccessToken()))
                .andExpect(status().isBadRequest())
                .andDo(document("game-result-save-Error-cause-score-sum-is-bigger-than-4"));

//        - Score값이 음수일 경우
        Map<String, String> body5 = new HashMap<>();
        body5.put("myTeamScore", "2");
        body5.put("enemyTeamScore", "-3");
        mockMvc.perform(post("/pingpong/games/result").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body5))
                        .header("Authorization", "Bearer " + initiator.tokens[2].getAccessToken()))
                .andExpect(status().isBadRequest())
                .andDo(document("game-result-save-Error-cause-score-is-negative"));

//        - Score값이 소수인 경우
        Map<String, String> body6 = new HashMap<>();
        body6.put("myTeamScore", "0.75");
        body6.put("enemyTeamScore", "2");
        mockMvc.perform(post("/pingpong/games/result").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body6))
                        .header("Authorization", "Bearer " + initiator.tokens[2].getAccessToken()))
                .andExpect(status().isBadRequest())
                .andDo(document("game-result-save-Error-cause-score-is-decimal"));

//        - Score가 숫자가 아닌 경우
        Map<String, String> body7 = new HashMap<>();
        body7.put("myTeamScore", "win");
        body7.put("enemyTeamScore", "lose");
        mockMvc.perform(post("/pingpong/games/result").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body7))
                        .header("Authorization", "Bearer " + initiator.tokens[2].getAccessToken()))
                .andExpect(status().isBadRequest())
                .andDo(document("game-result-save-Error-cause-score-is-not-integer"));

//        정 상 요 청
        Map<String, String> body8 = new HashMap<>();
        body8.put("myTeamScore", "2");
        body8.put("enemyTeamScore", "0");
        mockMvc.perform(post("/pingpong/games/result").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body8))
                        .header("Authorization", "Bearer " + initiator.tokens[2].getAccessToken()))
                .andExpect(status().isCreated())
                .andDo(document("game-result-save-isCreated"));

//        - 게임결과 입력이 끝난 경우
//          → 202 (Accepted)
        Map<String, String> body9 = new HashMap<>();
        body9.put("myTeamScore", "2");
        body9.put("enemyTeamScore", "1");
        mockMvc.perform(post("/pingpong/games/result").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body9))
                        .header("Authorization", "Bearer " + initiator.tokens[2].getAccessToken()))
                .andExpect(status().isAccepted())
                .andDo(document("game-result-save-isAccepted"));

//        입력된결과확인
        LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("count", "20");
        mockMvc.perform(get("/pingpong/users/{intraId}/games", users[2].getIntraId()).contentType(MediaType.APPLICATION_JSON)
                .params(params)
                .header("Authorization", "Bearer " + initiator.tokens[2].getAccessToken()))
                .andExpect(status().isOk())
                .andDo(document("game-find-results-only-user-request"));
    }
}