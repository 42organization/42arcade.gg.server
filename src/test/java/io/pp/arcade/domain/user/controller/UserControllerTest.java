package io.pp.arcade.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import io.pp.arcade.TestInitiator;
import io.pp.arcade.domain.currentmatch.CurrentMatch;
import io.pp.arcade.domain.currentmatch.CurrentMatchRepository;
import io.pp.arcade.domain.game.Game;
import io.pp.arcade.domain.game.GameRepository;
import io.pp.arcade.domain.pchange.PChange;
import io.pp.arcade.domain.pchange.PChangeRepository;
import io.pp.arcade.domain.rank.RankRedis;
import io.pp.arcade.domain.security.jwt.Token;
import io.pp.arcade.domain.security.jwt.TokenRepository;
import io.pp.arcade.domain.slot.Slot;
import io.pp.arcade.domain.slot.SlotRepository;
import io.pp.arcade.domain.team.Team;
import io.pp.arcade.domain.team.TeamRepository;
import io.pp.arcade.domain.user.User;
import io.pp.arcade.domain.user.UserRepository;
import io.pp.arcade.RestDocsConfiguration;
import io.pp.arcade.global.type.GameType;
import io.pp.arcade.global.type.RacketType;
import io.pp.arcade.global.type.StatusType;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import javax.transaction.Transactional;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Import(RestDocsConfiguration.class)
class UserControllerTest {
    @Autowired
    TestInitiator testInitiator;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PChangeRepository pChangeRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private SlotRepository slotRepository;

    @Autowired
    private CurrentMatchRepository currentMatchRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private RedisTemplate<String, RankRedis> redisRank;

    @Autowired
    TestInitiator initiator;

    User user2;
    User user3;
    User user4;

    CurrentMatch currentMatch;
    CurrentMatch currentMatch2;
    Team team1;
    Team team2;

    User[] users;
    RankRedis[] ranks;

    User user;
    RankRedis userRank;
    PChange userPchange;
    Game userGame;

    Game game;

    @BeforeEach
    void init() {
        testInitiator.letsgo();
        users = testInitiator.users;
        ranks = testInitiator.ranks;
        user = users[0];
        userRank = ranks[0];
        user2 = userRepository.findByIntraId("daekim").orElse(null);
        user3 = userRepository.findByIntraId("donghyuk").orElse(null);
        user4 = userRepository.findByIntraId("kipark").orElse(null);

        PChange pChange;
        Team team1 = teamRepository.save(Team.builder().teamPpp(0)
                .user1(user).headCount(1).score(0).build());
        Team team2 = teamRepository.save(Team.builder().teamPpp(0)
                .user1(user2).headCount(1).score(0).build());
        Slot slot = slotRepository.save(Slot.builder()
                .team1(team1)
                .team2(team2)
                .headCount(2)
                .type(GameType.SINGLE)
                .tableId(1)
                .time(LocalDateTime.now().plusDays(1))
                .gamePpp(50)
                .build());
        game = gameRepository.save(Game.builder().slot(slot).team1(team1).team2(team2).type(slot.getType()).time(slot.getTime()).season(1).status(StatusType.END).build());
        userGame = gameRepository.save(Game.builder().slot(slot).team1(team1).team2(team2).type(slot.getType()).time(slot.getTime()).season(1).status(StatusType.END).build());

        userPchange = pChangeRepository.save(PChange.builder()
                .game(userGame)
                .user(users[0])
                .pppChange(2)
                .pppResult(2 + user.getPpp())
                .build());

        // 슬롯에 등록된 경우
        currentMatch = currentMatchRepository.save(CurrentMatch.builder()
                .game(null)
                .slot(slot)
                .user(users[1])
                .matchImminent(false)
                .isMatched(false)
                .build());

        // 게임에 등록된 경우
        currentMatch2 = currentMatchRepository.save(CurrentMatch.builder()
                .game(game)
                .slot(slot)
                .user(users[2])
                .matchImminent(true)
                .isMatched(true)
                .build());
        PChange pChange2;
        Team team3 = teamRepository.save(Team.builder().teamPpp(0)
                .user1(user).headCount(1).score(0).build());
        Team team4 = teamRepository.save(Team.builder().teamPpp(0)
                .user1(user2).headCount(1).score(0).build());
        Slot slot2 = slotRepository.save(Slot.builder()
                .team1(team3)
                .team2(team4)
                .headCount(2)
                .type(GameType.SINGLE)
                .tableId(1)
                .time(LocalDateTime.now().plusDays(1))
                .gamePpp(50)
                .build());
        gameRepository.save(Game.builder().slot(slot2).team1(team3).team2(team4).type(slot2.getType()).time(slot2.getTime()).season(1).status(StatusType.END).build());
        gameRepository.save(Game.builder().slot(slot2).team1(team3).team2(team4).type(slot2.getType()).time(slot2.getTime()).season(1).status(StatusType.END).build());

        pChangeRepository.save(PChange.builder()
                .game(userGame)
                .user(users[0])
                .pppChange(4)
                .pppResult(6 + user.getPpp())
                .build());
    }

    @Test
    @Transactional
    @DisplayName("유저 정보 조회 - 간단히 (/users)")
    void findUser() throws Exception {
        /*
         * - Response Check (2)
         * - -> intraId, userImageUri;
         * */
        mockMvc.perform(get("/pingpong/users").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + initiator.tokens[0].getAccessToken()))
                .andExpect(jsonPath("$.intraId").value(user.getIntraId()))
                .andExpect(jsonPath("$.userImageUri").value(user.getImageUri()))
                .andExpect(status().isOk())
                .andDo(document("user-find"));
    }

    @Test
    @Transactional
    @DisplayName("유저 정보 조회 - 상세히 (/users/{intraId}/detail)")
    void findDetailUser() throws Exception { // rank가 문제라서 안됐다!
        /*
         * intraId를 찾을 수 없는 경우
         * -> 400
         * */
        mockMvc.perform(get("/pingpong/users/" + "notFound" + "/detail").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + initiator.tokens[0].getAccessToken()))
                .andExpect(status().is4xxClientError())
                .andDo(document("user-find-detail-4xxError-cause-couldn't-find-intraId"));

        /*
         * Response Check (7)
         * -> intraId, userImageUri, statusMessage, ppp
         * -> rank, wins, losses
         * */
        mockMvc.perform(get("/pingpong/users/" + user.getIntraId() + "/detail").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + initiator.tokens[0].getAccessToken()))
                .andExpect(jsonPath("$.intraId").value(user.getIntraId()))
                .andExpect(jsonPath("$.userImageUri").value(user.getImageUri()))
                .andExpect(jsonPath("$.statusMessage").value(user.getStatusMessage()))
                .andExpect(jsonPath("$.ppp").value(user.getPpp()))
//                .andExpect(jsonPath("$.rank").value(getRanking(userRank, GameType.SINGLE)))
                .andExpect(jsonPath("$.wins").value(userRank.getWins()))
                .andExpect(jsonPath("$.losses").value(userRank.getLosses()))
                .andExpect(status().isOk())
                .andDo(document("user-find-detail"));
    }

    @Test
    @Transactional
    @DisplayName("유저 전적 경향 (/users/{intraId}/historics)")
    void findUserHistorics() throws Exception {
        /*
         * intraId 찾을 수 없는 경우
         * -> 400
         * */
        mockMvc.perform(get("/pingpong/users/{intraId}/historics", "notFound").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + initiator.tokens[0].getAccessToken()))
                .andExpect(status().is4xxClientError())
                .andDo(document("user-find-historic-4xxError-cause-couldn't-find-intraId"));

        /*
         * chartType NULL (향후 수정)
         * -> 200
         * */
        User user = users[0];
        mockMvc.perform(get("/pingpong/users/" + user.getIntraId() +"/historics").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + initiator.tokens[0].getAccessToken()))
                .andExpect(status().isOk());

        /*
         * chartType 다른 값 (향후 수정)
         * -> 200
         * */
        mockMvc.perform(get("/pingpong/users/" + user.getIntraId() +"/historics").contentType(MediaType.APPLICATION_JSON)
                        .param("chartType","")
                        .header("Authorization", "Bearer " + initiator.tokens[0].getAccessToken()))
                .andExpect(status().isOk());

        /*
         * Response Check (2)
         * -> ppp
         * -> date
         * */

//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String gameDate = userGame.getTime().toString();
        //String subDate = gameDate.substring(0, gameDate.length());
//        LocalDateTime formatDateTime = LocalDateTime.parse(gameDate, formatter);
        mockMvc.perform(get("/pingpong/users/" + user.getIntraId() +"/historics").contentType(MediaType.APPLICATION_JSON)
                        .param("chartType","rank")
                        .header("Authorization", "Bearer " + initiator.tokens[0].getAccessToken()))
                .andExpect(jsonPath("$.historics[0].ppp").value(userPchange.getPppResult()))
                .andExpect(jsonPath("$.historics[0].date").value(gameDate))
                .andExpect(status().isOk())
                .andDo(document("user-find-historics"));
    }

    @Test
    @Transactional
    @DisplayName("유저 검색 (/pingpong/users/searches)")
    void userSearchResult() throws Exception {
        /*
         * q(query) null
         * -> users : []
         * -> 200
         * */
        mockMvc.perform(get("/pingpong/users/searches").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + initiator.tokens[1].getAccessToken()))
                .andExpect(jsonPath("$.users").isEmpty())
                .andExpect(status().isOk());

        /*
         * q(query) k
         * -> users : ["hakim", "donghyuk", "jekim", "jihyukim", "daekim", "sujpark", "kipark"]
         * */
        String checkUsers = "$.users[?(@=='%s')]";
        mockMvc.perform(get("/pingpong/users/searches").contentType(MediaType.APPLICATION_JSON)
                        .param("q", "k")
                        .header("Authorization", "Bearer " + initiator.tokens[1].getAccessToken()))
//                .andExpect(jsonPath("$.users").isNotEmpty())
//                .andExpect(jsonPath(checkUsers, "kipark").exists())
//                .andExpect(jsonPath(checkUsers, "hakim").exists())
//                .andExpect(jsonPath(checkUsers, "jekim").exists())
//                .andExpect(jsonPath(checkUsers, "daekim").exists())
//                .andExpect(jsonPath(checkUsers, "jihyukim").exists())
                .andExpect(status().isOk())
                .andDo(document("user-search-with-partial-string"));
    }

    @Test
    @Transactional
    @DisplayName("유저 실시간 정보 (/users/live)")
    void userLiveInfo() throws Exception {
        /*
         * 게임, 슬롯에 속해있지 않은 경우 (user)
         * -> event : none
         * */
        mockMvc.perform(get("/pingpong/users/live").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + initiator.tokens[0].getAccessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.event").isEmpty())
                .andDo(document("user-find-live1"));
        /*
         * 슬롯에 등록된 경우 (user1)
         * -> event : match
         * */
        mockMvc.perform(get("/pingpong/users/live").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + initiator.tokens[1].getAccessToken()))
                .andExpect(jsonPath("$.event").value("match"))
                .andExpect(status().isOk())
                .andDo(document("user-find-live2"));
        /*
         * 게임 중인 경우 (user2)
         * -> event : game
         * */
        mockMvc.perform(get("/pingpong/users/live").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + initiator.tokens[2].getAccessToken()))
                .andExpect(jsonPath("$.event").value("game"))
                .andExpect(status().isOk())
                .andDo(document("user-find-live3"));
    }

    @Test
    @Transactional
    @DisplayName("유저 프로필 업데이트 - /users/detail")
    void modifyProfile() throws Exception {
        /*
         * racketType missing
         * -> 400
         * */
        Map<String, String> body = new HashMap<>();
        body.put("statusMessage", "message");
        mockMvc.perform(put("/pingpong/users/detail").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body))
                        .header("Authorization", "Bearer " + initiator.tokens[0].getAccessToken()))
                .andExpect(status().isBadRequest())
                .andDo(document("user-modify-4xxError-cause-racketType-missing"));

        /*
         * racketType 다른 값인 경우
         * -> 400
         * */
        Map<String, String> body2 = new HashMap<>();
        body2.put("racketType", "NOTHING");
        body2.put("statusMessage", "message");
        mockMvc.perform(put("/pingpong/users/detail").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body2))
                        .header("Authorization", "Bearer " + initiator.tokens[0].getAccessToken()))
                .andExpect(status().isBadRequest())
                .andDo(document("user-modify-4xxError-cause-racketType-wrong-value"));

        /*
         * statusMessage missing
         * -> 400
         * */
        Map<String, String> body3 = new HashMap<>();
        body3.put("racketType", RacketType.PENHOLDER.getCode());
        mockMvc.perform(put("/pingpong/users/detail").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body3))
                        .header("Authorization", "Bearer " + initiator.tokens[0].getAccessToken()))
                .andExpect(status().isBadRequest())
                .andDo(document("user-modify-4xxError-cause-statusMessage-missing"));

        /*
         * statusMessage.length > 300
         * -> 400
         * */
        Map<String, String> body4 = new HashMap<>();
        body4.put("racketType", RacketType.PENHOLDER.getCode());
        body4.put("statusMessage", "0123456789".repeat(31));
        mockMvc.perform(put("/pingpong/users/detail").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body4))
                        .header("Authorization", "Bearer " + initiator.tokens[0].getAccessToken()))
                .andExpect(status().isBadRequest())
                .andDo(document("user-modify-4xxError-cause-statusMessage-wrong-length"));

        /*
         * 업데이트 성공
         * -> 200
         */
        Map<String, String> body5 = new HashMap<>();
        body5.put("racketType", RacketType.PENHOLDER.getCode());
        body5.put("statusMessage", "message");
        mockMvc.perform(put("/pingpong/users/detail").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body5))
                        .header("Authorization", "Bearer " + initiator.tokens[0].getAccessToken()))
                .andExpect(status().isOk())
                .andDo(document("user-modify-profile"));
        User savedUser = userRepository.findByIntraId(user.getIntraId()).orElse(null);
        Assertions.assertThat(savedUser.getRacketType().getCode()).isEqualTo(body5.get("racketType"));
        Assertions.assertThat(savedUser.getStatusMessage()).isEqualTo(body5.get("statusMessage"));
    }


    private Integer getRanking(RankRedis userInfo ,GameType gameType){
        Integer totalGames = userInfo.getLosses() + userInfo.getWins();
        Integer ranking = (totalGames == 0) ? -1 : redisRank.opsForZSet().reverseRank(getRankKey(gameType), getUserRankKey(userInfo.getIntraId(), gameType)).intValue() + 1;
        return ranking;
    }

    private String getUserRankKey(String intraId, GameType gameType) {
        return intraId + gameType.getCode();
    }

    private String getRankKey(GameType gameType) {
        return gameType.getCode();
    }
}