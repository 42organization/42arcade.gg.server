package io.pp.arcade.domain.feedback.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.pp.arcade.RestDocsConfiguration;
import io.pp.arcade.TestInitiator;
import io.pp.arcade.domain.admin.dto.AdminCheckerDto;
import io.pp.arcade.domain.feedback.Feedback;
import io.pp.arcade.domain.feedback.FeedbackRepository;
import io.pp.arcade.domain.user.User;
import io.pp.arcade.global.type.FeedbackType;
import io.pp.arcade.global.type.RoleType;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.redisson.misc.Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureRestDocs
@AutoConfigureMockMvc
@Import(RestDocsConfiguration.class)
public class FeedbackControllerTest {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    TestInitiator initiator;
    @Autowired
    private FeedbackRepository feedbackRepository;

    MockHttpSession session = new MockHttpSession();

    User user1;
    User user2;
    User user3;
    User user4;
    User user11;

    Feedback alreadySavedFeedback1;
    Feedback alreadySavedFeedback2;
    Feedback alreadySavedFeedback3;
    Feedback alreadySavedFeedback4;


    @BeforeEach
    void init() {
        initiator.letsgo();
        user1 = initiator.users[1];
        user2 = initiator.users[2];
        user3 = initiator.users[3];
        user4 = initiator.users[4];
        user11 = initiator.users[11];

        alreadySavedFeedback1 = Feedback.builder()
                .user(user1)
                .category(FeedbackType.BUG)
                .content("게임 결과 페이지가 안보여요")
                .build();

        feedbackRepository.save(alreadySavedFeedback1);

        alreadySavedFeedback2 = Feedback.builder()
                .user(user2)
                .category(FeedbackType.GAMERESULT)
                .content("방금 한 게임 스코어 1:0에서 2:0으로 바꿔주세요!")
                .build();

        feedbackRepository.save(alreadySavedFeedback2);

        alreadySavedFeedback3 = Feedback.builder()
                .user(user3)
                .category(FeedbackType.CHEERS)
                .content("색 조합이 너무 귀여워요. 프론트 짱이다..!")
                .build();

        feedbackRepository.save(alreadySavedFeedback3);

        alreadySavedFeedback4 = Feedback.builder()
                .user(user4)
                .category(FeedbackType.CHEERS)
                .content("서비스 진짜 최곱니다. 이거 하러 클러스터 온다..!")
                .build();

        feedbackRepository.save(alreadySavedFeedback4);
    }

    @Test
    @Transactional
    @DisplayName("건의 사항 요청")
    void FeedbackSave() throws Exception {
        /*
        category가 숫자인 경우
         */

        /*
        category가 다른 string일 경우
         */

        Map<String, String> body = new HashMap<>();
        body.put("category", FeedbackType.GAMERESULT.getCode());
        body.put("content", "방금 한 게임 스코어 0:2에서 2:0으로 바꿔주세요.");
        mockMvc.perform(post("/pingpong/feedback").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + initiator.tokens[0].getAccessToken())
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andDo(document("feedback-save1"));

        Map<String, String> body2 = new HashMap<>();
        body2.put("category", FeedbackType.CHEERS.getCode());
        body2.put("content", "프론트 진짜 기가 막히네요!!!!!!!!!! 너무 옙쁘다!");
        mockMvc.perform(post("/pingpong/feedback").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + initiator.tokens[0].getAccessToken())
                        .content(objectMapper.writeValueAsString(body2)))
                .andExpect(status().isOk())
                .andDo(document("feedback-save2"));

        Map<String, String> body3 = new HashMap<>();
        body3.put("category", FeedbackType.BUG.getCode());
        body3.put("content", "안들어가져요. 서버 터졌나봐!");
        mockMvc.perform(post("/pingpong/feedback").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + initiator.tokens[0].getAccessToken())
                        .content(objectMapper.writeValueAsString(body3)))
                .andExpect(status().isOk())
                .andDo(document("feedback-save3"));

        List<Feedback> feedbacks = feedbackRepository.findAll();
        for(Feedback f: feedbacks) {
            System.out.println(f.getCategory() + ": " + f.getContent());
            System.out.println("->" + f.getIsSolved());
        }

        Map<String, String> body4 = new HashMap<>();
        body4.put("category", FeedbackType.GAMERESULT.getCode());
        body4.put("content", "개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳"
        + "개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳개굳");
        mockMvc.perform(post("/pingpong/feedback").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + initiator.tokens[0].getAccessToken())
                        .content(objectMapper.writeValueAsString(body4)))
                .andExpect(status().isBadRequest())
                .andDo(document("feedback-save-error-cause-content-length-300-up"));

    }

    /* 세션에서 유저 가져와서 어드민인지 확인하는 부분이 잘 안됨. 일단 보류.
    @Test
    @Transactional
    @DisplayName("관리자 update")
    void FeedbackUpdate() throws Exception {

        Assertions.assertThat(alreadySavedFeedback1.getIsSolved()).isEqualTo(false);

        Map<String, String> body = new HashMap<>();
        body.put("feedbackId", alreadySavedFeedback1.getId().toString());
        body.put("isSolved", "true");
        mockMvc.perform(put("/admin/feedback").contentType(MediaType.APPLICATION_JSON)
                        .session(session)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andDo(document("feedback-admin-update"));

        Assertions.assertThat(alreadySavedFeedback1.getIsSolved()).isEqualTo(true);

    }

    @Test
    @Transactional
    @DisplayName("관리자 delete")
    void FeedbackDelete() throws Exception {
        int beforeDeleteSize = feedbackRepository.findAll().size();
        Assertions.assertThat(beforeDeleteSize).isEqualTo(4);
        MockHttpSession session2 = new MockHttpSession();

        session2.setAttribute("user", AdminCheckerDto.builder().intraId(user11.getIntraId()).roleType(RoleType.ADMIN).build());
        session2.getAttribute("user");
        AdminCheckerDto adminCheckerDto = (AdminCheckerDto) session2.getAttribute("user");

        mockMvc.perform(delete("/admin/feedback/{feedbackId}", alreadySavedFeedback2.getId().toString()).contentType(MediaType.APPLICATION_JSON)
                .session(session2))
                .andExpect(status().isOk())
                .andDo(document("feedback-admin-delete"));

        int afterDeleteSize = feedbackRepository.findAll().size();
        Assertions.assertThat(afterDeleteSize).isEqualTo(3);

    }

    @Test
    @Transactional
    @DisplayName("관리자 findAll")
    void FeedbackAll() throws Exception {

        mockMvc.perform(get("/admin/feedback/all").contentType(MediaType.APPLICATION_JSON)
                        .session(session))
                .andExpect(status().isOk())
                .andDo(document("feedback-admin-findAll"));

    }

    @Test
    @Transactional
    @DisplayName("관리자 findByCategory")
    void FeedbackFindByCategory() throws Exception {

        Map<String, String> body = new HashMap<>();
        body.put("content", "제 앞에 벌레 있어요 깍");
        mockMvc.perform(post("/pingpong/feedback/{category}", FeedbackType.BUG.getCode()).contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + initiator.tokens[0].getAccessToken())
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andDo(document("feedback-save-for-test"));

        mockMvc.perform(get("/admin/feedback/{category}", FeedbackType.BUG.getCode()).contentType(MediaType.APPLICATION_JSON)
                        .session(session))
                .andExpect(status().isOk())
                .andDo(document("feedback-admin-findByCategory-bug"));

        mockMvc.perform(get("/admin/feedback/{category}", FeedbackType.CHEERS.getCode()).contentType(MediaType.APPLICATION_JSON)
                        .session(session))
                .andExpect(status().isOk())
                .andDo(document("feedback-admin-findByCategory-cheers"));

    }

    @Test
    @Transactional
    @DisplayName("관리자 findByIsSolved")
    void FeedbackFindByIsSolved() throws Exception {

        mockMvc.perform(post("/admin/feedback/{isSolved}", "false").contentType(MediaType.APPLICATION_JSON)
                        .session(session))
                .andExpect(status().isOk())
                .andDo(document("feedback-admin-findByIsSolved-before-solving-two-feedback"));

        alreadySavedFeedback1.setIsSolved(true);
        alreadySavedFeedback2.setIsSolved(true);

        mockMvc.perform(post("/admin/feedback/{isSolved}", "true").contentType(MediaType.APPLICATION_JSON)
                        .session(session))
                .andExpect(status().isOk())
                .andDo(document("feedback-admin-findByIsSolved-after-solving-two-feedback"));

    }

    @Test
    @Transactional
    @DisplayName("관리자 findByCategoryAndIsSolved")
    void FeedbackFindByCategoryAndIsSolved() throws Exception {

        Map<String, String> body = new HashMap<>();
        body.put("category", FeedbackType.CHEERS.getCode());
        body.put("isSolved", "false");
        mockMvc.perform(post("/admin/feedback/categorized").contentType(MediaType.APPLICATION_JSON)
                        .session(session)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andDo(document("feedback-admin-findByCategoryAndIsSolved-before-solving-two-feedback"));

        alreadySavedFeedback3.setIsSolved(true);
        alreadySavedFeedback4.setIsSolved(true);

        Map<String, String> body2 = new HashMap<>();
        body2.put("category", FeedbackType.CHEERS.getCode());
        body2.put("isSolved", "true");
        mockMvc.perform(post("/admin/feedback/categorized").contentType(MediaType.APPLICATION_JSON)
                        .session(session)
                .content(objectMapper.writeValueAsString(body2)))
                .andExpect(status().isOk())
                .andDo(document("feedback-admin-findByCategoryAndIsSolved-after-solving-two-feedback"));

    }
     */
}
