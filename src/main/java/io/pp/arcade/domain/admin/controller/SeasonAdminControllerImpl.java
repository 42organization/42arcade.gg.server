package io.pp.arcade.domain.admin.controller;

import io.pp.arcade.domain.admin.dto.create.SeasonCreateRequestDto;
import io.pp.arcade.domain.admin.dto.update.SeasonUpdateDto;
import io.pp.arcade.domain.season.SeasonService;
import io.pp.arcade.domain.season.dto.SeasonDeleteDto;
import io.pp.arcade.domain.season.dto.SeasonDto;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Pageable;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping(value = "/admin")
public class SeasonAdminControllerImpl implements SeasonAdminController {
    private final SeasonService seasonService;

    @Override
    @PostMapping(value = "/season")
    public void seasonCreate(SeasonCreateRequestDto createRequestDto, HttpServletRequest request) {
        seasonService.createSeasonByAdmin(createRequestDto);
    }

    @Override
    @DeleteMapping(value = "/season/{seasonId}")
    public void seasonDelete(Integer seasonId, HttpServletRequest request) {
        SeasonDeleteDto deleteDto = SeasonDeleteDto.builder()
                .seasonId(seasonId).build();
        seasonService.deleteSeasonByAdmin(deleteDto);
    }

    @Override
    @PutMapping(value = "/season")
    public void seasonUpdate(SeasonUpdateDto seasonUpdateDto, HttpServletRequest request) {
        seasonService.updateSeasonByAdmin(seasonUpdateDto);
    }

    @Override
    @GetMapping(value = "/season/all")
    public List<SeasonDto> seasonAll(Pageable pageable, HttpServletRequest request) {
        List<SeasonDto> seasons = seasonService.findSeasonsByAdmin(pageable);
        return seasons;
    }
}
