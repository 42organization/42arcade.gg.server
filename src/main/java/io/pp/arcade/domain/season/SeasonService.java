package io.pp.arcade.domain.season;

import io.pp.arcade.domain.admin.dto.create.SeasonCreateRequestDto;
import io.pp.arcade.domain.admin.dto.update.SeasonUpdateDto;
import io.pp.arcade.domain.season.dto.SeasonAddDto;
import io.pp.arcade.domain.season.dto.SeasonDeleteDto;
import io.pp.arcade.domain.season.dto.SeasonDto;
import io.pp.arcade.global.exception.BusinessException;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class SeasonService {
    private final SeasonRepository seasonRepository;

    @Transactional
    public void createSeasonByAdmin(SeasonCreateRequestDto createDto) {
        Season lastSeason = seasonRepository.findFirstByOrderByIdDesc().orElse(null);
        if (lastSeason != null) {
            lastSeason.setEndTime(createDto.getStartTime().minusSeconds(1));
        }
        Season season = Season.builder()
                .seasonName(createDto.getSeasonName())
                .startTime(createDto.getStartTime())
                .endTime(LocalDateTime.of(9999, 12, 31, 23, 59, 59))
                .startPpp(createDto.getStartPpp())
                .pppGap(createDto.getPppGap()).build();
        seasonRepository.save(season);
    }

    @Transactional
    public void deleteSeasonByAdmin(SeasonDeleteDto deleteDto) {
        Season season = seasonRepository.findById(deleteDto.getSeasonId()).orElseThrow(() -> new BusinessException("E0001"));
        seasonRepository.delete(season);
    }

    @Transactional
    public SeasonDto findCurrentSeason() {
        LocalDateTime now = LocalDateTime.now();
//        Season season = seasonRepository.findSeasonByStartTimeIsBeforeAndEndTimeIsAfter(now, now).orElseThrow(() -> new BusinessException("E0001"));
        Season season = seasonRepository.findSeasonByStartTimeIsBeforeAndEndTimeIsAfter(now, now).orElse(null);
        if (season == null)
            return null;
        return SeasonDto.from(season);
    }

    @Transactional
    public List<SeasonDto> findSeasonsByAdmin(Pageable pageable) {
        Page<Season> seasons = seasonRepository.findAllByOrderByIdDesc(pageable);
        List<SeasonDto> seasonDtos = seasons.stream().map(SeasonDto::from).collect(Collectors.toList());
        return seasonDtos;
    }

    @Transactional
    public void updateSeasonByAdmin(SeasonUpdateDto seasonUpdateDto) {
        Season season = seasonRepository.findById(seasonUpdateDto.getId()).orElseThrow(() -> new BusinessException("E0001"));
        season.setPppGap(seasonUpdateDto.getPppGap());
        season.setStartPpp(seasonUpdateDto.getStartPpp());
    }
}
