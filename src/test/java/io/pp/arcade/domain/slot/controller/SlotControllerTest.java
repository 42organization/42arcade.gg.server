package io.pp.arcade.domain.slot.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.LettuceFutures;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.pp.arcade.RestDocsConfiguration;
import io.pp.arcade.TestInitiator;
import io.pp.arcade.domain.currentmatch.CurrentMatch;
import io.pp.arcade.domain.currentmatch.CurrentMatchRepository;
import io.pp.arcade.domain.game.Game;
import io.pp.arcade.domain.game.GameRepository;
import io.pp.arcade.domain.noti.Noti;
import io.pp.arcade.domain.noti.NotiRepository;
import io.pp.arcade.domain.slot.Slot;
import io.pp.arcade.domain.slot.SlotRepository;
import io.pp.arcade.domain.team.Team;
import io.pp.arcade.domain.user.User;
import io.pp.arcade.global.type.*;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.web.servlet.MockMvc;

import javax.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureRestDocs
@AutoConfigureMockMvc
@Import(RestDocsConfiguration.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@RecordApplicationEvents
class SlotControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private CurrentMatchRepository currentMatchRepository;
    @Autowired
    private GameRepository gameRepository;
    @Autowired
    private SlotRepository slotRepository;

    @Value("${spring.redis.host}")
    String host;
    @Value("${spring.redis.port}")
    String port;


    @Autowired
    private NotiRepository notiRepository;
    @Autowired
    TestInitiator testInitiator;

    Slot[] slots;
    User[] users;
    Team[] teams;
    Slot passedSlot;
    Team passedTeam1;

    @BeforeEach
    void init() {
        testInitiator.letsgo();
        teams = testInitiator.teams;
        users = testInitiator.users;
        slots = testInitiator.slots;

    }

    @Transactional
    void saveSlot(Slot slot, Integer headCount, GameType type, Integer gamePpp, User user) {
        slot.setHeadCount(headCount);
        slot.setType(type);
        slot.setGamePpp(gamePpp);
        if (user != null) {
            currentMatchRepository.save(CurrentMatch.builder().slot(slot).game(null).user(user).matchImminent(false).isMatched(false).build());
        }
    }

    @Transactional
    void saveSlot(Slot slot, Integer headCount, GameType type ,Integer gamePpp) {
        slot.setHeadCount(headCount);
        slot.setType(type);
        slot.setGamePpp(gamePpp);
    }

    @Transactional
    void saveUserPpp(User user, Integer ppp) {
        user.setPpp(ppp);
    }

    @Transactional
    void saveCurrentMatchImminent(CurrentMatch match, Boolean isImminent) {
        match.setMatchImminent(isImminent);
    }

    @Transactional
    void saveCurrentMatchGame(CurrentMatch match, Slot slot) {
        match.setGame(gameRepository.save(Game.builder().team1(slot.getTeam1()).team2(slot.getTeam2()).slot(slot).time(slot.getTime()).season(1).status(StatusType.LIVE).type(slot.getType()).build()));
    }

    @Transactional
    Slot saveSlot(Slot slot) {
        return slotRepository.save(slot);
    }

    @Test
    @Transactional
    @DisplayName("슬롯 조회 - 빈 슬롯")
    void noSlots() throws Exception {
        slotRepository.deleteAll();
        mockMvc.perform(get("/pingpong/match/tables/1/{type}", GameType.SINGLE).contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer 10"))
                //.andExpect(jsonPath("$.matchBoards").isEmpty())
                .andExpect(status().isOk())
                .andDo(document("no-slots"));
    }

    @Test
    @Transactional
    @DisplayName("슬롯 조회 - /match/tables/1/single")
    void slotStatusListSingle() throws Exception {
        /*
         * 단식 - 유저 2명 (풀방)
         * status : close
         * */
        Slot slot = slots[1];
        saveSlot(slot, 2, GameType.SINGLE, 950, null);
        mockMvc.perform(get("/pingpong/match/tables/1/{type}", GameType.SINGLE.getCode()).contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + testInitiator.tokens[10].getAccessToken()))
//                .andExpect(jsonPath("$.matchBoards[0][1].status").value(SlotStatusType.CLOSE.toString()))
                .andDo(document("slot-status-list-when-singleSlot-is-full"));

        /*
         * 단식 - 유저(100p) -> 슬롯(900p) 접근
         * status : close
         * */
        slot = slots[2];
        saveSlot(slot, 1, GameType.SINGLE, 900, null);
        saveUserPpp(users[10], 100);
        mockMvc.perform(get("/pingpong/match/tables/1/{type}", GameType.SINGLE.getCode()).contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + testInitiator.tokens[10].getAccessToken()))
//                .andExpect(jsonPath("$.matchBoards[0].slots[2].status").value(SlotStatusType.CLOSE.toString()))
                .andDo(document("slot-status-list-after-enter-100p-in-900p"));

        /*
         * 단식 - 자신의 슬롯인 경우
         * status : myTable
         * */
        slot = slots[5];
        saveSlot(slot, 1, GameType.DOUBLE, 100, users[10]);
        mockMvc.perform(get("/pingpong/match/tables/1/{type}", GameType.SINGLE.getCode()).contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + testInitiator.tokens[10].getAccessToken()))
//                .andExpect(jsonPath("$.matchBoards[0].slots[5].status").value(SlotStatusType.MYTABLE.toString()))
                .andExpect(status().isOk())
                .andDo(document("slot-status-list-when-i-have-a-slot"));

        /*
         * SINGLE 조회 시, BUNGLE 슬롯에 대한 처리
         * status : close
         * */
        slot = slots[6];
        saveSlot(slot, 1, GameType.DOUBLE, 1000, null);
        mockMvc.perform(get("/pingpong/match/tables/1/{type}", GameType.SINGLE.getCode()).contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + testInitiator.tokens[10].getAccessToken()))
//                .andExpect(jsonPath("$.matchBoards[1].slots[0].status").value(SlotStatusType.CLOSE.toString()))
                .andExpect(status().isOk())
                .andDo(document("slot-status-list-choose-single-look-double"));

        /*
         * 시간이 지난 슬롯
         * status : close
         * */
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime passed = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), 0, 0, 1);
        passedSlot = Slot.builder().tableId(1).team1(teams[0]).team2(teams[1]).time(passed).headCount(0).gamePpp(null).type(GameType.SINGLE).build();
        saveSlot(passedSlot);
        mockMvc.perform(get("/pingpong/match/tables/1/{type}", GameType.SINGLE.getCode()).contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + testInitiator.tokens[10].getAccessToken()))
//                .andExpect(jsonPath("$.matchBoards[0].slots[0].status").value(SlotStatusType.CLOSE.toString()))
                .andExpect(status().isOk())
                .andDo(document("slot-status-list-times-past"));
    }

    @Test
    @Transactional
    @DisplayName("(후 추 구 현) 슬롯 조회 - /match/tables/1/double")
    void slotStatusListDouble() throws Exception {
//        /*
//         * 복식 - 유저 4명 (풀방)
//         * status : close
//         * */
//        Slot slot = slots[3];
//        saveSlot(slot, 4, GameType.DOUBLE, 750, null);
//        mockMvc.perform(get("/pingpong/match/tables/1/{type}", GameType.DOUBLE.getCode()).contentType(MediaType.APPLICATION_JSON)
//                        .header("Authorization", "Bearer 10"))
//                //.andExpect(jsonPath("$.matchBoards[0].slots[3].status").value(SlotStatusType.CLOSE.toString()))
//                .andDo(document("slot-status-list-when-doubleSlot-is-full"));
//        /*
//         * 복식 - 유저(100p) -> 슬롯(900p) 접근
//         * status : close
//         * */
//        slot = slots[4];
//        saveSlot(slot, 3, GameType.DOUBLE, 900, null);
//        saveUserPpp(users[10], 100);
//        mockMvc.perform(get("/pingpong/match/tables/1/{type}", GameType.DOUBLE.getCode()).contentType(MediaType.APPLICATION_JSON)
//                        .header("Authorization", "Bearer " + testInitiator.tokens[10].getAccessToken()))
//                //.andExpect(jsonPath("$.matchBoards[0].slots[4].status").value(SlotStatusType.CLOSE.toString()))
//                .andDo(document("slot-status-list-after-enter-100p-in-900p"));
//        /*
//         * double 조회 시, SINGLE 슬롯에 대한 처리
//         * status : close
//         * */
//        slot = slots[7];
//        saveSlot(slot, 1, GameType.SINGLE, 1000, null);
//        mockMvc.perform(get("/pingpong/match/tables/1/{type}", GameType.DOUBLE.getCode()).contentType(MediaType.APPLICATION_JSON)
//                        .header("Authorization", "Bearer " + testInitiator.tokens[10].getAccessToken()))
//                //.andExpect(jsonPath("$.matchBoards[1].slots[1].status").value(SlotStatusType.CLOSE.toString()))
//                .andExpect(status().isOk())
//                .andDo(document("slot-status-list-choose-double-look-single"));
//        /*
//         * 시간이 지난 슬롯
//         * status : close
//         * */
//        LocalDateTime now = LocalDateTime.now();
//        LocalDateTime passed = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), 0, 0, 1);
//        passedSlot = Slot.builder().tableId(1).team1(teams[0]).team2(teams[1]).time(passed).headCount(0).gamePpp(null).type(GameType.SINGLE).build();
//        saveSlot(passedSlot);
//        mockMvc.perform(get("/pingpong/match/tables/1/{type}", GameType.DOUBLE.getCode()).contentType(MediaType.APPLICATION_JSON)
//                        .header("Authorization", "Bearer " + testInitiator.tokens[10].getAccessToken()))
//                //.andExpect(jsonPath("$.matchBoards[0].slots[0].status").value(SlotStatusType.CLOSE.toString()))
//                .andExpect(status().isOk());
    }

    @Test
    @Transactional
    @DisplayName("슬롯 등록 예외 - /match/tables/1/single")
    void slotAddUserException() throws Exception {
        Map<String, String> body = new HashMap<>();
        Slot slot;

        /*
         * SlotId = -1 (음수인 경우)
         * -> 400
         * */
        body = new HashMap<>();
        body.put("slotId", "-1");
        mockMvc.perform(post("/pingpong/match/tables/1/{type}", GameType.SINGLE.getCode()).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body))
                        .header("Authorization", "Bearer " + testInitiator.tokens[2].getAccessToken()))
                .andExpect(status().isBadRequest())
                        .andDo(document("slot-add-user-4xxError-cause-slotId-is-negative"));

        /*
         * SlotId = null (없는 경우)
         * -> 400
         * */
        mockMvc.perform(post("/pingpong/match/tables/1/{type}", GameType.SINGLE.getCode()).contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + testInitiator.tokens[2].getAccessToken()))
                .andExpect(status().isBadRequest())
                .andDo(document("slot-add-user-4xxError-cause-slotId-is-null"));

        /*
         * SlotId = string (문자열인 경우)
         * -> 400
         * */
        body = new HashMap<>();
        body.put("slotId", "String");
        mockMvc.perform(post("/pingpong/match/tables/1/{type}", GameType.SINGLE.getCode()).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body))
                        .header("Authorization", "Bearer " + testInitiator.tokens[2].getAccessToken()))
                .andExpect(status().isBadRequest())
                .andDo(document("slot-add-user-4xxError-cause-slotId-is-string"));

        /*
         * 단식 - 유저(840p) -> Slot(1000p) 접근
         * -> 400
         * */
        slot = slots[0];
        saveSlot(slot, 1, GameType.SINGLE, 1000);
        saveUserPpp(users[0], 840);
        body.put("slotId", slot.getId().toString());
        mockMvc.perform(post("/pingpong/match/tables/1/{type}", GameType.SINGLE.getCode()).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body))
                        .header("Authorization", "Bearer " + testInitiator.tokens[0].getAccessToken()))
                .andExpect(status().isBadRequest())
                .andDo(document("slot-add-user-4xxError-cause-slotPpp-is-too-high"));

        /*
         * 단식 - 유저(Single) -> Slot(Bungle) 접근
         * -> 400
         * */
        slot = slots[1];
        saveSlot(slot, 1, GameType.DOUBLE, 1000);
        body = new HashMap<>();
        body.put("slotId", slot.getId().toString());
        saveUserPpp(users[1], 1000);
        mockMvc.perform(post("/pingpong/match/tables/1/{type}", GameType.SINGLE.getCode()).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body))
                        .header("Authorization", "Bearer " + testInitiator.tokens[1].getAccessToken()))
                .andExpect(status().isBadRequest())
                .andDo(document("slot-add-user-4xxError-cause-single-try-enter-double"));

        /*
         * 단식 - 풀방 (2/2) 접근
         * -> 400
         * */
        slot = slots[2];
        saveSlot(slot, 2, GameType.SINGLE, 1000);
        saveUserPpp(users[2], 1000);
        body = new HashMap<>();
        body.put("slotId", slot.getId().toString());
        mockMvc.perform(post("/pingpong/match/tables/1/{type}", GameType.SINGLE.getCode()).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body))
                        .header("Authorization", "Bearer " + testInitiator.tokens[2].getAccessToken()))
                .andExpect(status().isBadRequest())
                .andDo(document("slot-add-user-4xxError-cause-try-enter-full"));

        /*
         * 시간이 지난 슬롯 접근
         * -> 400
         * */
        body = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime passed = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), 0, 0, 1);
        passed.minusDays(1);
        passedSlot = Slot.builder().tableId(1).team1(teams[0]).team2(teams[1]).time(passed).headCount(0).gamePpp(null).type(GameType.SINGLE).build();
        passedSlot = saveSlot(passedSlot);
        body.put("slotId", passedSlot.getId().toString());
        mockMvc.perform(post("/pingpong/match/tables/1/{type}", GameType.SINGLE.getCode()).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body))
                        .header("Authorization", "Bearer " + testInitiator.tokens[2].getAccessToken()))
                .andExpect(status().isBadRequest())
                .andDo(document("slot-status-list-times-past"));
    }

    @Test
    @Transactional
    @DisplayName("슬롯 등록 - /match/tables/1/single")
    void slotAddUserSingle() throws Exception {
        Map<String, String> body = new HashMap<>();
        Slot slot;
        User user;
        /*
         * 단식(0/2) - user 등록
         * */
        {
            passedSlot = slots[0];
            passedTeam1 = slots[0].getTeam1();
            user = users[0];
            body.put("slotId", passedSlot.getId().toString());

            mockMvc.perform(get("/pingpong/match/tables/1/{type}", GameType.SINGLE.getCode()).contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + testInitiator.tokens[0].getAccessToken()))
                    //.andExpect(jsonPath("$.matchBoards[0].slots[0].status").value(SlotStatusType.OPEN.toString()))
                    .andExpect(status().isOk())
                    .andDo(document("slot-before-add-user-in-0(2)-check-is-myTable-or-not"));

            mockMvc.perform(post("/pingpong/match/tables/1/{type}", GameType.SINGLE.getCode()).contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body))
                            .header("Authorization", "Bearer " + testInitiator.tokens[0].getAccessToken()))
                    .andExpect(status().isOk())
                    .andDo(document("slot-add-user-in-0(2)"));

            /* 슬롯 확인 */
            mockMvc.perform(get("/pingpong/match/tables/1/{type}", GameType.SINGLE.getCode()).contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + testInitiator.tokens[11].getAccessToken()))
                    .andExpect(status().isOk())
                    .andDo(document("slot-add-user-in-0(2)-check-other-user-view"));
            mockMvc.perform(get("/pingpong/match/tables/1/{type}", GameType.SINGLE.getCode()).contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + testInitiator.tokens[0].getAccessToken()))
                    //.andExpect(jsonPath("$.matchBoards[0].slots[0].status").value(SlotStatusType.OPEN.toString()))
                    .andExpect(status().isOk())
                    .andDo(document("slot-after-add-user-in-0(2)-check-is-myTable-or-not"));
            slot = slotRepository.getById(passedSlot.getId());
            Assertions.assertThat(slot.getGamePpp()).isEqualTo(user.getPpp());
            Assertions.assertThat(slot.getHeadCount()).isEqualTo(1);
            Assertions.assertThat(slot.getType()).isEqualTo(GameType.SINGLE);
            Team team1 = slot.getTeam1();
            Assertions.assertThat(team1.getUser1().getIntraId()).isEqualTo(user.getIntraId());
            Assertions.assertThat(team1.getTeamPpp()).isEqualTo(user.getPpp());
            Assertions.assertThat(team1.getHeadCount()).isEqualTo(1);
            CurrentMatch user1CurrentMatch = currentMatchRepository.findByUser(user).orElse(null);
            Assertions.assertThat(user1CurrentMatch.getSlot().getId()).isEqualTo(slot.getId());
            Assertions.assertThat(user1CurrentMatch.getIsMatched()).isEqualTo(false);
        }
        /*
         * 단식(1/2) - user2 등록
         * */
        {
            slot = slots[0];
            user = users[1];
            Map<String, String> body1 = new HashMap<>();
            body.put("slotId", slot.getId().toString());
            mockMvc.perform(get("/pingpong/match/tables/1/{type}", GameType.SINGLE.getCode()).contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + testInitiator.tokens[1].getAccessToken()))
                    //.andExpect(jsonPath("$.matchBoards[0].slots[0].status").value(SlotStatusType.CLOSE.toString()))
                    .andExpect(status().isOk())
                    .andDo(document("slot-before-add-user-in-1(2)-check-is-myTable-or-not"));

            mockMvc.perform(post("/pingpong/match/tables/1/{type}", GameType.SINGLE.getCode()).contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body))
                            .header("Authorization", "Bearer " + testInitiator.tokens[1].getAccessToken()))
                    .andExpect(status().isOk())
                            .andDo(document("slot-add-user-in-1(2)"));

            mockMvc.perform(get("/pingpong/match/tables/1/{type}", GameType.SINGLE.getCode()).contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + testInitiator.tokens[1].getAccessToken()))
                    //.andExpect(jsonPath("$.matchBoards[0].slots[0].status").value(SlotStatusType.CLOSE.toString()))
                    .andExpect(status().isOk())
                    .andDo(document("slot-after-add-user-in-1(2)-check-is-myTable-or-not"));

            mockMvc.perform(get("/pingpong/match/tables/1/{type}", GameType.SINGLE.getCode()).contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + testInitiator.tokens[11].getAccessToken()))
                    //.andExpect(jsonPath("$.matchBoards[0].slots[0].status").value(SlotStatusType.CLOSE.toString()))
                    .andExpect(status().isOk())
                    .andDo(document("slot-after-add-user-in-1(2)-check-other-user-view"));
            /* 슬롯 확인 */
            slot = slotRepository.getById(slot.getId());
            Assertions.assertThat(slot.getGamePpp()).isEqualTo((users[0].getPpp() + user.getPpp()) / 2);
            Assertions.assertThat(slot.getHeadCount()).isEqualTo(2);
            Assertions.assertThat(slot.getType()).isEqualTo(GameType.SINGLE);

            Team team1 = slot.getTeam1();
            Assertions.assertThat(team1.getUser1().getIntraId()).isEqualTo(users[0].getIntraId());
            Assertions.assertThat(team1.getTeamPpp()).isEqualTo(users[0].getPpp());
            Assertions.assertThat(team1.getHeadCount()).isEqualTo(1);

            Team team2 = slot.getTeam2();
            Assertions.assertThat(team2.getUser1().getIntraId()).isEqualTo(user.getIntraId());
            Assertions.assertThat(team2.getTeamPpp()).isEqualTo(user.getPpp());
            Assertions.assertThat(team2.getHeadCount()).isEqualTo(1);

            CurrentMatch user1CurrentMatch = currentMatchRepository.findByUser(team1.getUser1()).orElse(null);
            CurrentMatch user2CurrentMatch = currentMatchRepository.findByUser(user).orElse(null);
            Assertions.assertThat(user2CurrentMatch.getSlot().getId()).isEqualTo(slot.getId());
            Assertions.assertThat(user1CurrentMatch.getIsMatched()).isEqualTo(true);
            Assertions.assertThat(user2CurrentMatch.getIsMatched()).isEqualTo(true);

            Noti user0Noti = notiRepository.findAllByUser(users[0]).get(0);
            Noti user1Noti = notiRepository.findAllByUser(user).get(0);
            Assertions.assertThat(user0Noti.getType()).isEqualTo(NotiType.MATCHED);
            Assertions.assertThat(user1Noti.getType()).isEqualTo(NotiType.MATCHED);
        }
    }

    @Test
    @Transactional
    @DisplayName("(후 추 구 현) 슬롯 등록 - /match/tables/1/double")
    void slotAddUserDouble() throws Exception {
//        User hakim = users[0];
//        User nheo = users[1];
//        User donghyuk = users[2];
//        User jiyun = users[3];
//
//        flushAll();
//
//        /* - 복식(A: 0/2, B: 0/2) 등록 */
//        {
//            Slot slot = slots[0];
//            Map<String, String> body1 = new HashMap<>();
//            body1.put("slotId", slot.getId().toString());
//            mockMvc.perform(get("/pingpong/match/tables/1/{type}", GameType.DOUBLE.getCode()).contentType(MediaType.APPLICATION_JSON)
//                            .header("Authorization", "Bearer " + testInitiator.tokens[0].getAccessToken()))
//                    //.andExpect(jsonPath("$.matchBoards[0].slots[0].status").value(SlotStatusType.OPEN.toString()))
//                    .andExpect(status().isOk())
//                    .andDo(document("slot-before-add-user-in-0(2)+0(2)-check-is-myTable-or-not"));
//
//            mockMvc.perform(post("/pingpong/match/tables/1/{type}", GameType.DOUBLE.getCode()).contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(body1))
//                            .header("Authorization", "Bearer " + testInitiator.tokens[0].getAccessToken()))
//                    .andExpect(status().isOk())
//                    .andDo(document("slot-add-user-in-0(2)+0(2)"));
//            Slot afterAdd = slotRepository.findById(slots[0].getId()).orElse(null);
//            Team team1 = afterAdd.getTeam1();
//            CurrentMatch hakimMatch = currentMatchRepository.findByUser(hakim).orElse(null);
//            Assertions.assertThat(afterAdd.getGamePpp()).isEqualTo(hakim.getPpp());
//            Assertions.assertThat(afterAdd.getType()).isEqualTo(GameType.DOUBLE);
//            Assertions.assertThat(afterAdd.getHeadCount()).isEqualTo(1);
//
//            mockMvc.perform(get("/pingpong/match/tables/1/{type}", GameType.DOUBLE.getCode()).contentType(MediaType.APPLICATION_JSON)
//                            .header("Authorization", "Bearer " + testInitiator.tokens[0].getAccessToken()))
//                    //.andExpect(jsonPath("$.matchBoards[0].slots[0].status").value(SlotStatusType.OPEN.toString()))
//                    .andExpect(status().isOk())
//                    .andDo(document("slot-after-add-user-in-0(2)+0(2)-check-is-myTable-or-not"));
//
//            mockMvc.perform(get("/pingpong/match/tables/1/{type}", GameType.DOUBLE.getCode()).contentType(MediaType.APPLICATION_JSON)
//                            .header("Authorization", "Bearer " + testInitiator.tokens[11].getAccessToken()))
//                    //.andExpect(jsonPath("$.matchBoards[0].slots[0].status").value(SlotStatusType.OPEN.toString()))
//                    .andExpect(status().isOk())
//                    .andDo(document("slot-after-add-user-in-0(2)+0(2)-check-other-user-view"));
//            Assertions.assertThat(team1.getUser1().getIntraId()).isEqualTo(hakim.getIntraId());
//            Assertions.assertThat(team1.getTeamPpp()).isEqualTo(hakim.getPpp());
//            Assertions.assertThat(team1.getHeadCount()).isEqualTo(1);
//            Assertions.assertThat(hakimMatch.getSlot().getId()).isEqualTo(slot.getId());
//        }
//        /* - 복식(A: 1/2, B: 0/2) 등록 */
//        {
//            Slot slot = slots[0];
//            Map<String, String> body = new HashMap<>();
//            body.put("slotId", slot.getId().toString());
//            mockMvc.perform(get("/pingpong/match/tables/1/{type}", GameType.DOUBLE.getCode()).contentType(MediaType.APPLICATION_JSON)
//                            .header("Authorization", "Bearer " + testInitiator.tokens[1].getAccessToken()))
//                    //.andExpect(jsonPath("$.matchBoards[0].slots[0].status").value(SlotStatusType.OPEN.toString()))
//                    .andExpect(status().isOk())
//                    .andDo(document("slot-before-add-user-in-1(2)+0(2)-check-is-myTable-or-not"));
//
//            mockMvc.perform(post("/pingpong/match/tables/1/{type}", GameType.DOUBLE.getCode()).contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(body))
//                            .header("Authorization", "Bearer " + testInitiator.tokens[1].getAccessToken()))
//                    .andExpect(status().isOk())
//                    .andDo(document("slot-add-user-in-1(2)+0(2)"));
//            Slot afterAdd = slotRepository.findById(slots[0].getId()).orElse(null);
//            Team team1 = afterAdd.getTeam1();
//            CurrentMatch nheoMatch = currentMatchRepository.findByUser(nheo).orElse(null);
//            Assertions.assertThat(afterAdd.getGamePpp()).isEqualTo((nheo.getPpp() + hakim.getPpp()) / 2);
//            Assertions.assertThat(afterAdd.getType()).isEqualTo(GameType.DOUBLE);
//            Assertions.assertThat(afterAdd.getHeadCount()).isEqualTo(2);
//
//            mockMvc.perform(get("/pingpong/match/tables/1/{type}", GameType.DOUBLE.getCode()).contentType(MediaType.APPLICATION_JSON)
//                            .header("Authorization", "Bearer " + testInitiator.tokens[1].getAccessToken()))
//                    //.andExpect(jsonPath("$.matchBoards[0].slots[0].status").value(SlotStatusType.OPEN.toString()))
//                    .andExpect(status().isOk())
//                    .andDo(document("slot-after-add-user-in-1(2)+0(2)-check-is-myTable-or-not"));
//            mockMvc.perform(get("/pingpong/match/tables/1/{type}", GameType.DOUBLE.getCode()).contentType(MediaType.APPLICATION_JSON)
//                            .header("Authorization", "Bearer " + testInitiator.tokens[11].getAccessToken()))
//                    //.andExpect(jsonPath("$.matchBoards[0].slots[0].status").value(SlotStatusType.OPEN.toString()))
//                    .andExpect(status().isOk())
//                    .andDo(document("slot-after-add-user-in-1(2)+0(2)-check-other-user-view"));
//            Assertions.assertThat(team1.getUser2().getIntraId()).isEqualTo(nheo.getIntraId());
//            Assertions.assertThat(team1.getTeamPpp()).isEqualTo((hakim.getPpp() + nheo.getPpp()) / 2);
//            Assertions.assertThat(team1.getHeadCount()).isEqualTo(2);
//            Assertions.assertThat(nheoMatch.getSlot().getId()).isEqualTo(slot.getId());
//        }
//
//    /* - 복식(A: 2/2, B: 0/2) 등록 */
//        {
//            Slot slot = slots[0];
//            Map<String, String> body = new HashMap<>();
//            body.put("slotId", slot.getId().toString());
//            mockMvc.perform(get("/pingpong/match/tables/1/{type}", GameType.DOUBLE.getCode()).contentType(MediaType.APPLICATION_JSON)
//                            .header("Authorization", "Bearer " + testInitiator.tokens[2].getAccessToken()))
//                    //.andExpect(jsonPath("$.matchBoards[0].slots[0].status").value(SlotStatusType.OPEN.toString()))
//                    .andExpect(status().isOk())
//                    .andDo(document("slot-before-add-user-in-2(2)+0(2)-check-is-myTable-or-not"));
//
//            mockMvc.perform(post("/pingpong/match/tables/1/{type}", GameType.DOUBLE.getCode()).contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(body))
//                            .header("Authorization", "Bearer " + testInitiator.tokens[2].getAccessToken()))
//                    .andExpect(status().isOk())
//                    .andDo(document("slot-add-user-in-2(2)+0(2)"));
//
//            Slot afterAdd = slotRepository.findById(slots[0].getId()).orElse(null);
//            Team team2 = afterAdd.getTeam2();
//            CurrentMatch donghyukMatch = currentMatchRepository.findByUser(donghyuk).orElse(null);
//            Assertions.assertThat(afterAdd.getGamePpp()).isEqualTo((donghyuk.getPpp() + hakim.getPpp() + nheo.getPpp()) / 3);
//            Assertions.assertThat(afterAdd.getType()).isEqualTo(GameType.DOUBLE);
//            Assertions.assertThat(afterAdd.getHeadCount()).isEqualTo(3);
//            mockMvc.perform(get("/pingpong/match/tables/1/{type}", GameType.DOUBLE.getCode()).contentType(MediaType.APPLICATION_JSON)
//                            .header("Authorization", "Bearer " + testInitiator.tokens[2].getAccessToken()))
//                    //.andExpect(jsonPath("$.matchBoards[0].slots[0].status").value(SlotStatusType.OPEN.toString()))
//                    .andExpect(status().isOk())
//                    .andDo(document("slot-after-add-user-in-2(2)+0(2)-check-is-myTable-or-not"));
//
//            mockMvc.perform(get("/pingpong/match/tables/1/{type}", GameType.DOUBLE.getCode()).contentType(MediaType.APPLICATION_JSON)
//                            .header("Authorization", "Bearer " + testInitiator.tokens[11].getAccessToken()))
//                    //.andExpect(jsonPath("$.matchBoards[0].slots[0].status").value(SlotStatusType.OPEN.toString()))
//                    .andExpect(status().isOk())
//                    .andDo(document("slot-after-add-user-in-2(2)+0(2)-check-other-user-view"));
//            Assertions.assertThat(team2.getUser1().getIntraId()).isEqualTo(donghyuk.getIntraId());
//            Assertions.assertThat(team2.getTeamPpp()).isEqualTo(donghyuk.getPpp());
//            Assertions.assertThat(team2.getHeadCount()).isEqualTo(1);
//            Assertions.assertThat(donghyukMatch.getSlot().getId()).isEqualTo(slot.getId());
//        }
//    /* - 복식(A: 2/2, B: 1/2) 등록 */
//        {
//            Slot slot = slots[0];
//            Map<String, String> body = new HashMap<>();
//            body.put("slotId", slot.getId().toString());
//            mockMvc.perform(get("/pingpong/match/tables/1/{type}", GameType.DOUBLE.getCode()).contentType(MediaType.APPLICATION_JSON)
//                            .header("Authorization", "Bearer " + testInitiator.tokens[3].getAccessToken()))
//                    //.andExpect(jsonPath("$.matchBoards[0].slots[0].status").value(SlotStatusType.CLOSE.toString()))
//                    .andExpect(status().isOk())
//                    .andDo(document("slot-before-add-user-in-2(2)+1(2)-check-is-myTable-or-not"));
//            mockMvc.perform(post("/pingpong/match/tables/1/{type}", GameType.DOUBLE.getCode()).contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(body))
//                            .header("Authorization", "Bearer " + testInitiator.tokens[3].getAccessToken()))
//                    .andExpect(status().isOk())
//                    .andDo(document("slot-add-user-in-2(2)+1(2)"));
//            Slot afterAdd = slotRepository.findById(slots[0].getId()).orElse(null);
//            Team team2 = afterAdd.getTeam2();
//            CurrentMatch hakimMatch = currentMatchRepository.findByUser(hakim).orElse(null);
//            CurrentMatch nheoMatch = currentMatchRepository.findByUser(nheo).orElse(null);
//            CurrentMatch donghyukMatch = currentMatchRepository.findByUser(donghyuk).orElse(null);
//            CurrentMatch jiyunMatch = currentMatchRepository.findByUser(jiyun).orElse(null);
//            Assertions.assertThat(afterAdd.getGamePpp()).isEqualTo((jiyun.getPpp() + donghyuk.getPpp() + hakim.getPpp() + nheo.getPpp()) / 4);
//            Assertions.assertThat(afterAdd.getType()).isEqualTo(GameType.DOUBLE);
//            Assertions.assertThat(afterAdd.getHeadCount()).isEqualTo(4);
//            mockMvc.perform(get("/pingpong/match/tables/1/{type}", GameType.DOUBLE.getCode()).contentType(MediaType.APPLICATION_JSON)
//                            .header("Authorization", "Bearer " + testInitiator.tokens[3].getAccessToken()))
//                    //.andExpect(jsonPath("$.matchBoards[0].slots[0].status").value(SlotStatusType.CLOSE.toString()))
//                    .andExpect(status().isOk())
//                    .andDo(document("slot-after-add-user-in-2(2)+1(2)-check-is-myTable-or-not"));
//            mockMvc.perform(get("/pingpong/match/tables/1/{type}", GameType.DOUBLE.getCode()).contentType(MediaType.APPLICATION_JSON)
//                            .header("Authorization", "Bearer " + testInitiator.tokens[11].getAccessToken()))
//                    //.andExpect(jsonPath("$.matchBoards[0].slots[0].status").value(SlotStatusType.CLOSE.toString()))
//                    .andExpect(status().isOk())
//                    .andDo(document("slot-after-add-user-in-2(2)+1(2)-check-other-user-view"));
//            Assertions.assertThat(team2.getUser2().getIntraId()).isEqualTo(jiyun.getIntraId());
//            Assertions.assertThat(team2.getTeamPpp()).isEqualTo((jiyun.getPpp() + donghyuk.getPpp()) / 2);
//            Assertions.assertThat(team2.getHeadCount()).isEqualTo(2);
//            Assertions.assertThat(jiyunMatch.getSlot().getId()).isEqualTo(slot.getId());
//            Assertions.assertThat(hakimMatch.getIsMatched()).isEqualTo(true);
//            Assertions.assertThat(nheoMatch.getIsMatched()).isEqualTo(true);
//            Assertions.assertThat(donghyukMatch.getIsMatched()).isEqualTo(true);
//            Assertions.assertThat(jiyunMatch.getIsMatched()).isEqualTo(true);
//            Noti hakimNoti = notiRepository.findAllByUser(hakim).get(0);
//            Noti nheoNoti = notiRepository.findAllByUser(nheo).get(0);
//            Noti donghyukNoti = notiRepository.findAllByUser(donghyuk).get(0);
//            Noti jiyunNoti = notiRepository.findAllByUser(jiyun).get(0);
//            Assertions.assertThat(hakimNoti.getType()).isEqualTo(NotiType.MATCHED);
//            Assertions.assertThat(nheoNoti.getType()).isEqualTo(NotiType.MATCHED);
//            Assertions.assertThat(donghyukNoti.getType()).isEqualTo(NotiType.MATCHED);
//            Assertions.assertThat(jiyunNoti.getType()).isEqualTo(NotiType.MATCHED);
//        }
//        flushAll();
    }

    @Test
    @Transactional
    @DisplayName("슬롯 등록 단식 && 에러 - /match")
    void slotRemoveUserSingle() throws Exception {
        HashMap<String, String> body = new HashMap<String, String>();
       /*
        * 인원이 빈 슬롯에 취소를 요청할 경우
        * -> 400
        * */
        mockMvc.perform(delete("/pingpong/match/slots/1").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + testInitiator.tokens[0].getAccessToken()))
                .andExpect(status().is4xxClientError());

        flushAll();

       /*
        * currentMatch imminent인 경우
        * -> 400
        * */
        {
            Slot slot = slots[0];
            User team1User = users[0];
            body.put("slotId", slot.getId().toString());
            /* slot - user1 등록 */
            mockMvc.perform(post("/pingpong/match/tables/1/{type}", GameType.SINGLE.getCode()).contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body))
                            .header("Authorization", "Bearer " + testInitiator.tokens[0].getAccessToken()))
                    .andExpect(status().isOk());

            /* slot - user2 등록 */
            mockMvc.perform(post("/pingpong/match/tables/1/{type}", GameType.SINGLE.getCode()).contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body))
                            .header("Authorization", "Bearer " + testInitiator.tokens[1].getAccessToken()))
                    .andExpect(status().isOk());

            /* match - Imminent 등록 */
            CurrentMatch currentMatch = currentMatchRepository.findByUser(team1User).orElse(null);
            Assertions.assertThat(currentMatch).isNotNull();
            saveCurrentMatchImminent(currentMatch, true);

            /* slot - user1 등록 취소 */
            mockMvc.perform(delete("/pingpong/match/slots/1").contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + testInitiator.tokens[0].getAccessToken()))
                    .andExpect(status().is4xxClientError())
                    .andDo(document("slot-cancel-imminent-game"));
        }

        flushAll();

        /*
         *  Game 중인 Slot 등록 취소할 경우
         * */
        {
            Slot slot = slots[6];
            User team1User = users[7];
            User team2User = users[8];
            body.put("slotId", slot.getId().toString());
            /* slot - user1 등록 */
            mockMvc.perform(post("/pingpong/match/tables/1/{type}", GameType.SINGLE.getCode()).contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body))
                            .header("Authorization", "Bearer " + testInitiator.tokens[7].getAccessToken()))
                    .andExpect(status().isOk());

            /* slot - user2 등록 */
            mockMvc.perform(post("/pingpong/match/tables/1/{type}", GameType.SINGLE.getCode()).contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body))
                            .header("Authorization", "Bearer " + testInitiator.tokens[8].getAccessToken()))
                    .andExpect(status().isOk());

            /* match - Imminent 등록 */
            CurrentMatch currentMatch = currentMatchRepository.findByUser(team1User).orElse(null);
            CurrentMatch currentMatch2 = currentMatchRepository.findByUser(team2User).orElse(null);
            Assertions.assertThat(currentMatch).isNotNull();
            Assertions.assertThat(currentMatch2).isNotNull();
            saveCurrentMatchImminent(currentMatch, true);
            saveCurrentMatchImminent(currentMatch2, true);
            saveCurrentMatchGame(currentMatch, slot);
            saveCurrentMatchGame(currentMatch2, slot);

            /* slot - user1 등록 취소 imminent라서 취소 불가*/
            mockMvc.perform(delete("/pingpong/match/slots/1").contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + testInitiator.tokens[7].getAccessToken()))
                    .andExpect(status().isBadRequest())
                    .andDo(document("slot-cancel-live-game"));
        }

        flushAll();

        /*
         * 단식(1/2) -> User1 취소
         * */
        {
            Slot slot = slots[1];
            User team1User = users[2];
            body.put("slotId", slot.getId().toString());

            /* slot - user1 등록 */
            mockMvc.perform(post("/pingpong/match/tables/1/{type}", GameType.SINGLE.getCode()).contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body))
                            .header("Authorization", "Bearer " + testInitiator.tokens[2].getAccessToken()))
                    .andExpect(status().isOk())
                    .andDo(document("(single1)slot-add-user1st-in-0(2)"));

            mockMvc.perform(get("/pingpong/match/tables/1/{type}", GameType.SINGLE.getCode()).contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + testInitiator.tokens[2].getAccessToken()))
                    .andExpect(status().isOk())
                    .andDo(document("(single1)slot-before-user1st-cancel-when-status-1(2)-check-is-myTable-or-not"));

            /* slot - user1 등록 취소 */
            mockMvc.perform(delete("/pingpong/match/slots/1").contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + testInitiator.tokens[2].getAccessToken()))
                    .andExpect(status().isOk())
                    .andDo(document("(single1)slot-user1st-cancel-when-status-1(2)"));

            mockMvc.perform(get("/pingpong/match/tables/1/{type}", GameType.SINGLE.getCode()).contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + testInitiator.tokens[2].getAccessToken()))
                    .andExpect(status().isOk())
                    .andDo(document("(single1)slot-after-user1st-cancel-when-status-1(2)-check-is-myTable-or-not"));
            /* User1 매치테이블 조회 */
            CurrentMatch user1CurrentMatch = currentMatchRepository.findByUser(team1User).orElse(null);

            Assertions.assertThat(slot.getTeam1().getUser1()).isEqualTo(null);
            Assertions.assertThat(slot.getTeam1().getHeadCount()).isEqualTo(0);
            Assertions.assertThat(slot.getTeam1().getTeamPpp()).isEqualTo(0);
            Assertions.assertThat(user1CurrentMatch).isEqualTo(null);
            Assertions.assertThat(slot.getGamePpp()).isEqualTo(null);
            Assertions.assertThat(slot.getHeadCount()).isEqualTo(0);
            Assertions.assertThat(slot.getType()).isEqualTo(null);
        }

        flushAll();

        /*
         * 단식(2/2) -> User1 취소
         * */
        {
            Slot slot = slots[2];
            User team1User = users[3];
            User team2User = users[4];
            body.put("slotId", slot.getId().toString());
            /* slot - user1 등록 */
            mockMvc.perform(post("/pingpong/match/tables/1/{type}", GameType.SINGLE.getCode()).contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body))
                            .header("Authorization", "Bearer " + testInitiator.tokens[3].getAccessToken()))
                    .andExpect(status().isOk())
                    .andDo(document("(single2)slot-add-user1st-in-0(2)"));
            mockMvc.perform(post("/pingpong/match/tables/1/{type}", GameType.SINGLE.getCode()).contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body))
                            .header("Authorization", "Bearer " + testInitiator.tokens[4].getAccessToken()))
                    .andExpect(status().isOk())
                    .andDo(document("(single2)slot-add-user2nd-in-1(2)"));

            mockMvc.perform(get("/pingpong/match/tables/1/{type}", GameType.SINGLE.getCode()).contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + testInitiator.tokens[3].getAccessToken()))
                    .andExpect(status().isOk())
                    .andDo(document("(single2)slot-before-user1st-cancel-when-status-2(2)-check-is-myTable-or-not"));
            /* slot - user1 등록 취소 */
            mockMvc.perform(delete("/pingpong/match/slots/1").contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + testInitiator.tokens[3].getAccessToken()))
                    .andExpect(status().isOk())
                    .andDo(document("slot-user1st-cancel-status-2(2)"));
            mockMvc.perform(get("/pingpong/match/tables/1/{type}", GameType.SINGLE.getCode()).contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + testInitiator.tokens[3].getAccessToken()))
                    .andExpect(status().isOk())
                    .andDo(document("(single2)slot-after-user1st-cancel-when-status-2(2)-check-is-myTable-or-not"));


            CurrentMatch user1CurrentMatch = currentMatchRepository.findByUser(team1User).orElse(null);
            CurrentMatch user2CurrentMatch = currentMatchRepository.findByUser(team2User).orElse(null);
            Noti user1Noti = notiRepository.findAllByUser(team1User).get(0); // matched(0), canceled(1)
            Noti user2Noti = notiRepository.findAllByUser(team2User).get(1); // matched(0), canceled(1)

            Assertions.assertThat(slot.getTeam1().getUser1()).isEqualTo(null);
            Assertions.assertThat(slot.getTeam1().getHeadCount()).isEqualTo(0);
            Assertions.assertThat(slot.getTeam1().getTeamPpp()).isEqualTo(0);

            Assertions.assertThat(slot.getTeam2().getUser1()).isEqualTo(team2User);
            Assertions.assertThat(slot.getTeam2().getHeadCount()).isEqualTo(1);
            Assertions.assertThat(slot.getTeam2().getTeamPpp()).isEqualTo(team2User.getPpp());

            Assertions.assertThat(slot.getGamePpp()).isEqualTo(team2User.getPpp());
            Assertions.assertThat(slot.getHeadCount()).isEqualTo(1);
            Assertions.assertThat(slot.getType()).isEqualTo(GameType.SINGLE);

            Assertions.assertThat(user1CurrentMatch).isEqualTo(null);
            Assertions.assertThat(user2CurrentMatch.getIsMatched()).isEqualTo(false);

            Assertions.assertThat(user1Noti.getType()).isEqualTo(NotiType.MATCHED);
            Assertions.assertThat(user2Noti.getType()).isEqualTo(NotiType.CANCELEDBYMAN);
            flushAll();

        }

        /*
         * 단식(2/2) -> User2 취소
         * */
        {
            Slot slot = slots[3];
            User team1User = users[5];
            User team2User = users[6];
            body.put("slotId", slot.getId().toString());
            /* slot - user 등록 */
            mockMvc.perform(post("/pingpong/match/tables/1/{type}", GameType.SINGLE.getCode()).contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body))
                            .header("Authorization", "Bearer " + testInitiator.tokens[5].getAccessToken()))
                    .andExpect(status().isOk())
                    .andDo(document("(single3)slot-add-user1st-in-0(2)"));
            mockMvc.perform(post("/pingpong/match/tables/1/{type}", GameType.SINGLE.getCode()).contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body))
                            .header("Authorization", "Bearer " + testInitiator.tokens[6].getAccessToken()))
                    .andExpect(status().isOk())
                    .andDo(document("(single3)slot-add-user2nd-in-1(2)"));

            mockMvc.perform(get("/pingpong/match/tables/1/{type}", GameType.SINGLE.getCode()).contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + testInitiator.tokens[6].getAccessToken()))
                    .andExpect(status().isOk())
                    .andDo(document("(single3)slot-before-user2nd-cancel-when-status-2(2)-check-is-myTable-or-not"));
            /* slot - team2user 등록 취소 */
            mockMvc.perform(delete("/pingpong/match/slots/1").contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body))
                            .header("Authorization", "Bearer " + testInitiator.tokens[6].getAccessToken()))
                    .andExpect(status().isOk())
                    .andDo(document("(single3)slot-user2nd-cancel-when-status-2(2)"));
            mockMvc.perform(get("/pingpong/match/tables/1/{type}", GameType.SINGLE.getCode()).contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + testInitiator.tokens[6].getAccessToken()))
                    .andExpect(status().isOk())
                    .andDo(document("(single3)slot-after-user2nd-cancel-when-status-2(2)-check-is-myTable-or-not"));
            CurrentMatch user1CurrentMatch = currentMatchRepository.findByUser(team1User).orElse(null);
            CurrentMatch user2CurrentMatch = currentMatchRepository.findByUser(team2User).orElse(null);
            Noti user1Noti = notiRepository.findAllByUser(team1User).get(1);
            Noti user2Noti = notiRepository.findAllByUser(team2User).get(0);

            Assertions.assertThat(slot.getTeam1().getUser1()).isEqualTo(team1User);
            Assertions.assertThat(slot.getTeam1().getHeadCount()).isEqualTo(1);
            Assertions.assertThat(slot.getTeam1().getTeamPpp()).isEqualTo(team1User.getPpp());
            Assertions.assertThat(slot.getTeam2().getUser1()).isEqualTo(null);
            Assertions.assertThat(slot.getTeam2().getHeadCount()).isEqualTo(0);
            Assertions.assertThat(slot.getTeam2().getTeamPpp()).isEqualTo(0);
            Assertions.assertThat(slot.getGamePpp()).isEqualTo(team1User.getPpp());
            Assertions.assertThat(slot.getHeadCount()).isEqualTo(1);
            Assertions.assertThat(slot.getType()).isEqualTo(GameType.SINGLE);
            Assertions.assertThat(user1CurrentMatch.getIsMatched()).isEqualTo(false);
            Assertions.assertThat(user2CurrentMatch).isEqualTo(null);
            Assertions.assertThat(user1Noti.getType()).isEqualTo(NotiType.CANCELEDBYMAN);
            Assertions.assertThat(user2Noti.getType()).isEqualTo(NotiType.MATCHED);
            flushAll();
        }
        flushAll();
    }

//    @Test
//    @Transactional
//    @DisplayName("슬롯 삭제 복식 - /match")
//    void slotRemoveUserDouble() throws Exception {
//        User hakim = users[0];
//        User nheo = users[1];
//        User donghyuk = users[2];
//        User jiyun = users[3];
//        Slot slot = slots[0];
//
//        flushAll();
//
//        Map<String, String> body = new HashMap<>();
//        body.put("slotId", slot.getId().toString());
//        /* slot에 user1,2,3,4 등록 */
//        {
//
//            mockMvc.perform(post("/pingpong/match/tables/1/{type}", GameType.DOUBLE.getCode()).contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(body))
//                            .header("Authorization", "Bearer " + testInitiator.tokens[0].getAccessToken()))
//                    .andExpect(status().isOk())
//                    .andDo(document("(double1)slot-add-user1st"));
//            mockMvc.perform(post("/pingpong/match/tables/1/{type}", GameType.DOUBLE.getCode()).contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(body))
//                            .header("Authorization", "Bearer " + testInitiator.tokens[1].getAccessToken()))
//                    .andExpect(status().isOk())
//                    .andDo(document("(double1)slot-add-user2nd"));
//            mockMvc.perform(post("/pingpong/match/tables/1/{type}", GameType.DOUBLE.getCode()).contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(body))
//                            .header("Authorization", "Bearer " + testInitiator.tokens[2].getAccessToken()))
//                    .andExpect(status().isOk())
//                            .andDo(document("(double1)slot-add-user3rd"));
//            mockMvc.perform(post("/pingpong/match/tables/1/{type}", GameType.DOUBLE.getCode()).contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(body))
//                            .header("Authorization", "Bearer " + testInitiator.tokens[3].getAccessToken()))
//                    .andExpect(status().isOk())
//                    .andDo(document("(double1)slot-add-user4th"));
//            // 넷 중 하나가 봤을 때
//            mockMvc.perform(get("/pingpong/match/tables/1/{type}", GameType.DOUBLE.getCode()).contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(body))
//                            .header("Authorization", "Bearer " + testInitiator.tokens[3].getAccessToken()))
//                    .andExpect(status().isOk())
//                    .andDo(document("(double1)slot-after-add-users-check-is-myTable-or-not"));
//            mockMvc.perform(get("/pingpong/match/tables/1/{type}", GameType.DOUBLE.getCode()).contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(body))
//                            .header("Authorization", "Bearer " + testInitiator.tokens[11].getAccessToken()))
//                    .andExpect(status().isOk())
//                    .andDo(document("(double1)slot-after-add-users-check-other-user-view"));
//        }
//
//        /* slot에서 user4 제거 */
//        {
//            mockMvc.perform(get("/pingpong/match/tables/1/{type}", GameType.DOUBLE.getCode()).contentType(MediaType.APPLICATION_JSON)
//                            .header("Authorization", "Bearer " + testInitiator.tokens[3].getAccessToken()))
//                    //.andExpect(jsonPath("$.matchBoards[0].slots[0].status").value(SlotStatusType.OPEN.toString()))
//                    .andExpect(status().isOk())
//                    .andDo(document("(double2)slot-before-user4th-cancel-when-status-2(2)+2(2)-check-is-myTable-or-not"));
//            mockMvc.perform(delete("/pingpong/match/slots/1").contentType(MediaType.APPLICATION_JSON)
//                    .header("Authorization", "Bearer " + testInitiator.tokens[3].getAccessToken()))
//                    .andDo(document("(double2)slot-user4th-cancel-when-status-2(2)+2(2)"));
//            Assertions.assertThat(slot.getGamePpp()).isEqualTo((hakim.getPpp() + nheo.getPpp() + donghyuk.getPpp()) / 3);
//            mockMvc.perform(get("/pingpong/match/tables/1/{type}", GameType.DOUBLE.getCode()).contentType(MediaType.APPLICATION_JSON)
//                            .header("Authorization", "Bearer " + testInitiator.tokens[11].getAccessToken()))
//                    //.andExpect(jsonPath("$.matchBoards[0].slots[0].status").value(SlotStatusType.OPEN.toString()))
//                    .andExpect(status().isOk())
//                    .andDo(document("(double2)slot-after-user4th-cancel-when-status-2(2)+2(2)-check-other-user-view"));
//            mockMvc.perform(get("/pingpong/match/tables/1/{type}", GameType.DOUBLE.getCode()).contentType(MediaType.APPLICATION_JSON)
//                            .header("Authorization", "Bearer " + testInitiator.tokens[3].getAccessToken()))
//                    //.andExpect(jsonPath("$.matchBoards[0].slots[0].status").value(SlotStatusType.OPEN.toString()))
//                    .andExpect(status().isOk())
//                    .andDo(document("(double2)slot-after-user4th-cancel-when-status-2(2)+2(2)-check-is-myTable-or-not"));
//            Assertions.assertThat(slot.getHeadCount()).isEqualTo(3);
//            Assertions.assertThat(slot.getType()).isEqualTo(GameType.DOUBLE);
//
//            Team team1 = slot.getTeam1();
//            Assertions.assertThat(team1.getUser1().getIntraId()).isEqualTo(hakim.getIntraId());
//            Assertions.assertThat(team1.getUser2().getIntraId()).isEqualTo(nheo.getIntraId());
//            Assertions.assertThat(team1.getTeamPpp()).isEqualTo((hakim.getPpp() + nheo.getPpp()) / 2);
//            Assertions.assertThat(team1.getHeadCount()).isEqualTo(2);
//            Team team2 = slot.getTeam2();
//            Assertions.assertThat(team2.getUser1().getIntraId()).isEqualTo(donghyuk.getIntraId());
//            Assertions.assertThat(team2.getUser2()).isEqualTo(null);
//            Assertions.assertThat(team2.getTeamPpp()).isEqualTo(donghyuk.getPpp());
//            Assertions.assertThat(team2.getHeadCount()).isEqualTo(1);
//
//            CurrentMatch hakimMatch = currentMatchRepository.findByUser(hakim).orElse(null);
//            CurrentMatch nheoMatch = currentMatchRepository.findByUser(nheo).orElse(null);
//            CurrentMatch donghyukMatch = currentMatchRepository.findByUser(donghyuk).orElse(null);
//            CurrentMatch jiyunMatch = currentMatchRepository.findByUser(jiyun).orElse(null);
//            Assertions.assertThat(hakimMatch.getIsMatched()).isEqualTo(false);
//            Assertions.assertThat(nheoMatch.getIsMatched()).isEqualTo(false);
//            Assertions.assertThat(donghyukMatch.getIsMatched()).isEqualTo(false);
//            Assertions.assertThat(jiyunMatch).isEqualTo(null);
//
//            Noti hakimNoti = notiRepository.findAllByUser(hakim).get(0); // matched 알림이 given에서 왔기 때문에 ! 그 다음걸 확인
//            Noti nheoNoti = notiRepository.findAllByUser(nheo).get(1);
//            Noti donghyukNoti = notiRepository.findAllByUser(donghyuk).get(1);
//            Noti jiyunNoti = notiRepository.findAllByUser(jiyun).get(0);
//            Assertions.assertThat(hakimNoti.getType()).isEqualTo(NotiType.MATCHED);
//            Assertions.assertThat(nheoNoti.getType()).isEqualTo(NotiType.CANCELEDBYMAN);
//            Assertions.assertThat(donghyukNoti.getType()).isEqualTo(NotiType.CANCELEDBYMAN);
//            Assertions.assertThat(jiyunNoti.getType()).isEqualTo(NotiType.MATCHED);
//        }
//
//        /* slot에서 user2 제거 */
//        {
//            mockMvc.perform(get("/pingpong/match/tables/1/{type}", GameType.DOUBLE.getCode()).contentType(MediaType.APPLICATION_JSON)
//                            .header("Authorization", "Bearer " + testInitiator.tokens[1].getAccessToken()))
//                    //.andExpect(jsonPath("$.matchBoards[0].slots[0].status").value(SlotStatusType.OPEN.toString()))
//                    .andExpect(status().isOk())
//                    .andDo(document("(double3)slot-before-user2nd-cancel-when-status-2(2)+1(2)-check-is-myTable-or-not"));
//            mockMvc.perform(delete("/pingpong/match/slots/1").contentType(MediaType.APPLICATION_JSON)
//                    .header("Authorization", "Bearer " + testInitiator.tokens[1].getAccessToken()))
//                    .andDo(document("(double3)slot-user2nd-cancel-when-status-2(2)+1(2)"));
//            Assertions.assertThat(slot.getGamePpp()).isEqualTo((hakim.getPpp() + donghyuk.getPpp()) / 2);
//            mockMvc.perform(get("/pingpong/match/tables/1/{type}", GameType.DOUBLE.getCode()).contentType(MediaType.APPLICATION_JSON)
//                            .header("Authorization", "Bearer " + testInitiator.tokens[11].getAccessToken()))
//                    //.andExpect(jsonPath("$.matchBoards[0].slots[0].status").value(SlotStatusType.OPEN.toString()))
//                    .andExpect(status().isOk())
//                    .andDo(document("(double3)slot-after-user2nd-cancel-when-status-2(2)+1(2)-check-other-user-view"));
//            mockMvc.perform(get("/pingpong/match/tables/1/{type}", GameType.DOUBLE.getCode()).contentType(MediaType.APPLICATION_JSON)
//                            .header("Authorization", "Bearer " + testInitiator.tokens[1].getAccessToken()))
//                    //.andExpect(jsonPath("$.matchBoards[0].slots[0].status").value(SlotStatusType.OPEN.toString()))
//                    .andExpect(status().isOk())
//                    .andDo(document("(double3)slot-after-user2nd-cancel-when-status-2(2)+1(2)-check-is-myTable-or-not"));
//            Assertions.assertThat(slot.getHeadCount()).isEqualTo(2);
//            Assertions.assertThat(slot.getType()).isEqualTo(GameType.DOUBLE);
//
//            Team team1 = slot.getTeam1();
//            Assertions.assertThat(team1.getUser1().getIntraId()).isEqualTo(hakim.getIntraId());
//            Assertions.assertThat(team1.getUser2()).isEqualTo(null);
//            Assertions.assertThat(team1.getTeamPpp()).isEqualTo(hakim.getPpp());
//            Assertions.assertThat(team1.getHeadCount()).isEqualTo(1);
//            Team team2 = slot.getTeam2();
//            Assertions.assertThat(team2.getUser1().getIntraId()).isEqualTo(donghyuk.getIntraId());
//            Assertions.assertThat(team2.getUser2()).isEqualTo(null);
//            Assertions.assertThat(team2.getTeamPpp()).isEqualTo(donghyuk.getPpp());
//            Assertions.assertThat(team2.getHeadCount()).isEqualTo(1);
//
//            CurrentMatch hakimMatch = currentMatchRepository.findByUser(hakim).orElse(null);
//            CurrentMatch nheoMatch = currentMatchRepository.findByUser(nheo).orElse(null);
//            CurrentMatch donghyukMatch = currentMatchRepository.findByUser(donghyuk).orElse(null);
//            CurrentMatch jiyunMatch = currentMatchRepository.findByUser(jiyun).orElse(null);
//            Assertions.assertThat(hakimMatch.getIsMatched()).isEqualTo(false);
//            Assertions.assertThat(nheoMatch).isEqualTo(null);
//            Assertions.assertThat(donghyukMatch.getIsMatched()).isEqualTo(false);
//            Assertions.assertThat(jiyunMatch).isEqualTo(null);
//
//            Noti hakimNoti = notiRepository.findAllByUser(hakim).get(1); // matched 알림이 given에서 왔기 때문에 ! 그 다음걸 확인
//            Noti nheoNoti = notiRepository.findAllByUser(nheo).get(1);
//            Noti donghyukNoti = notiRepository.findAllByUser(donghyuk).get(1);
//            Integer jiyunNotiSize = notiRepository.findAllByUser(jiyun).size();
//            Assertions.assertThat(hakimNoti.getType()).isEqualTo(NotiType.CANCELEDBYMAN);
//            Assertions.assertThat(nheoNoti.getType()).isEqualTo(NotiType.CANCELEDBYMAN);
//            Assertions.assertThat(donghyukNoti.getType()).isEqualTo(NotiType.CANCELEDBYMAN);
//            Assertions.assertThat(jiyunNotiSize).isEqualTo(1);
//        }
//
//        flushAll();
//
//        /* slot에 user2 다시 추가 */
//        mockMvc.perform(post("/pingpong/match/tables/1/{type}", GameType.DOUBLE.getCode()).contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(body))
//                        .header("Authorization", "Bearer " + testInitiator.tokens[1].getAccessToken()))
//                .andExpect(status().isOk())
//                .andDo(document("slot-add-user2nd-again"));
//
//
//        /* slot에서 user3 제거 */
//        {
//            mockMvc.perform(get("/pingpong/match/tables/1/{type}", GameType.DOUBLE.getCode()).contentType(MediaType.APPLICATION_JSON)
//                            .header("Authorization", "Bearer " + testInitiator.tokens[2].getAccessToken()))
//                    //.andExpect(jsonPath("$.matchBoards[0].slots[0].status").value(SlotStatusType.OPEN.toString()))
//                    .andExpect(status().isOk())
//                    .andDo(document("(double4)slot-before-user3rd-cancel-when-status-2(2)+1(2)-check-is-myTable-or-not"));
//            mockMvc.perform(delete("/pingpong/match/slots/1").contentType(MediaType.APPLICATION_JSON)
//                    .header("Authorization", "Bearer " + testInitiator.tokens[2].getAccessToken()))
//                    .andDo(document("(double4)slot-user3rd-cancel-when-status-2(2)+1(2)"));
//            Assertions.assertThat(slot.getGamePpp()).isEqualTo((hakim.getPpp() + nheo.getPpp()) / 2);
//            mockMvc.perform(get("/pingpong/match/tables/1/{type}", GameType.DOUBLE.getCode()).contentType(MediaType.APPLICATION_JSON)
//                            .header("Authorization", "Bearer " + testInitiator.tokens[11].getAccessToken()))
//                    //.andExpect(jsonPath("$.matchBoards[0].slots[0].status").value(SlotStatusType.OPEN.toString()))
//                    .andExpect(status().isOk())
//                    .andDo(document("(double4)slot-after-user3rd-cancel-when-status-2(2)+1(2)-check-other-user-view"));
//            mockMvc.perform(get("/pingpong/match/tables/1/{type}", GameType.DOUBLE.getCode()).contentType(MediaType.APPLICATION_JSON)
//                            .header("Authorization", "Bearer " + testInitiator.tokens[2].getAccessToken()))
//                    //.andExpect(jsonPath("$.matchBoards[0].slots[0].status").value(SlotStatusType.OPEN.toString()))
//                    .andExpect(status().isOk())
//                    .andDo(document("(double4)slot-after-user3rd-cancel-when-status-2(2)+1(2)-check-is-myTable-or-not"));
//            Assertions.assertThat(slot.getHeadCount()).isEqualTo(2);
//            Assertions.assertThat(slot.getType()).isEqualTo(GameType.DOUBLE);
//
//            Team team1 = slot.getTeam1();
//            Assertions.assertThat(team1.getUser1().getIntraId()).isEqualTo(hakim.getIntraId());
//            Assertions.assertThat(team1.getUser2().getIntraId()).isEqualTo(nheo.getIntraId());
//            Assertions.assertThat(team1.getTeamPpp()).isEqualTo((hakim.getPpp() + nheo.getPpp()) / 2);
//            Assertions.assertThat(team1.getHeadCount()).isEqualTo(2);
//            Team team2 = slot.getTeam2();
//            Assertions.assertThat(team2.getUser1()).isEqualTo(null);
//            Assertions.assertThat(team2.getUser2()).isEqualTo(null);
//            Assertions.assertThat(team2.getTeamPpp()).isEqualTo(0);
//            Assertions.assertThat(team2.getHeadCount()).isEqualTo(0);
//
//            CurrentMatch hakimMatch = currentMatchRepository.findByUser(hakim).orElse(null);
//            CurrentMatch nheoMatch = currentMatchRepository.findByUser(nheo).orElse(null);
//            CurrentMatch donghyukMatch = currentMatchRepository.findByUser(donghyuk).orElse(null);
//            CurrentMatch jiyunMatch = currentMatchRepository.findByUser(jiyun).orElse(null);
//            Assertions.assertThat(hakimMatch.getIsMatched()).isEqualTo(false);
//            Assertions.assertThat(nheoMatch.getIsMatched()).isEqualTo(false);
//            Assertions.assertThat(donghyukMatch).isEqualTo(null);
//            Assertions.assertThat(jiyunMatch).isEqualTo(null);
//
//            Noti hakimNoti = notiRepository.findAllByUser(hakim).get(1);
//            Noti nheoNoti = notiRepository.findAllByUser(nheo).get(1);
//            Noti donghyukNoti = notiRepository.findAllByUser(donghyuk).get(1);
//            Integer jiyunNotiSize = notiRepository.findAllByUser(jiyun).size();
//            Assertions.assertThat(hakimNoti.getType()).isEqualTo(NotiType.CANCELEDBYMAN);
//            Assertions.assertThat(nheoNoti.getType()).isEqualTo(NotiType.CANCELEDBYMAN);
//            Assertions.assertThat(donghyukNoti.getType()).isEqualTo(NotiType.CANCELEDBYMAN);
//            Assertions.assertThat(jiyunNotiSize).isEqualTo(1);
//        }
//
//        /* slot에서 user1 제거 */
//        {
//            mockMvc.perform(get("/pingpong/match/tables/1/{type}", GameType.DOUBLE.getCode()).contentType(MediaType.APPLICATION_JSON)
//                            .header("Authorization", "Bearer " + testInitiator.tokens[0].getAccessToken()))
//                    //.andExpect(jsonPath("$.matchBoards[0].slots[0].status").value(SlotStatusType.OPEN.toString()))
//                    .andExpect(status().isOk())
//                    .andDo(document("(double5)slot-before-user1st-cancel-when-status-2(2)+0(2)-check-is-myTable-or-not"));
//            mockMvc.perform(delete("/pingpong/match/slots/1").contentType(MediaType.APPLICATION_JSON)
//                    .header("Authorization", "Bearer " + testInitiator.tokens[0].getAccessToken()))
//                    .andDo(document("(double5)slot-user1st-cancel-when-status-2(2)+0(2)"));
//            Assertions.assertThat(slot.getGamePpp()).isEqualTo(nheo.getPpp());
//            mockMvc.perform(get("/pingpong/match/tables/1/{type}", GameType.DOUBLE.getCode()).contentType(MediaType.APPLICATION_JSON)
//                            .header("Authorization", "Bearer " + testInitiator.tokens[11].getAccessToken()))
//                    //.andExpect(jsonPath("$.matchBoards[0].slots[0].status").value(SlotStatusType.OPEN.toString()))
//                    .andExpect(status().isOk())
//                    .andDo(document("(double5)slot-after-user1st-cancel-when-status-2(2)+0(2)-check-other-user-view"));
//            mockMvc.perform(get("/pingpong/match/tables/1/{type}", GameType.DOUBLE.getCode()).contentType(MediaType.APPLICATION_JSON)
//                            .header("Authorization", "Bearer " + testInitiator.tokens[0].getAccessToken()))
//                    //.andExpect(jsonPath("$.matchBoards[0].slots[0].status").value(SlotStatusType.OPEN.toString()))
//                    .andExpect(status().isOk())
//                    .andDo(document("(double5)slot-after-user1st-cancel-when-status-2(2)+0(2)-check-is-myTable-or-not"));
//            Assertions.assertThat(slot.getHeadCount()).isEqualTo(1);
//            Assertions.assertThat(slot.getType()).isEqualTo(GameType.DOUBLE);
//
//            Team team1 = slot.getTeam1();
//            Assertions.assertThat(team1.getUser1()).isEqualTo(null);
//            Assertions.assertThat(team1.getUser2().getIntraId()).isEqualTo(nheo.getIntraId());
//            Assertions.assertThat(team1.getTeamPpp()).isEqualTo(nheo.getPpp());
//            Assertions.assertThat(team1.getHeadCount()).isEqualTo(1);
//            Team team2 = slot.getTeam2();
//            Assertions.assertThat(team2.getUser1()).isEqualTo(null);
//            Assertions.assertThat(team2.getUser2()).isEqualTo(null);
//            Assertions.assertThat(team2.getTeamPpp()).isEqualTo(0);
//            Assertions.assertThat(team2.getHeadCount()).isEqualTo(0);
//
//            CurrentMatch hakimMatch = currentMatchRepository.findByUser(hakim).orElse(null);
//            CurrentMatch nheoMatch = currentMatchRepository.findByUser(nheo).orElse(null);
//            CurrentMatch donghyukMatch = currentMatchRepository.findByUser(donghyuk).orElse(null);
//            CurrentMatch jiyunMatch = currentMatchRepository.findByUser(jiyun).orElse(null);
//            Assertions.assertThat(hakimMatch).isEqualTo(null);
//            Assertions.assertThat(nheoMatch.getIsMatched()).isEqualTo(false);
//            Assertions.assertThat(donghyukMatch).isEqualTo(null);
//            Assertions.assertThat(jiyunMatch).isEqualTo(null);
//
//            Noti hakimNoti = notiRepository.findAllByUser(hakim).get(1);
//            Noti nheoNoti = notiRepository.findAllByUser(nheo).get(1);
//            Integer donghyukNotiSize = notiRepository.findAllByUser(donghyuk).size();
//            Integer jiyunNotiSize = notiRepository.findAllByUser(jiyun).size();
//            Assertions.assertThat(hakimNoti.getType()).isEqualTo(NotiType.CANCELEDBYMAN);
//            Assertions.assertThat(nheoNoti.getType()).isEqualTo(NotiType.CANCELEDBYMAN);
//            Assertions.assertThat(donghyukNotiSize).isEqualTo(2);
//            Assertions.assertThat(jiyunNotiSize).isEqualTo(1);
//        }
//
//        flushAll();
//
//        /* slot에서 user2 제거 --> 빈슬롯 */
//        {
//            mockMvc.perform(get("/pingpong/match/tables/1/{type}", GameType.DOUBLE.getCode()).contentType(MediaType.APPLICATION_JSON)
//                            .header("Authorization", "Bearer " + testInitiator.tokens[1].getAccessToken()))
//                    //.andExpect(jsonPath("$.matchBoards[0].slots[0].status").value(SlotStatusType.OPEN.toString()))
//                    .andExpect(status().isOk())
//                    .andDo(document("(double6)slot-before-user2nd-when-status-1(2)+0(2)-check-is-myTable-or-not"));
//            mockMvc.perform(delete("/pingpong/match/slots/1").contentType(MediaType.APPLICATION_JSON)
//                    .header("Authorization", "Bearer " + testInitiator.tokens[1].getAccessToken()))
//                    .andDo(document("(double6)slot-user2nd-cancel-when-status-1(2)+0(2)"));
//            Assertions.assertThat(slot.getGamePpp()).isEqualTo(null);
//            mockMvc.perform(get("/pingpong/match/tables/1/{type}", GameType.DOUBLE.getCode()).contentType(MediaType.APPLICATION_JSON)
//                            .header("Authorization", "Bearer " + testInitiator.tokens[11].getAccessToken()))
//                    //.andExpect(jsonPath("$.matchBoards[0].slots[0].status").value(SlotStatusType.OPEN.toString()))
//                    .andExpect(status().isOk())
//                    .andDo(document("(double6)slot-after-user2nd-when-status-1(2)+0(2)-check-other-user-view"));
//            mockMvc.perform(get("/pingpong/match/tables/1/{type}", GameType.DOUBLE.getCode()).contentType(MediaType.APPLICATION_JSON)
//                            .header("Authorization", "Bearer " + testInitiator.tokens[1].getAccessToken()))
//                    //.andExpect(jsonPath("$.matchBoards[0].slots[0].status").value(SlotStatusType.OPEN.toString()))
//                    .andExpect(status().isOk())
//                    .andDo(document("(double6)slot-after-user2nd-when-status-1(2)+0(2)-check-is-myTable-or-not"));
//            Assertions.assertThat(slot.getHeadCount()).isEqualTo(0);
//            Assertions.assertThat(slot.getType()).isEqualTo(null);
//
//            Team team1 = slot.getTeam1();
//            Assertions.assertThat(team1.getUser1()).isEqualTo(null);
//            Assertions.assertThat(team1.getUser2()).isEqualTo(null);
//            Assertions.assertThat(team1.getTeamPpp()).isEqualTo(0);
//            Assertions.assertThat(team1.getHeadCount()).isEqualTo(0);
//            Team team2 = slot.getTeam2();
//            Assertions.assertThat(team2.getUser1()).isEqualTo(null);
//            Assertions.assertThat(team2.getUser2()).isEqualTo(null);
//            Assertions.assertThat(team2.getTeamPpp()).isEqualTo(0);
//            Assertions.assertThat(team2.getHeadCount()).isEqualTo(0);
//
//            CurrentMatch hakimMatch = currentMatchRepository.findByUser(hakim).orElse(null);
//            CurrentMatch nheoMatch = currentMatchRepository.findByUser(nheo).orElse(null);
//            CurrentMatch donghyukMatch = currentMatchRepository.findByUser(donghyuk).orElse(null);
//            CurrentMatch jiyunMatch = currentMatchRepository.findByUser(jiyun).orElse(null);
//            Assertions.assertThat(hakimMatch).isEqualTo(null);
//            Assertions.assertThat(nheoMatch).isEqualTo(null);
//            Assertions.assertThat(donghyukMatch).isEqualTo(null);
//            Assertions.assertThat(jiyunMatch).isEqualTo(null);
//
//            Integer hakimNotiSize = notiRepository.findAllByUser(hakim).size();
//            Noti nheoNoti = notiRepository.findAllByUser(nheo).get(1);
//            Integer donghyukNotiSize = notiRepository.findAllByUser(donghyuk).size();
//            Integer jiyunNotiSize = notiRepository.findAllByUser(jiyun).size();
//            Assertions.assertThat(hakimNotiSize).isEqualTo(2);
//            Assertions.assertThat(nheoNoti.getType()).isEqualTo(NotiType.CANCELEDBYMAN);
//            Assertions.assertThat(donghyukNotiSize).isEqualTo(2);
//            Assertions.assertThat(jiyunNotiSize).isEqualTo(1);
//        }
//        flushAll();
//    }

    private void flushAll() {
        RedisClient redisClient = RedisClient.create("redis://"+ host + ":" + port);
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        RedisAsyncCommands<String, String> commands = connection.async();
        commands.flushall();
        boolean result = LettuceFutures.awaitAll(5, TimeUnit.SECONDS);
    }

}