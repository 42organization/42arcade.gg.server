package io.pp.arcade.domain.rank.controller;

import io.jsonwebtoken.lang.Collections;
import io.pp.arcade.domain.rank.dto.*;
import io.pp.arcade.domain.rank.service.RankRedisService;
import io.pp.arcade.domain.rank.service.RankService;
import io.pp.arcade.domain.security.jwt.TokenService;
import io.pp.arcade.domain.user.dto.UserDto;
import io.pp.arcade.global.type.GameType;
import io.pp.arcade.global.util.HeaderUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/pingpong")
public class RankControllerImpl implements RankController {
    private final RankService rankService;
    private final RankRedisService rankRedisService;
    private final TokenService tokenService;

    @Override
    @GetMapping(value = "/ranks/{gameType}")
    public RankListResponseDto rankList(Pageable pageable, Integer count, GameType gameType, HttpServletRequest request) {
        UserDto user = tokenService.findUserByAccessToken(HeaderUtil.getAccessToken(request));
        RankListDto rankListDto = rankRedisService.findRankList(RankFindListDto.builder().pageable(pageable).gameType(gameType).count(count).build());
        RankUserDto userRank = rankRedisService.findRankById(RankFindDto.builder().intraId(user.getIntraId()).gameType(gameType).build());
        RankListResponseDto rankListResponseDto = RankListResponseDto.builder()
                    .myIntraId(user.getIntraId())
                    .myRank(userRank.getRank())
                    .currentPage(rankListDto.getCurrentPage())
                    .totalPage(rankListDto.getTotalPage())
                    .rankList(rankListDto.getRankList())
                    .build();
        return rankListResponseDto;
    }

    @PostConstruct
    private void init (){
        if (rankRedisService.isEmpty()) {
            List<RankDto> rankList = rankService.findAll();
            if (!Collections.isEmpty(rankList)) {
                rankRedisService.saveAll(rankList);
            }
        }
    }
}
