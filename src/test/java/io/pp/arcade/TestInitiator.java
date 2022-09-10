package io.pp.arcade;

import io.pp.arcade.domain.currentmatch.CurrentMatchRepository;
import io.pp.arcade.domain.game.GameRepository;
import io.pp.arcade.domain.noti.NotiRepository;
import io.pp.arcade.domain.pchange.PChangeRepository;
import io.pp.arcade.domain.rank.RankRedis;
import io.pp.arcade.domain.rank.RankRepository;
import io.pp.arcade.domain.season.Season;
import io.pp.arcade.domain.season.SeasonRepository;
import io.pp.arcade.domain.security.jwt.Token;
import io.pp.arcade.domain.security.jwt.TokenRepository;
import io.pp.arcade.domain.slot.Slot;
import io.pp.arcade.domain.slot.SlotRepository;
import io.pp.arcade.domain.team.Team;
import io.pp.arcade.domain.team.TeamRepository;
import io.pp.arcade.domain.user.User;
import io.pp.arcade.domain.user.UserRepository;
import io.pp.arcade.domain.user.dto.UserDto;
import io.pp.arcade.global.redis.Key;
import io.pp.arcade.global.type.GameType;
import io.pp.arcade.global.type.RacketType;
import io.pp.arcade.global.type.RoleType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TestInitiator {
    @Autowired
    CurrentMatchRepository currentMatchRepository;
    @Autowired
    GameRepository gameRepository;
    @Autowired
    NotiRepository notiRepository;
    @Autowired
    PChangeRepository pChangeRepository;
    @Autowired
    RankRepository rankRepository;
    @Autowired
    SeasonRepository seasonRepository;
    @Autowired
    TokenRepository tokenRepository;
    @Autowired
    SlotRepository slotRepository;
    @Autowired
    TeamRepository teamRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    private RedisTemplate redisTemplate;


    public User[] users;
    public Token[] tokens;
    public Team[] teams;
    public Slot[] slots;
    public RankRedis[] ranks;
    public void letsgo() {
        users = new User[12];
        users[0] = userRepository.save(User.builder().intraId("hakim").eMail("hihihoho").imageUri("hakim.jpg").statusMessage("kikikaka").ppp(1040).roleType(RoleType.ADMIN).racketType(RacketType.SHAKEHAND).build());
        users[1] = userRepository.save(User.builder().intraId("nheo").eMail("hihihoho").imageUri("neho.jpg").statusMessage("kikikaka").ppp(1030).roleType(RoleType.USER).racketType(RacketType.SHAKEHAND).build());
        users[2] = userRepository.save(User.builder().intraId("donghyuk").eMail("hihihoho").imageUri("null").statusMessage("kikikaka").ppp(1020).roleType(RoleType.USER).racketType(RacketType.SHAKEHAND).build());
        users[3] = userRepository.save(User.builder().intraId("jiyun").eMail("hihihoho").imageUri("null").statusMessage("kikikaka").ppp(1010).roleType(RoleType.USER).racketType(RacketType.SHAKEHAND).build());
        users[4] = userRepository.save(User.builder().intraId("jekim").eMail("hihihoho").imageUri("jekim.jpg").statusMessage("kikikaka").ppp(990).roleType(RoleType.USER).racketType(RacketType.SHAKEHAND).build());
        users[5] = userRepository.save(User.builder().intraId("wochae").eMail("hihihoho").imageUri("wochae.jpg").statusMessage("kikikaka").ppp(980).roleType(RoleType.USER).racketType(RacketType.SHAKEHAND).build());
        users[6] = userRepository.save(User.builder().intraId("jabae").eMail("hihihoho").imageUri("jabae.jpg").statusMessage("kikikaka").ppp(1000).roleType(RoleType.USER).racketType(RacketType.SHAKEHAND).build());
        users[7] = userRepository.save(User.builder().intraId("jihyukim").eMail("hihihoho").imageUri("jihyukim.jpg").statusMessage("kikikaka").ppp(992).roleType(RoleType.USER).racketType(RacketType.SHAKEHAND).build());
        users[8] = userRepository.save(User.builder().intraId("daekim").eMail("hihihoho").imageUri("null").statusMessage("kikikaka").ppp(996).roleType(RoleType.USER).racketType(RacketType.SHAKEHAND).build());
        users[9] = userRepository.save(User.builder().intraId("sujpark").eMail("hihihoho").imageUri("null").statusMessage("kikikaka").ppp(994).roleType(RoleType.USER).racketType(RacketType.SHAKEHAND).build());
        users[10] = userRepository.save(User.builder().intraId("kipark").eMail("hihihoho").imageUri("null").statusMessage("kikikaka").ppp(100).roleType(RoleType.USER).racketType(RacketType.SHAKEHAND).build());
        users[11] = userRepository.save(User.builder().intraId("jujeon").eMail("hihi").imageUri("null").statusMessage("kiki").ppp(1000).roleType(RoleType.ADMIN).racketType(RacketType.DUAL).build());
        tokens = new Token[12];
        for (Integer i = 0; i < 12; i++) {
            tokens[i] = tokenRepository.save(new Token(users[i], i.toString(), i.toString()));
        }

        ranks = new RankRedis[users.length * GameType.values().length];
        List<User> userList = Arrays.stream(users).collect(Collectors.toList());
        for (User user : userList) {
            int idx = userList.indexOf(user);
            RankRedis singleRank = RankRedis.from(UserDto.from(user), GameType.SINGLE);
            RankRedis doubleRank = RankRedis.from(UserDto.from(user), GameType.DOUBLE);

            ranks[idx] = singleRank;
            ranks[users.length + idx] = doubleRank;
            redisTemplate.opsForValue().set(getUserKey(user.getIntraId(), GameType.SINGLE), singleRank);
            redisTemplate.opsForValue().set(getUserKey(user.getIntraId(), GameType.DOUBLE), doubleRank);

            redisTemplate.opsForZSet().add(getRankKey(GameType.SINGLE), getUserRankKey(user.getIntraId(), GameType.SINGLE), user.getPpp());
            redisTemplate.opsForZSet().add(getRankKey(GameType.DOUBLE), getUserRankKey(user.getIntraId(), GameType.DOUBLE), user.getPpp());
        }

        teams = new Team[8];
        teams[0] = teamRepository.save(Team.builder().user1(users[0]).user2(null).teamPpp(1004).headCount(1).score(0).build());
        teams[1] = teamRepository.save(Team.builder().user1(users[1]).user2(null).teamPpp(1030).headCount(1).score(0).build());
        teams[2] = teamRepository.save(Team.builder().user1(users[2]).user2(null).teamPpp(1020).headCount(1).score(0).build());
        teams[3] = teamRepository.save(Team.builder().user1(users[3]).user2(null).teamPpp(1010).headCount(1).score(0).build());
        teams[4] = teamRepository.save(Team.builder().user1(users[4]).user2(users[5]).teamPpp(985).headCount(2).score(0).build());
        teams[5] = teamRepository.save(Team.builder().user1(users[6]).user2(users[7]).teamPpp(996).headCount(2).score(0).build());
        teams[6] = teamRepository.save(Team.builder().user1(users[8]).user2(users[9]).teamPpp(995).headCount(2).score(0).build());
        teams[7] = teamRepository.save(Team.builder().user1(users[10]).user2(users[0]).teamPpp(1006).headCount(2).score(0).build());

        slots = new Slot[18];
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
        for (int i = 0; i < 18; i++) {
            Team t1 = teamRepository.save(Team.builder().teamPpp(0).headCount(0).score(0).build());
            Team t2 = teamRepository.save(Team.builder().teamPpp(0).headCount(0).score(0).build());
            LocalDateTime time = LocalDateTime.of(tomorrow.getYear(), tomorrow.getMonth(), tomorrow.getDayOfMonth(),
                    15 + i / 6, (i * 10) % 60, 0); // 3시부터 10분 간격으로 18개 슬롯 생성
            slots[i] = slotRepository.save(Slot.builder().team1(t1).team2(t2).tableId(1).headCount(0).time(time).build());
        }

        Season testSeason = seasonRepository.save(Season.builder().seasonName("Test").startTime(LocalDateTime.now().minusYears(1)).endTime(LocalDateTime.now().plusYears(1)).startPpp(1000).pppGap(150).build());
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
}
