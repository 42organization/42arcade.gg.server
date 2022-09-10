package io.pp.arcade.domain.noti.controller;

import io.pp.arcade.RestDocsConfiguration;
import io.pp.arcade.TestInitiator;
import io.pp.arcade.domain.game.GameRepository;
import io.pp.arcade.domain.noti.Noti;
import io.pp.arcade.domain.noti.NotiRepository;
import io.pp.arcade.domain.noti.NotiService;
import io.pp.arcade.domain.slot.Slot;
import io.pp.arcade.domain.slot.SlotRepository;
import io.pp.arcade.domain.team.Team;
import io.pp.arcade.domain.team.TeamRepository;
import io.pp.arcade.domain.user.User;
import io.pp.arcade.domain.user.UserRepository;
import io.pp.arcade.global.type.GameType;
import io.pp.arcade.global.type.NotiType;
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

import javax.transaction.Transactional;


import java.util.ArrayList;
import java.util.List;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureRestDocs
@AutoConfigureMockMvc
@Import(RestDocsConfiguration.class)
class NotiControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    NotiRepository notiRepository;

    @Autowired
    NotiService notiService;

    @Autowired
    GameRepository gameRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TeamRepository teamRepository;

    @Autowired
    SlotRepository slotRepository;

    @Autowired
    TestInitiator initiator;

    Slot slot;
    User user1;
    User user2;
    User user3;
    User user4;
    Team team1;
    Team team2;
    List<Noti> notis;

    @BeforeEach
    void init() {
        initiator.letsgo();
        user1 = initiator.users[0];
        user2 = initiator.users[1];
        user3 = initiator.users[2];
        user4 = initiator.users[3];
        slot =initiator.slots[0];
        notis = new ArrayList<Noti>();

        notis.add(notiRepository.save(Noti.builder().type(NotiType.MATCHED).user(user1).isChecked(false).slot(slot).build()));
        notis.add(notiRepository.save(Noti.builder().type(NotiType.CANCELEDBYMAN).user(user1).isChecked(false).slot(slot).build()));
        notis.add(notiRepository.save(Noti.builder().type(NotiType.CANCELEDBYTIME).user(user1).isChecked(false).slot(slot).build()));
        notis.add(notiRepository.save(Noti.builder().type(NotiType.MATCHED).user(user1).isChecked(false).slot(slot).build()));
        notis.add(notiRepository.save(Noti.builder().type(NotiType.IMMINENT).user(user1).isChecked(false).slot(slot).build()));
        notis.add(notiRepository.save(Noti.builder().type(NotiType.ANNOUNCE).user(user1).message("공지사항").isChecked(false).slot(slot).build()));

        slot.setType(GameType.SINGLE);
        team1 = slot.getTeam1();
        team2 = slot.getTeam2();
        team1.setUser1(user1);
        team2.setUser1(user2);
    }

    @Test
    @Transactional
    @DisplayName("알림 조회 - /notifications")
    void notiFindByUser() throws Exception {
        mockMvc.perform(get("/pingpong/notifications").contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + initiator.tokens[0].getAccessToken()))
                .andExpect(jsonPath("$.notifications[0].isChecked").value("false"))
                .andExpect(status().isOk())
                .andDo(document("notifications-find"));

        mockMvc.perform(get("/pingpong/notifications").contentType(MediaType.APPLICATION_JSON)
                   .header("Authorization", "Bearer " + initiator.tokens[0].getAccessToken()))
                .andExpect(jsonPath("$.notifications[0].isChecked").value("true"))
                .andExpect(status().isOk())
                .andDo(document("notifications-find-twice"));
    }

    @Test
    @Transactional
    @DisplayName("알림 삭제 - /notifications/{notiId}")
    void notiRemoveOne() throws Exception{
        mockMvc.perform(delete("/pingpong/notifications/" + notiRepository.findAll().get(0).getId().toString()).contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + initiator.tokens[0].getAccessToken()))
                .andExpect(status().isOk())
                .andDo(document("notification-delete-one"));

        // 관련되지 않은 유저가 삭제하는 경우  -> 400
        mockMvc.perform(delete("/pingpong/notifications/" + notiRepository.findAll().get(1).getId().toString()).contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + initiator.tokens[1].getAccessToken()))
                .andExpect(status().isBadRequest())
                .andDo(document("notification-delete-4xxError-cause-other-user-try"));

        // notiId → 마이너스인 경우  -> 400
        mockMvc.perform(delete("/pingpong/notifications/{notiId}", "-1").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + initiator.tokens[0].getAccessToken()))
                .andExpect(status().isBadRequest())
                .andDo(document("notification-delete-4xxError-cause-notiId-is-negative"));

        // notiId → 존재하지 않는 경우  -> 400
        mockMvc.perform(delete("/pingpong/notifications/{notiId}", "3000").contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + initiator.tokens[0].getAccessToken()))
                .andExpect(status().isBadRequest())
                .andDo(document("notification-delete-4xxError-cause-notiId-don't-exist"));

        // notiId → 숫자가 아닌 경우  -> 400
        mockMvc.perform(delete("/pingpong/notifications/{notiId}", "string").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + initiator.tokens[0].getAccessToken()))
                .andExpect(status().isBadRequest())
                .andDo(document("notification-delete-4xxError-cause-notiId-is-not-integer"));

        // 정상 요청
        mockMvc.perform(get("/pingpong/notifications").contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + initiator.tokens[0].getAccessToken()))
                .andExpect(status().isOk())
                .andDo(document("notification-after-delete-one"));
    }

    @Test
    @Transactional
    @DisplayName("알림 전체 삭제 - /notifications")
    void notiRemoveAll() throws Exception{
        // 모든 알림 삭제
        mockMvc.perform(delete("/pingpong/notifications").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + initiator.tokens[0].getAccessToken()))
                .andExpect(status().isOk())
                .andDo(document("notification-delete-all"));

        mockMvc.perform(get("/pingpong/notifications").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + initiator.tokens[0].getAccessToken()))
                .andExpect(status().isOk())
                .andDo(document("notification-after-delete-all"));
    }
}