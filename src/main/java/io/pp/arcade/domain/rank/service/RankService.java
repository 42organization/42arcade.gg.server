package io.pp.arcade.domain.rank.service;

import io.jsonwebtoken.lang.Collections;
import io.pp.arcade.domain.admin.dto.create.RankCreateRequestDto;
import io.pp.arcade.domain.admin.dto.delete.RankDeleteDto;
import io.pp.arcade.domain.admin.dto.update.RankUpdateRequestDto;
import io.pp.arcade.domain.rank.Rank;
import io.pp.arcade.domain.rank.RankRedis;
import io.pp.arcade.domain.rank.RankRepository;
import io.pp.arcade.domain.rank.dto.RankDto;
import io.pp.arcade.domain.rank.dto.RankRedisDto;
import io.pp.arcade.domain.rank.dto.RankSaveAllDto;
import io.pp.arcade.domain.rank.dto.RankUserDto;
import io.pp.arcade.domain.user.User;
import io.pp.arcade.domain.user.UserRepository;
import io.pp.arcade.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RankService {
    private final RankRepository rankRepository;
    private final UserRepository userRepository;

    @Transactional
    public void saveAll(RankSaveAllDto saveAllDto) {
        if (saveAllDto.getSeasonId() == null) {
            throw new BusinessException("{server.internal.error}");
        }
        List<RankRedisDto> rankRedisDtos = saveAllDto.getRankRedisDtos();
        List<Rank> rankList = rankRedisDtos.stream().map(rankRedisDto -> {
            Rank rank = new Rank();
            User user = userRepository.getUserByIntraId(rankRedisDto.getIntraId());
            rank.update(rankRedisDto, user, saveAllDto.getSeasonId());
            return rank;
        }).collect(Collectors.toList());
        if (!Collections.isEmpty(rankList))
            rankRepository.saveAll(rankList);
    }

    @Transactional
    public List<RankDto> findAll() {
        List<Rank> ranks = rankRepository.findAll();
        List<RankDto> rankDtos = ranks.stream().map(RankDto::from).collect(Collectors.toList());
        return rankDtos;
    }

    @Transactional
    public void createRankByAdmin(RankCreateRequestDto createRequestDto) {
        User user = userRepository.findById(createRequestDto.getUserId()).orElseThrow();
        Rank rank = Rank.builder()
                .user(user)
                .seasonId(createRequestDto.getSeasonId())
                .racketType(createRequestDto.getRacketType())
                .ppp(createRequestDto.getPpp())
                .ranking(createRequestDto.getRangking())
                .wins(createRequestDto.getWins())
                .losses(createRequestDto.getLosses())
                .build();
        rankRepository.save(rank);
    }

    @Transactional
    public void updateRankByAdmin(RankUpdateRequestDto updateRequestDto) {
        Rank rank = rankRepository.findById(updateRequestDto.getRankId()).orElseThrow();
        rank.update(updateRequestDto.getPpp(), updateRequestDto.getWins(), updateRequestDto.getLosses());
    }

    @Transactional
    public void deleteRankByAdmin(RankDeleteDto deleteDto) {
        Rank rank = rankRepository.findById(deleteDto.getRankId()).orElseThrow();
        rankRepository.delete(rank);
    }

    @Transactional
    public List<RankDto> findRankByAdmin(Pageable pageable) {
        Page<Rank> ranks = rankRepository.findAllByOrderByIdDesc(pageable);
        List<RankDto> rankDtos = ranks.stream().map(RankDto::from).collect(Collectors.toList());
        return rankDtos;
    }
}
