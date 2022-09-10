package io.pp.arcade.domain.flowtest.notiFlowTest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.LettuceFutures;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.pp.arcade.RestDocsConfiguration;
import io.pp.arcade.TestInitiator;
import io.pp.arcade.domain.currentmatch.CurrentMatch;
import io.pp.arcade.domain.game.Game;
import io.pp.arcade.domain.noti.dto.NotiDto;
import io.pp.arcade.domain.noti.dto.NotiFindDto;
import io.pp.arcade.domain.security.jwt.Token;
import io.pp.arcade.domain.slot.Slot;
import io.pp.arcade.domain.slot.dto.SlotRemoveUserDto;
import io.pp.arcade.domain.team.Team;
import io.pp.arcade.domain.user.User;
import org.aspectj.weaver.patterns.ExactTypePattern;
import org.assertj.core.api.Assertions;
import io.pp.arcade.domain.user.dto.UserDto;
import io.pp.arcade.global.type.GameType;
import io.pp.arcade.global.type.StatusType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.transaction.Transactional;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Import(RestDocsConfiguration.class)
@RecordApplicationEvents
public class NotiFlowTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    TestInitiator testInitiator;

    @Value("${spring.redis.host}")
    String host;
    @Value("${spring.redis.port}")
    String port;

    Slot[] slots;
    Team[] teams;
    User[] users;
    Map<String, String> HttpRequestBody;

    @BeforeEach
    void init() {
        testInitiator.letsgo();
        slots = testInitiator.slots;
        teams = testInitiator.teams;
        users = testInitiator.users;
        HttpRequestBody = new HashMap<>();
    }

    @Test
    @Transactional
    void testNotiFlowSingleNormalCases() throws Exception {
        // 싱글 슬롯 생성 후 유저 진입 및 매칭 성사시

        Slot slot = slots[0];
        Team team1 = slots[0].getTeam1();
        Team team2 = slots[0].getTeam2();
        User user1 = users[0];
        User user2 = users[1];
        Token userToken1 = testInitiator.tokens[0];
        Token userToken2 = testInitiator.tokens[1];
        HttpRequestBody.put("slotId", slot.getId().toString());


        // 슬롯에 유저 추가
        mockMvc.perform(post("/pingpong/match/tables/1/{type}", GameType.SINGLE.getCode()).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(HttpRequestBody))
                        .header("Authorization", "Bearer " + userToken1.getAccessToken()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/pingpong/notifications").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + userToken1.getAccessToken()))
                .andExpect(jsonPath("$.notifications.size()").value(0))
                .andExpect(status().isOk())
                .andDo(document("NotiFlowTest-SingleGameType-Positive-MatchedNoti-exist-or-not"));

        mockMvc.perform(post("/pingpong/match/tables/1/{type}", GameType.SINGLE.getCode()).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(HttpRequestBody))
                        .header("Authorization", "Bearer " + userToken2.getAccessToken()))
                .andExpect(status().isOk());

        // 유저 1의 노티 상태 체크 테스트
        mockMvc.perform(get("/pingpong/notifications").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + userToken1.getAccessToken()))
                .andExpect(jsonPath("$.notifications[0].isChecked").value("false"))
                .andExpect(jsonPath("$.notifications[0].type").value("matched"))
                .andExpect(status().isOk())
                .andDo(document("NotiFlowTest-SingleGameType-Positive-MatchedNoti-checked"));

        mockMvc.perform(get("/pingpong/notifications").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + userToken1.getAccessToken()))
                .andExpect(jsonPath("$.notifications[0].isChecked").value("true"))
                .andExpect(jsonPath("$.notifications[0].type").value("matched"))
                .andExpect(status().isOk())
                .andDo(document("NotiFlowTest-SingleGameType-Positive-MatchedNoti-checked-twice"));

        // 유저 2의 노티 상태 체크 테스트
        mockMvc.perform(get("/pingpong/notifications").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + userToken2.getAccessToken()))
                .andExpect(jsonPath("$.notifications[0].isChecked").value("false"))
                .andExpect(jsonPath("$.notifications[0].type").value("matched"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/pingpong/notifications").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + userToken2.getAccessToken()))
                .andExpect(jsonPath("$.notifications[0].isChecked").value("true"))
                .andExpect(jsonPath("$.notifications[0].type").value("matched"))
                .andExpect(status().isOk());

    }

    /* 현재는 더블 badrequest로 처리해놓았기 때문에 당분간 주석
    @Test
    @Transactional
    void testNotiFlowDoubleNormalCases() throws Exception{
        Slot slot = slots[0];
        Team team1 = slots[0].getTeam1();
        Team team2 = slots[0].getTeam2();
        HttpRequestBody.put("slotId", slot.getId().toString());


        // 유저 슬롯에 추가
        mockMvc.perform(post("/pingpong/match/tables/1/{type}", GameType.DOUBLE.getCode()).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(HttpRequestBody))
                        .header("Authorization", "Bearer " + testInitiator.tokens[0].getAccessToken()))
                .andExpect(status().isOk());

        mockMvc.perform(post("/pingpong/match/tables/1/{type}", GameType.DOUBLE.getCode()).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(HttpRequestBody))
                        .header("Authorization", "Bearer " + testInitiator.tokens[1].getAccessToken()))
                .andExpect(status().isOk());

        mockMvc.perform(post("/pingpong/match/tables/1/{type}", GameType.DOUBLE.getCode()).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(HttpRequestBody))
                        .header("Authorization", "Bearer " + testInitiator.tokens[2].getAccessToken()))
                .andExpect(status().isOk());

        mockMvc.perform(post("/pingpong/match/tables/1/{type}", GameType.DOUBLE.getCode()).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(HttpRequestBody))
                        .header("Authorization", "Bearer " + testInitiator.tokens[3].getAccessToken()))
                .andExpect(status().isOk());


        // 유저 1의 노티 상태 체크 테스트
        mockMvc.perform(get("/pingpong/notifications").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + testInitiator.tokens[0].getAccessToken()))
                .andExpect(jsonPath("$.notifications[0].isChecked").value("false"))
                .andExpect(jsonPath("$.notifications[0].type").value("matched"))
                .andExpect(status().isOk())
                .andDo(document("NotiFlowTest-DoubleGameType-Positive-MatchedNoti-checked"));

        mockMvc.perform(get("/pingpong/notifications").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + testInitiator.tokens[0].getAccessToken()))
                .andExpect(jsonPath("$.notifications[0].isChecked").value("true"))
                .andExpect(jsonPath("$.notifications[0].type").value("matched"))
                .andExpect(status().isOk())
                .andDo(document("NotiFlowTest-DoubleGameType-Positive-MatchedNoti-checked-twice"));

        // 유저 2의 노티 상태 체크 테스트
        mockMvc.perform(get("/pingpong/notifications").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + testInitiator.tokens[1].getAccessToken()))
                .andExpect(jsonPath("$.notifications[0].isChecked").value("false"))
                .andExpect(jsonPath("$.notifications[0].type").value("matched"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/pingpong/notifications").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + testInitiator.tokens[1].getAccessToken()))
                .andExpect(jsonPath("$.notifications[0].isChecked").value("true"))
                .andExpect(jsonPath("$.notifications[0].type").value("matched"))
                .andExpect(status().isOk());

        // 유저 3의 노티 상태 체크 테스트
        mockMvc.perform(get("/pingpong/notifications").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + testInitiator.tokens[2].getAccessToken()))
                .andExpect(jsonPath("$.notifications[0].isChecked").value("false"))
                .andExpect(jsonPath("$.notifications[0].type").value("matched"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/pingpong/notifications").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + testInitiator.tokens[2].getAccessToken()))
                .andExpect(jsonPath("$.notifications[0].isChecked").value("true"))
                .andExpect(jsonPath("$.notifications[0].type").value("matched"))
                .andExpect(status().isOk());

        // 유저 4의 노티 상태 체크 테스트
        mockMvc.perform(get("/pingpong/notifications").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + testInitiator.tokens[3].getAccessToken()))
                .andExpect(jsonPath("$.notifications[0].isChecked").value("false"))
                .andExpect(jsonPath("$.notifications[0].type").value("matched"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/pingpong/notifications").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + testInitiator.tokens[3].getAccessToken()))
                .andExpect(jsonPath("$.notifications[0].isChecked").value("true"))
                .andExpect(jsonPath("$.notifications[0].type").value("matched"))
                .andExpect(status().isOk());

    }
     */

    @Test
    @Transactional
    void testNotiFlowSingleCancelledCases() throws Exception {
        Slot slot = slots[0];
        Team team1 = slots[0].getTeam1();
        Team team2 = slots[0].getTeam2();
        User user1 = users[0];
        User user2 = users[1];
        Token userToken1 = testInitiator.tokens[0];
        Token userToken2 = testInitiator.tokens[1];
        HttpRequestBody.put("slotId", slot.getId().toString());

        flushAll();

        // 슬롯에 유저 추가
        mockMvc.perform(post("/pingpong/match/tables/1/{type}", GameType.SINGLE.getCode()).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(HttpRequestBody))
                        .header("Authorization", "Bearer " + userToken1.getAccessToken()))
                .andExpect(status().isOk());

        mockMvc.perform(post("/pingpong/match/tables/1/{type}", GameType.SINGLE.getCode()).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(HttpRequestBody))
                        .header("Authorization", "Bearer " + userToken2.getAccessToken()))
                .andExpect(status().isOk());

        // 유저 1의 노티 상태 체크 테스트
        mockMvc.perform(get("/pingpong/notifications").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + userToken1.getAccessToken()))
                .andExpect(jsonPath("$.notifications[0].isChecked").value("false"))
                .andExpect(jsonPath("$.notifications[0].type").value("matched"))
                .andExpect(status().isOk())
                .andDo(document("NotiFlowTest-SingleGameType-Canceled-Positive-MatchedNoti-checked"));

        mockMvc.perform(get("/pingpong/notifications").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + userToken1.getAccessToken()))
                .andExpect(jsonPath("$.notifications[0].isChecked").value("true"))
                .andExpect(jsonPath("$.notifications[0].type").value("matched"))
                .andExpect(status().isOk())
                .andDo(document("NotiFlowTest-SingleGameType-Canceled-Positive-MatchedNoti-checked-twice"));

        // 유저 2의 노티 상태 체크 테스트
        mockMvc.perform(get("/pingpong/notifications").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + userToken2.getAccessToken()))
                .andExpect(jsonPath("$.notifications[0].isChecked").value("false"))
                .andExpect(jsonPath("$.notifications[0].type").value("matched"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/pingpong/notifications").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + userToken2.getAccessToken()))
                .andExpect(jsonPath("$.notifications[0].isChecked").value("true"))
                .andExpect(jsonPath("$.notifications[0].type").value("matched"))
                .andExpect(status().isOk());

        // 유저 1의 슬롯 취소
        mockMvc.perform(delete("/pingpong/match/slots/{slotId}", slot.getId()).contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + userToken1.getAccessToken()))
                .andExpect(status().isOk())
                .andDo(document("slot-user1st-cancel-when-status-1(2)"));

        // 유저 2의 노티 상태 체크 테스트
        mockMvc.perform(get("/pingpong/notifications").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + userToken2.getAccessToken()))
                .andExpect(jsonPath("$.notifications[0].isChecked").value("false"))
                .andExpect(jsonPath("$.notifications[0].type").value("canceledbyman"))
                .andExpect(status().isOk())
                .andDo(document("NotiFlowTest-SingleGameType-Canceled-Positive-CanceledbymanNoti-checked"));

        mockMvc.perform(get("/pingpong/notifications").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + userToken2.getAccessToken()))
                .andExpect(jsonPath("$.notifications[0].isChecked").value("true"))
                .andExpect(jsonPath("$.notifications[0].type").value("canceledbyman"))
                .andExpect(status().isOk())
                .andDo(document("NotiFlowTest-SingleGameType-Canceled-Positive-CanceledbymanNoti-checked-twice"));

        flushAll();
    }

    /*
    @Test
    @Transactional
    void testNotiFlowDoubleCancelledCases() throws Exception {
        Slot slot = slots[0];
        Team team1 = slots[0].getTeam1();
        Team team2 = slots[0].getTeam2();
        HttpRequestBody.put("slotId", slot.getId().toString());

        flushAll();

        // 유저 슬롯에 추가
        mockMvc.perform(post("/pingpong/match/tables/1/{type}", GameType.DOUBLE.getCode()).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(HttpRequestBody))
                        .header("Authorization", "Bearer " + testInitiator.tokens[0].getAccessToken()))
                .andExpect(status().isOk());

        mockMvc.perform(post("/pingpong/match/tables/1/{type}", GameType.DOUBLE.getCode()).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(HttpRequestBody))
                        .header("Authorization", "Bearer " + testInitiator.tokens[1].getAccessToken()))
                .andExpect(status().isOk());

        mockMvc.perform(post("/pingpong/match/tables/1/{type}", GameType.DOUBLE.getCode()).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(HttpRequestBody))
                        .header("Authorization", "Bearer " + testInitiator.tokens[2].getAccessToken()))
                .andExpect(status().isOk());

        mockMvc.perform(post("/pingpong/match/tables/1/{type}", GameType.DOUBLE.getCode()).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(HttpRequestBody))
                        .header("Authorization", "Bearer " + testInitiator.tokens[3].getAccessToken()))
                .andExpect(status().isOk());


        // 유저 1의 노티 상태 체크 테스트
        mockMvc.perform(get("/pingpong/notifications").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + testInitiator.tokens[0].getAccessToken()))
                .andExpect(jsonPath("$.notifications[0].isChecked").value("false"))
                .andExpect(jsonPath("$.notifications[0].type").value("matched"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/pingpong/notifications").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + testInitiator.tokens[0].getAccessToken()))
                .andExpect(jsonPath("$.notifications[0].isChecked").value("true"))
                .andExpect(jsonPath("$.notifications[0].type").value("matched"))
                .andExpect(status().isOk());

        // 유저 2의 노티 상태 체크 테스트
        mockMvc.perform(get("/pingpong/notifications").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + testInitiator.tokens[1].getAccessToken()))
                .andExpect(jsonPath("$.notifications[0].isChecked").value("false"))
                .andExpect(jsonPath("$.notifications[0].type").value("matched"))
                .andExpect(status().isOk())
                .andDo(document("NotiFlowTest-DoubleGameType-Canceled-Positive-MatchedNoti-checked"));

        mockMvc.perform(get("/pingpong/notifications").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + testInitiator.tokens[1].getAccessToken()))
                .andExpect(jsonPath("$.notifications[0].isChecked").value("true"))
                .andExpect(jsonPath("$.notifications[0].type").value("matched"))
                .andExpect(status().isOk())
                .andDo(document("NotiFlowTest-DoubleGameType-Canceled-Positive-MatchedNoti-checked-twice"));

        // 유저 3의 노티 상태 체크 테스트
        mockMvc.perform(get("/pingpong/notifications").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + testInitiator.tokens[2].getAccessToken()))
                .andExpect(jsonPath("$.notifications[0].isChecked").value("false"))
                .andExpect(jsonPath("$.notifications[0].type").value("matched"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/pingpong/notifications").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + testInitiator.tokens[2].getAccessToken()))
                .andExpect(jsonPath("$.notifications[0].isChecked").value("true"))
                .andExpect(jsonPath("$.notifications[0].type").value("matched"))
                .andExpect(status().isOk());

        // 유저 4의 노티 상태 체크 테스트
        mockMvc.perform(get("/pingpong/notifications").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + testInitiator.tokens[3].getAccessToken()))
                .andExpect(jsonPath("$.notifications[0].isChecked").value("false"))
                .andExpect(jsonPath("$.notifications[0].type").value("matched"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/pingpong/notifications").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + testInitiator.tokens[3].getAccessToken()))
                .andExpect(jsonPath("$.notifications[0].isChecked").value("true"))
                .andExpect(jsonPath("$.notifications[0].type").value("matched"))
                .andExpect(status().isOk());

        // 유저 1의 슬롯 취소
        mockMvc.perform(delete("/pingpong/match/slots/{slotId}", slot.getId()).contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + testInitiator.tokens[0].getAccessToken()))
                .andExpect(status().isOk())
                .andDo(document("slot-user1st-cancel-when-status-1(2)"));

        // 유저 2의 노티 상태 체크 테스트
        mockMvc.perform(get("/pingpong/notifications").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + testInitiator.tokens[1].getAccessToken()))
                .andExpect(jsonPath("$.notifications[0].isChecked").value("false"))
                .andExpect(jsonPath("$.notifications[0].type").value("canceledbyman"))
                .andExpect(status().isOk())
                .andDo(document("NotiFlowTest-DoubleGameType-Canceled-Positive-CanceledbymanNoti-checked"));

        mockMvc.perform(get("/pingpong/notifications").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + testInitiator.tokens[1].getAccessToken()))
                .andExpect(jsonPath("$.notifications[0].isChecked").value("true"))
                .andExpect(jsonPath("$.notifications[0].type").value("canceledbyman"))
                .andExpect(status().isOk())
                .andDo(document("NotiFlowTest-DoubleGameType-Canceled-Positive-CanceledbymanNoti-checked-twice"));

        // 유저 3의 노티 상태 체크 테스트
        mockMvc.perform(get("/pingpong/notifications").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + testInitiator.tokens[2].getAccessToken()))
                .andExpect(jsonPath("$.notifications[0].isChecked").value("false"))
                .andExpect(jsonPath("$.notifications[0].type").value("canceledbyman"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/pingpong/notifications").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + testInitiator.tokens[2].getAccessToken()))
                .andExpect(jsonPath("$.notifications[0].isChecked").value("true"))
                .andExpect(jsonPath("$.notifications[0].type").value("canceledbyman"))
                .andExpect(status().isOk());

        // 유저 4의 노티 상태 체크 테스트
        mockMvc.perform(get("/pingpong/notifications").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + testInitiator.tokens[3].getAccessToken()))
                .andExpect(jsonPath("$.notifications[0].isChecked").value("false"))
                .andExpect(jsonPath("$.notifications[0].type").value("canceledbyman"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/pingpong/notifications").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + testInitiator.tokens[3].getAccessToken()))
                .andExpect(jsonPath("$.notifications[0].isChecked").value("true"))
                .andExpect(jsonPath("$.notifications[0].type").value("canceledbyman"))
                .andExpect(status().isOk());

        flushAll();

    }
    */
    private void flushAll() {
        RedisClient redisClient = RedisClient.create("redis://"+ host + ":" + port);
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        RedisAsyncCommands<String, String> commands = connection.async();
        commands.flushall();
        boolean result = LettuceFutures.awaitAll(5, TimeUnit.SECONDS);
    }

}
