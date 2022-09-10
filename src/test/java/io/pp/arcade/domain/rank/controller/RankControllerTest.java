package io.pp.arcade.domain.rank.controller;

import io.lettuce.core.LettuceFutures;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.pp.arcade.RestDocsConfiguration;
import io.pp.arcade.TestInitiator;
import io.pp.arcade.domain.rank.RankRedis;
import io.pp.arcade.domain.team.Team;
import io.pp.arcade.domain.user.User;
import io.pp.arcade.domain.user.UserRepository;
import io.pp.arcade.domain.user.dto.UserDto;
import io.pp.arcade.global.redis.Key;
import io.pp.arcade.global.type.GameType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;


import javax.transaction.Transactional;

import java.util.concurrent.TimeUnit;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureRestDocs
@AutoConfigureMockMvc
@Import(RestDocsConfiguration.class)
class RankControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedisTemplate<String, RankRedis> redisRank;

    @Autowired
    private TestInitiator testInitiator;

    @Value("${spring.redis.host}")
    String host;
    @Value("${spring.redis.port}")
    String port;


    User[] users;
    Team[] teams;
    //Slot[] slots;
    @BeforeEach
    void init(){
        flushAll();
        testInitiator.letsgo();
        users = testInitiator.users;
        teams = testInitiator.teams;
        /*
        user = userRepository.save(User.builder().ppp(0).statusMessage("").intraId("donghyuk").eMail("").imageUri("").racketType(RacketType.SHAKEHAND).build());
        user2 = userRepository.save(User.builder().ppp(100).statusMessage("").intraId("nheo").eMail("").imageUri("").racketType(RacketType.SHAKEHAND).build());
        user3 = userRepository.save(User.builder().ppp(100).statusMessage("").intraId("hakim").eMail("").imageUri("").racketType(RacketType.SHAKEHAND).build());
        user4 = userRepository.save(User.builder().ppp(100).statusMessage("").intraId("jiyun").eMail("").imageUri("").racketType(RacketType.SHAKEHAND).build());
        user5 = userRepository.save(User.builder().ppp(400).statusMessage("").intraId("jekim").eMail("").imageUri("").racketType(RacketType.SHAKEHAND).build());
        user6 = userRepository.save(User.builder().ppp(500).statusMessage("").intraId("wochae").eMail("").imageUri("").racketType(RacketType.SHAKEHAND).build());


        users = new HashMap<>();
        for (int i = 0; i < 10; i++){
            String testIntraId = "test" + i;
            User testUser = userRepository.save(User.builder().ppp(500).statusMessage("").intraId(testIntraId).eMail("").imageUri("").racketType(RacketType.SHAKEHAND).build());
            users.put(testIntraId, testUser);
        }
        users.put("donghyuk", user);
        users.put("nheo", user2);
        users.put("hakim", user3);
        users.put("jiyun", user4);
        users.put("jekim", user5);
        users.put("wochae", user6);
        */
        //rankRepository.save(Rank.builder().user(user).ranking(0).losses(0).wins(0).ppp(0).racketType(RacketType.SHAKEHAND).seasonId(0).build());
    }
    @AfterEach
    void end(){
        redisTemplate.unwatch();
        redisRank.unwatch();
    }

    @Test
    @Transactional
    void rankList() throws Exception {
        /* 유저가 존재히지 않을 경우 */
        /*
        mockMvc.perform((get("/pingpong/ranks/single").contentType(MediaType.APPLICATION_JSON)
                        .param("page","1"))
                        .header("Authorization", "Bearer " + 0))
                .andExpect(jsonPath("$.myRank").value(null))
                .andExpect(jsonPath("$.currentPage").value(1))
                .andExpect(jsonPath("$.totalPage").value(1))
                .andExpect(jsonPath("$.rankList").isEmpty())
                .andExpect(status().isOk());
         */

        /* 유저가 존재할 경우 */

        User client = users[0];
        /*
        for (User user : Arrays.stream(users).collect(Collectors.toList())) {
            RankRedis singleRank = RankRedis.from(UserDto.from(user), GameType.SINGLE.getKey());
            RankRedis doubleRank = RankRedis.from(UserDto.from(user), GameType.BUNGLE.getCode());
            redisTemplate.opsForValue().set(getUserKey(user.getIntraId(), GameType.SINGLE), singleRank);
            redisTemplate.opsForValue().set(getUserKey(user.getIntraId(), GameType.BUNGLE), doubleRank);
            redisTemplate.opsForZSet().add(getRankKey(GameType.SINGLE), getUserRankKey(user.getIntraId(), GameType.SINGLE), user.getPpp());
            redisTemplate.opsForZSet().add(getRankKey(GameType.BUNGLE), getUserRankKey(user.getIntraId(), GameType.BUNGLE), user.getPpp());
        }*/

        GameType type = GameType.SINGLE;
        RankRedis userRankInfo = RankRedis.from(UserDto.from(client), type);
        mockMvc.perform((get("/pingpong/ranks/single").contentType(MediaType.APPLICATION_JSON)
                .param("page","1"))
                .header("Authorization", "Bearer " + 0))
                .andExpect(jsonPath("$.myRank").value(getRanking(userRankInfo, type)))
//                .andExpect(jsonPath("$.currentPage").value(1))
//                .andExpect(jsonPath("$.totalPage").value(1))
                .andExpect(jsonPath("$.rankList[0].intraId").value(userRankInfo.getIntraId()))
                .andExpect(jsonPath("$.rankList[0].ppp").value(userRankInfo.getPpp()))
                .andExpect(jsonPath("$.rankList[0].statusMessage").value(userRankInfo.getStatusMessage()))
                .andExpect(jsonPath("$.rankList[0].losses").value(userRankInfo.getLosses()))
                .andExpect(jsonPath("$.rankList[0].wins").value(userRankInfo.getWins()))
                .andExpect(jsonPath("$.rankList[0].winRate").value(userRankInfo.getWinRate()))
                .andExpect(status().isOk())
                .andDo(document("ranking-List"));
    }

    private String getUserRankKey(String intraId, GameType gameType) {
        return intraId + gameType.getCode();
    }

    private String getRankKey(GameType gameType) {
        return gameType.getCode();
    }


    private Integer getRanking(RankRedis userInfo ,GameType gameType){
        Integer totalGames = userInfo.getLosses() + userInfo.getWins();
        Integer ranking= (totalGames == 0) ? -1 : redisRank.opsForZSet().reverseRank(getRankKey(gameType), getUserRankKey(userInfo.getIntraId(), gameType)).intValue() + 1;
        return ranking;
    }

    private void flushAll() {
        RedisClient redisClient = RedisClient.create("redis://"+ host + ":" + port);
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        RedisAsyncCommands<String, String> commands = connection.async();
        commands.flushall();
        boolean result = LettuceFutures.awaitAll(5, TimeUnit.SECONDS);
    }
}