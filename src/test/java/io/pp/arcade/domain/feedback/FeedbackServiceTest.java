package io.pp.arcade.domain.feedback;

import io.pp.arcade.TestInitiator;
import io.pp.arcade.domain.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class FeedbackServiceTest {
    @Autowired
    TestInitiator initiator;
    @Autowired
    private FeedbackService feedbackService;

    User user1;

    @BeforeEach
    void init() {

    }
}
