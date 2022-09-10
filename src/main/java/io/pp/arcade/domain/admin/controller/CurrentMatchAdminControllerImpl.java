package io.pp.arcade.domain.admin.controller;

import io.pp.arcade.domain.admin.dto.create.CurrentMatchCreateRequestDto;
import io.pp.arcade.domain.admin.dto.delete.CurrentMatchDeleteDto;
import io.pp.arcade.domain.admin.dto.update.CurrentMatchUpdateRequestDto;
import io.pp.arcade.domain.currentmatch.CurrentMatchService;
import io.pp.arcade.domain.currentmatch.dto.CurrentMatchDto;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping(value = "/admin")
public class CurrentMatchAdminControllerImpl implements CurrentMatchAdminController {
    private final CurrentMatchService currentMatchService;

    @Override
    @PostMapping(value = "/currentMatch")
    public void currentMatchCreate(CurrentMatchCreateRequestDto createRequestDto, HttpServletRequest request) {
        currentMatchService.createCurrentMatchByAdmin(createRequestDto);
    }

    @Override
    @PutMapping(value = "/currentMatch")
    public void currentMatchUpdate(CurrentMatchUpdateRequestDto updateRequestDto, HttpServletRequest request) {
        currentMatchService.updateCurrentMatchByAdmin(updateRequestDto);
    }

    @Override
    @DeleteMapping(value = "/currentMatch/{currentMatchId}")
    public void currentMatchDelete(Integer currentMatchId, HttpServletRequest request) {
        CurrentMatchDeleteDto deleteDto = CurrentMatchDeleteDto.builder().currentMatchId(currentMatchId).build();
        currentMatchService.deleteCurrentMatchByAdmin(deleteDto);
    }

    @Override
    @GetMapping(value = "/currentMatch/all")
    public List<CurrentMatchDto> currentMatchAll(Pageable pageable, HttpServletRequest request) {
        List<CurrentMatchDto> currentMatches = currentMatchService.findCurrentMatchByAdmin(pageable);
        return currentMatches;
    }
}
