package io.pp.arcade.domain.admin.controller;

import io.pp.arcade.domain.admin.dto.CronUpdateDto;
import io.pp.arcade.domain.admin.dto.update.SlotGeneratorUpdateDto;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;

public interface SchedulerController {
    void slotGeneratorCronUpdate(@RequestBody CronUpdateDto cron);
    void rankSchedulerUpdate(@RequestBody CronUpdateDto cron);
    void rankDailyProcessNow();
    void gameCronUpdate(@RequestBody CronUpdateDto cron);
    void currentMatchCronUpdate(@RequestBody CronUpdateDto cron);
}
