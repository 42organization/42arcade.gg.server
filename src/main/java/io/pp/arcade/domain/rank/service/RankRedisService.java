package io.pp.arcade.domain.rank.service;

import io.jsonwebtoken.lang.Collections;
import io.pp.arcade.domain.admin.dto.create.RankCreateRequestDto;
import io.pp.arcade.domain.admin.dto.delete.RankDeleteDto;
import io.pp.arcade.domain.admin.dto.update.RankUpdateRequestDto;
import io.pp.arcade.domain.user.dto.UserDto;
import io.pp.arcade.global.redis.Key;
import io.pp.arcade.domain.rank.RankRedis;
import io.pp.arcade.domain.rank.dto.*;
import io.pp.arcade.global.type.GameType;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.pp.arcade.global.type.GameType.DOUBLE;
import static io.pp.arcade.global.type.GameType.SINGLE;


@Service
@AllArgsConstructor
public class RankRedisService implements RankNTService {
    private final RedisTemplate<String, String> redisRank;
    private final RedisTemplate<String, RankRedis> redisUser;

    @Transactional
    public RankListDto findRankList(RankFindListDto rankFindListDto) {
        int pageNumber = rankFindListDto.getPageable().getPageNumber();
        int count =rankFindListDto.getCount();
        GameType type = rankFindListDto.getGameType();
        Long size = redisRank.opsForZSet().size(getRankKey(type));

        int currentPage = (pageNumber < 1) ? 1 : pageNumber;
        int totalPage = 1;
        if (size != null) {
            totalPage = size.intValue() / (count) + 1;
            totalPage = size.intValue() % (count) == 0 ? totalPage - 1 : totalPage;
        }
        currentPage = currentPage > totalPage ? totalPage : currentPage;
        int start = (currentPage - 1) * count;
        int end = start + count - 1;

        Set<String> reverseRange = redisRank.opsForZSet().reverseRange(getRankKey(type), start, end);
        List<RankUserDto> rankUserList = getUserRankingList(reverseRange, type);
        RankListDto findListDto = RankListDto.builder()
                .currentPage(currentPage) // 최대값은 totalPage
                .totalPage(totalPage)
                .rankList(rankUserList)
                .build();
        return findListDto;
    }

    @Transactional
    public RankUserDto findRankById(RankFindDto findDto) {
        String intraId = findDto.getIntraId();
        GameType gameType = findDto.getGameType();

        RankRedis userRankInfo = getUserRank(intraId, gameType);
        return (userRankInfo == null) ? null : RankUserDto.from(userRankInfo, getRanking(userRankInfo));
    }

    @Transactional
    public void modifyUserPpp(RankModifyDto modifyDto) {
        String intraId = modifyDto.getIntraId();
        GameType gameType = modifyDto.getGameType();

        RankRedis userRank = getUserRank(intraId, gameType);
        userRank.update(modifyDto.getIsWin(), modifyDto.getPpp());
        saveUserRank(userRank);
        saveUserRankingPpp(userRank, modifyDto.getPpp());
    }


    @Transactional
    public void modifyRankStatusMessage(RankModifyStatusMessageDto modifyDto) {
        RankRedis singleRank = getUserRank(modifyDto.getIntraId(), SINGLE);
        RankRedis doubleRank = getUserRank(modifyDto.getIntraId(), DOUBLE);
        singleRank.updateStatusMessage(modifyDto.getStatusMessage());
        doubleRank.updateStatusMessage(modifyDto.getStatusMessage());
        saveUserRank(singleRank);
        saveUserRank(doubleRank);
    }

    @Transactional
    public List<RankRedisDto> findRankAll(RankFindAllDto findAllDto) {
        Set rankKeys = redisRank.keys(Key.RANK_USER_ALL + findAllDto.getGameType().getCode());
        List<RankRedis> rankList = redisUser.opsForValue().multiGet(rankKeys);
        List<RankRedisDto> rankRedisDtos = new ArrayList<RankRedisDto>();
        if (!Collections.isEmpty(rankList)) {
            rankRedisDtos = rankList.stream().map(rank -> {
                Integer singleRanking = getRanking(rank);
                return RankRedisDto.from(rank, singleRanking.intValue());
            }).collect(Collectors.toList());
        }
        return rankRedisDtos;
    }

    public List<RankRedisDto> findRankAll() {
        Set rankKeys = redisRank.keys(Key.RANK_USER_ALL + SINGLE.getCode());
        List<RankRedis> rankList = redisUser.opsForValue().multiGet(rankKeys);
        List<RankRedisDto> rankRedisDtos = new ArrayList<RankRedisDto>();
        if (!Collections.isEmpty(rankList)) {
            rankRedisDtos = rankList.stream().map(rank -> {
                Integer singleRanking = getRanking(rank);
                return RankRedisDto.from(rank, singleRanking.intValue());
            }).collect(Collectors.toList());
        }
        return rankRedisDtos;
    }

    @Transactional
    public Boolean isEmpty() {
        return !redisRank.hasKey(Key.SINGLE);
    }

    @Transactional
    public void saveAll(List<RankDto> rankDtos) {
        rankDtos.forEach(rankDto -> {
            rankToRedisRank(rankDto);
        });
    }

    /* Admin method */

    @Transactional
    public void createRankByAdmin(RankCreateRequestDto createRequestDto) {

    }

    @Transactional
    public void updateRankByAdmin(RankUpdateRequestDto updateRequestDto) {

    }

    @Transactional
    public void deleteRankByAdmin(RankDeleteDto deleteDto) {

    }

    @Transactional
    public List<RankDto> findRankByAdmin(Pageable pageable) {
       return null;
    }

    /* Private Method */
    private List<RankUserDto> getUserRankingList(Set<String> range, GameType gameType) {
        List<RankUserDto> rankList = new ArrayList<RankUserDto>();
        range.forEach(key -> {
            RankRedis userRankInfo = getUserRank(key);
            rankList.add(RankUserDto.builder()
                    .intraId(userRankInfo.getIntraId())
                    .ppp(userRankInfo.getPpp())
                    .rank(getRanking(userRankInfo))
                    .statusMessage(userRankInfo.getStatusMessage())
                    .losses(userRankInfo.getLosses())
                    .wins(userRankInfo.getWins())
                    .winRate(userRankInfo.getWinRate())
                    .build());
        });
        return rankList;
    }

    @Transactional
    protected RankRedis getUserRank(String rankingKey) {
        String userKey = getUserKey(rankingKey.replaceAll("\"",""));
        RankRedis userRankInfo = redisUser.opsForValue().get(userKey);
        return userRankInfo;
    }

    @Transactional
    protected RankRedis getUserRank(String intraId, GameType type) {
        String userKey = getUserKey(intraId, type);
        RankRedis userRankInfo = redisUser.opsForValue().get(userKey);
        return userRankInfo;
    }

    @Transactional
    protected void saveUserRankingPpp(RankRedis userRank, Integer ppp) {
        Integer isRanked = userRank.getLosses() + userRank.getWins() != 0 ? 1 : 0;
        redisRank.opsForZSet().add(userRank.getGameType().getCode(), getUserRankKey(userRank.getIntraId(), userRank.getGameType()), ppp * isRanked);
    }

    @Transactional
    protected void saveUserRank(RankRedis rank) {
        redisUser.opsForValue().set(getUserKey(rank.getIntraId(), rank.getGameType()), rank);
    }

    private String getUserKey(String key) {
        return Key.RANK_USER + key;
    }

    private String getUserKey(String intraId, GameType gameType) {
        return Key.RANK_USER + intraId + gameType.getCode();
    }

    private String getUserRankKey(String intraId, GameType gameType) {
        return "\"" + intraId + gameType.getCode() + "\"";
    }

    private String getRankKey(GameType gameType) {
        return gameType.getCode();
    }

    @Transactional
    protected Integer getRanking(RankRedis userInfo){
        Integer totalGames = userInfo.getLosses() + userInfo.getWins();
        GameType gameType = userInfo.getGameType();
        Integer ranking = (totalGames == 0) ? -1 : redisRank.opsForZSet().reverseRank(getRankKey(gameType), getUserRankKey(userInfo.getIntraId(), gameType)).intValue() + 1;
        return ranking;
    }

    @Transactional
    protected void rankToRedisRank(RankDto rankDto) {
        RankRedis rank = RankRedis.from(rankDto, rankDto.getGameType());
        saveUserRank(rank);
        saveUserRankingPpp(rank, rankDto.getPpp());
    }

    @Transactional
    public void userToRedisRank(UserDto user) {
        RankRedis singleRank = RankRedis.from(user, SINGLE);
        RankRedis doubleRank = RankRedis.from(user, DOUBLE);
        saveUserRank(singleRank);
        saveUserRank(doubleRank);
        saveUserRankingPpp(singleRank, user.getPpp());
        saveUserRankingPpp(doubleRank, user.getPpp());
    }
}
