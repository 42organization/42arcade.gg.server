package io.pp.arcade.global.scheduler;

import io.pp.arcade.domain.rank.dto.RankRedisDto;
import io.pp.arcade.domain.rank.dto.RankSaveAllDto;
import io.pp.arcade.domain.rank.service.RankRedisService;
import io.pp.arcade.domain.rank.service.RankService;
import io.pp.arcade.domain.season.SeasonService;
import lombok.Setter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;

@Component
public class RankScheduler extends  AbstractScheduler {
    private final RankRedisService rankRedisService;
    private final SeasonService seasonService;
    private final RankService rankService;

    public RankScheduler(RankRedisService rankRedisService, RankService rankService, SeasonService seasonService) {
        this.rankRedisService = rankRedisService;
        this.rankService = rankService;
        this.seasonService = seasonService;
        this.setCron("0 55 23 * * *");
        this.setInterval(0);
    }

    public void dailyProcess() {
        List<RankRedisDto> rankRedisDtos = rankRedisService.findRankAll();
        Integer seasonId = seasonService.findCurrentSeason().getId();
        RankSaveAllDto rankSaveAllDto = RankSaveAllDto.builder().rankRedisDtos(rankRedisDtos).seasonId(seasonId).build();
        rankService.saveAll(rankSaveAllDto);
    }

    @Override
    public Runnable runnable() {
        return () -> {
            dailyProcess();
        };
    }
}
