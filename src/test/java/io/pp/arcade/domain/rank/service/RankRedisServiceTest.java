package io.pp.arcade.domain.rank.service;

import io.lettuce.core.LettuceFutures;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.pp.arcade.TestInitiator;
import io.pp.arcade.domain.rank.RankRedis;
import io.pp.arcade.domain.rank.RankRepository;
import io.pp.arcade.domain.rank.dto.*;
import io.pp.arcade.domain.user.User;
import io.pp.arcade.domain.user.dto.UserDto;
import io.pp.arcade.global.redis.Key;
import io.pp.arcade.global.type.GameType;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;

import javax.transaction.Transactional;
import java.util.*;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class RankRedisServiceTest {

    @Autowired
    private RankRepository rankRepository;
    @Autowired
    private RankService rankService;
    @Autowired
    private RedisTemplate<String, RankRedis> redisTemplate;
    @Autowired
    private RankRedisService rankRedisService;
    @Autowired
    private TestInitiator testInitiator;
    @Value("${spring.redis.host}")
    String host;
    @Value("${spring.redis.port}")
    String port;

    @BeforeEach
    void init (){
        flushAll();
    }

    @Test
    @Transactional
    @DisplayName("모든 랭킹 조회 - Redis 데이터 Xx")
    void findRankListWhenEmpty() {
        // given
        flushAll();

        Pageable pageable = PageRequest.of(0, 10);
        RankFindListDto rankFindListDto = RankFindListDto.builder().gameType(GameType.SINGLE).pageable(pageable).count(10).build();

        // when
        RankListDto rankList = rankRedisService.findRankList(rankFindListDto);

        // then
        Assertions.assertThat(rankList.getRankList()).isEmpty();
        Assertions.assertThat(rankList.getRankList()).isEmpty();
    }

    @Test
    @Transactional
    @DisplayName("모든 랭킹 조회")
    void findRankList() {
        // given
        testInitiator.letsgo();
        Pageable pageable = PageRequest.of(0, 10);
        RankFindListDto rankFindListDto = RankFindListDto.builder().gameType(GameType.SINGLE).pageable(pageable).count(10).build();

        // when
        RankListDto rankList = rankRedisService.findRankList(rankFindListDto);

        // then
        Assertions.assertThat(rankList.getRankList()).isNotEmpty();
        Assertions.assertThat(rankList.getRankList().size()).isEqualTo(10);
//        Assertions.assertThat(rankList.getTotalPage()).isEqualTo(2);
//        Assertions.assertThat(rankList.getCurrentPage()).isEqualTo(1);
    }

    @Test
    @Transactional
    @DisplayName("유저 랭크 조회")
    void findRankById() {
        // given
        testInitiator.letsgo();
        User user =  testInitiator.users[0];
        RankFindDto rankFindDto = RankFindDto.builder().gameType(GameType.SINGLE).intraId(user.getIntraId()).build();

        // when
        RankUserDto rankUserDto = rankRedisService.findRankById(rankFindDto);

        // then
        Assertions.assertThat(rankUserDto.getIntraId()).isEqualTo(user.getIntraId());
    }

    @Test
    @Transactional
    @DisplayName("유저 랭크 조회 - Redis 데이터 X")
    void findRankByIdWhenEmpty() {
        // given
        RankFindDto rankFindDto = RankFindDto.builder().gameType(GameType.SINGLE).intraId("hakim").build();

        // when
        RankUserDto rankUserDto = rankRedisService.findRankById(rankFindDto);

        // then
        Assertions.assertThat(rankUserDto).isNull();
    }

    @Test
    @Transactional
    @DisplayName("랭크 점수 수정 - Single, Win")
    void modifyUserPppWhenWin() {
        // given
        testInitiator.letsgo();
        User user =  testInitiator.users[0];
        RankModifyDto modifyDto = RankModifyDto.builder().intraId(user.getIntraId()).Ppp(1010).isWin(true).gameType(GameType.SINGLE).build();

        // when
        rankRedisService.modifyUserPpp(modifyDto);

        // then
        RankRedis rankRedis =  redisTemplate.opsForValue().get(getUserKey(user.getIntraId(), GameType.SINGLE));
        Assertions.assertThat(rankRedis.getIntraId()).isEqualTo(user.getIntraId());
        Assertions.assertThat(rankRedis.getPpp()).isEqualTo(1010);
        Assertions.assertThat(rankRedis.getWins()).isEqualTo(1);
    }

    @Test
    @Transactional
    @DisplayName("랭크 점수 수정 - Single, Loss")
    void modifyUserPppWhenLoss() {
        // given
        testInitiator.letsgo();
        User user =  testInitiator.users[0];
        RankModifyDto modifyDto = RankModifyDto.builder().intraId(user.getIntraId()).Ppp(1010).isWin(false).gameType(GameType.SINGLE).build();

        // when
        rankRedisService.modifyUserPpp(modifyDto);

        // then
        RankRedis rankRedis =  redisTemplate.opsForValue().get(getUserKey(user.getIntraId(), GameType.SINGLE));
        Assertions.assertThat(rankRedis.getIntraId()).isEqualTo(user.getIntraId());
        Assertions.assertThat(rankRedis.getPpp()).isEqualTo(1010);
        Assertions.assertThat(rankRedis.getLosses()).isEqualTo(1);
    }

    @Test
    @Transactional
    @DisplayName("랭크 상태 메시지 변경")
    void modifyRankStatusMessage() {
        // given
        testInitiator.letsgo();
        User user =  testInitiator.users[0];
        RankModifyStatusMessageDto modifyDto = RankModifyStatusMessageDto.builder().intraId(user.getIntraId()).statusMessage("change").build();

        // when
        rankRedisService.modifyRankStatusMessage(modifyDto);

        // then
        RankRedis singleRank =  redisTemplate.opsForValue().get(getUserKey(user.getIntraId(), GameType.SINGLE));
        RankRedis bungleRank =  redisTemplate.opsForValue().get(getUserKey(user.getIntraId(), GameType.DOUBLE));
        Assertions.assertThat(singleRank.getStatusMessage()).isEqualTo("change");
        Assertions.assertThat(bungleRank.getStatusMessage()).isEqualTo("change");
    }

    @Test
    @Transactional
    @DisplayName("모든 유저 랭크 조회")
    void findRankAll() {
        // given
        testInitiator.letsgo();
        List<RankRedis> rankList = Arrays.asList(testInitiator.ranks);
        Map<String, RankRedis> rankMap = new HashMap<>();
        for (RankRedis rank : rankList) {
            rankMap.put(rank.getIntraId() + rank.getGameType().getCode(), rank);
        }

        // when
        List<RankRedisDto> rankAll = rankRedisService.findRankAll();

        // then
        Assertions.assertThat(rankAll).isNotEmpty();
        for(RankRedisDto rankRedis : rankAll) {
            RankRedis savedRankRedis =  rankMap.get(rankRedis.getIntraId() + rankRedis.getGameType().getCode());
            Assertions.assertThat(rankRedis.getIntraId()).isEqualTo(savedRankRedis.getIntraId());
            Assertions.assertThat(rankRedis.getPpp()).isEqualTo(savedRankRedis.getPpp());
            Assertions.assertThat(rankRedis.getWins()).isEqualTo(savedRankRedis.getWins());
            Assertions.assertThat(rankRedis.getLosses()).isEqualTo(savedRankRedis.getLosses());
            Assertions.assertThat(rankRedis.getStatusMessage()).isEqualTo(savedRankRedis.getStatusMessage());
        }
    }

    @Test
    @Transactional
    @DisplayName("모든 유저 랭크 조회 - Redis 데이터 X")
    void findRankAllWhenEmpty() {
        flushAll();

        // when
        List<RankRedisDto> rankAll = rankRedisService.findRankAll();

        // then
        Assertions.assertThat(rankAll).isEmpty();
    }

    @Test
    @Transactional
    @DisplayName("빈 랭크 체크")
    void isEmpty() {
        flushAll();
        // when
        Boolean isEmpty = rankRedisService.isEmpty();

        // then
        Assertions.assertThat(isEmpty).isTrue();
    }

    @Test
    @Transactional
    @DisplayName("빈 랭크 체크 - 데이터가 있을 경우")
    void isEmptyWhenDataExists() {
        testInitiator.letsgo();
        // when
        Boolean isEmpty = rankRedisService.isEmpty();
        // then
        Assertions.assertThat(isEmpty).isFalse();
    }

    @Test
    @Transactional
    void saveAll() {
        // given
        testInitiator.letsgo();
        flushAll();

        User[] users = testInitiator.users;
        List<UserDto> userDtos = new ArrayList<>();
        for (User user : users){
            userDtos.add(UserDto.from(user));
        }

        List<RankRedis> rankList = Arrays.asList(testInitiator.ranks);
        for (RankRedis rankRedis : rankList) {
            int idx = rankList.indexOf(rankRedis);
            rankRedis.setStatusMessage(String.valueOf(idx));
        }

        /* single */
        {
            List<RankDto> rankDtos = new ArrayList<>();
            for (int i = 0; i < users.length; i++) {
                rankDtos.add(RankDto.builder().ranking(i).ppp(1100).losses(i).wins(i).gameType(GameType.SINGLE).racketType(userDtos.get(i).getRacketType()).seasonId(i).user(userDtos.get(i)).build());
            }

            HashMap<String, RankDto> rankDtoMap = new HashMap<>();
            for (int i = 0; i < users.length; i++) {
                rankDtoMap.put(rankDtos.get(i).getUser().getIntraId() + rankDtos.get(i).getGameType().getCode(), rankDtos.get(i));
            }

            // when
            rankRedisService.saveAll(rankDtos);

            // then
            List<RankRedis> singleList = redisTemplate.opsForValue().multiGet(redisTemplate.keys(Key.RANK_USER + "*" + GameType.SINGLE.getCode()));
            Assertions.assertThat(singleList).isNotEmpty();
            for (RankRedis rankRedis : singleList) {
                RankDto rankDto = rankDtoMap.get(rankRedis.getIntraId() + GameType.SINGLE.getCode());
                Assertions.assertThat(rankRedis.getPpp()).isEqualTo(rankDto.getPpp());
                Assertions.assertThat(rankRedis.getWins()).isEqualTo(rankDto.getWins());
                Assertions.assertThat(rankRedis.getLosses()).isEqualTo(rankDto.getLosses());
            }
        }

        /* bungle */
        {
            List<RankDto> rankDtos = new ArrayList<>();
            for (int i = 0; i < users.length; i++) {
                rankDtos.add(RankDto.builder().ranking(i).ppp(1100).losses(i).wins(i).gameType(GameType.DOUBLE).racketType(userDtos.get(i).getRacketType()).seasonId(i).user(userDtos.get(i)).build());
            }

            HashMap<String, RankDto> rankDtoMap = new HashMap<>();
            for (int i = 0; i < users.length; i++) {
                rankDtoMap.put(rankDtos.get(i).getUser().getIntraId() + rankDtos.get(i).getGameType().getCode(), rankDtos.get(i));
            }

            // when
            rankRedisService.saveAll(rankDtos);

            List<RankRedis> bungleList = redisTemplate.opsForValue().multiGet(redisTemplate.keys(Key.RANK_USER + "*" + GameType.DOUBLE.getCode()));
            Assertions.assertThat(bungleList).isNotEmpty();
            for (RankRedis rankRedis : bungleList) {
                RankDto rankDto = rankDtoMap.get(rankRedis.getIntraId() + GameType.DOUBLE.getCode());
                Assertions.assertThat(rankRedis.getPpp()).isEqualTo(rankDto.getPpp());
                Assertions.assertThat(rankRedis.getWins()).isEqualTo(rankDto.getWins());
                Assertions.assertThat(rankRedis.getLosses()).isEqualTo(rankDto.getLosses());
            }
        }
    }

    private void flushAll() {
        RedisClient redisClient = RedisClient.create("redis://"+ host + ":" + port);
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        RedisAsyncCommands<String, String> commands = connection.async();
        commands.flushall();
        boolean result = LettuceFutures.awaitAll(5, TimeUnit.SECONDS);
    }

    private String getUserKey(String intraId, GameType gameType) {
        return Key.RANK_USER + intraId + gameType.getCode();
    }

    private String getUserRankKey(String intraId, GameType gameType) {
        return intraId + gameType.getCode();
    }

    private String getRankKey(GameType gameType) {
        return gameType.getCode();
    }


    private Integer getRanking(RankRedis userInfo ,GameType gameType){
        Integer totalGames = userInfo.getLosses() + userInfo.getWins();
        Integer ranking= (totalGames == 0) ? -1 : redisTemplate.opsForZSet().reverseRank(getRankKey(gameType), getUserRankKey(userInfo.getIntraId(), gameType)).intValue() + 1;
        return ranking;
    }
}