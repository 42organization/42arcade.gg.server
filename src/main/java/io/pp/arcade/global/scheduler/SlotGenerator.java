package io.pp.arcade.global.scheduler;

import io.pp.arcade.domain.slot.SlotService;
import io.pp.arcade.domain.slot.dto.SlotAddDto;
import io.pp.arcade.domain.team.Team;
import io.pp.arcade.domain.team.TeamRepository;
import io.pp.arcade.domain.team.TeamService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

// 하루에 한 두 번
@Component
public class SlotGenerator extends AbstractScheduler {
    private final SlotService slotService;

    public SlotGenerator(SlotService slotService) {
        this.slotService = slotService;
        this.setCron("30 0 0 * * *");
    }

    @Getter @Setter
    private Integer startTime = 14;
    @Getter @Setter
    private Integer interval = 10;
    @Getter @Setter
    private Integer slotNum = 18;

    public void dailyGenerate() {
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < slotNum; i++) {
            LocalDateTime time = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(),
                    startTime + i / (60 / interval), (i * interval) % 60, 0); // 3시부터 10분 간격으로 18개 슬롯 생성
            SlotAddDto dto = SlotAddDto.builder().tableId(1).time(time).build();
            slotService.addSlot(dto);
        }
    }

    @Override
    public Runnable runnable() {
        return () -> {
            dailyGenerate();
        };
    }
}
