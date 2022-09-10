package io.pp.arcade.domain.admin.controller;

import io.pp.arcade.domain.admin.dto.CronUpdateDto;
import io.pp.arcade.domain.admin.dto.update.SlotGeneratorUpdateDto;
import io.pp.arcade.global.scheduler.CurrentMatchUpdater;
import io.pp.arcade.global.scheduler.GameGenerator;
import io.pp.arcade.global.scheduler.RankScheduler;
import io.pp.arcade.global.scheduler.SlotGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/admin")
public class SchedulerControllerImpl implements SchedulerController {
    private final RankScheduler rankScheduler;
    private final CurrentMatchUpdater currentMatchUpdater;
    private final SlotGenerator slotGenerator;
    private final GameGenerator gameGenerator;

    @Override
    @PostMapping(value = "/scheduler/slot")
    public void slotGeneratorCronUpdate(CronUpdateDto cron) {
        slotGenerator.setInterval(cron.getInterval());
        slotGenerator.setCron(cron.getCron());
        slotGenerator.renewScheduler();
    }

    @Override
    @PutMapping(value = "/scheduler/rank")
    public void rankSchedulerUpdate(CronUpdateDto cron) {
        rankScheduler.setInterval(cron.getInterval());
        rankScheduler.setCron(cron.getCron());
        rankScheduler.renewScheduler();
    }

    @Override
    @PostMapping(value = "/scheduler/rank")
    public void rankDailyProcessNow() {
        rankScheduler.dailyProcess();
    }

    @PutMapping(value = "/scheduler/current")
    public void currentMatchCronUpdate(CronUpdateDto cron) {
        currentMatchUpdater.setInterval(cron.getInterval());
        currentMatchUpdater.setCron(cron.getCron());
        currentMatchUpdater.renewScheduler();
    }

    @PutMapping(value = "/scheduler/game")
    public void gameCronUpdate(CronUpdateDto cron) {
        gameGenerator.setInterval(cron.getInterval());
        gameGenerator.setCron(cron.getCron());
        gameGenerator.renewScheduler();
    }
}
