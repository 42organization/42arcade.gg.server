package io.pp.arcade.domain.admin.controller;

import io.pp.arcade.domain.admin.dto.create.TeamCreateRequestDto;
import io.pp.arcade.domain.admin.dto.delete.TeamDeleteDto;
import io.pp.arcade.domain.admin.dto.update.TeamUpdateDto;
import io.pp.arcade.domain.admin.dto.update.TeamUpdateRequestDto;
import io.pp.arcade.domain.team.TeamService;
import io.pp.arcade.domain.team.dto.TeamDto;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping(value = "/admin")
public class TeamAdminControllerImpl implements TeamAdminController {
    private final TeamService teamService;

    @Override
    @PostMapping(value = "/team")
    public void teamCreate(TeamCreateRequestDto teamCreateDto, HttpServletRequest request) {
        teamService.createTeamByAdmin(teamCreateDto);
    }

    @Override
    @PutMapping(value = "/team")
    public void teamUpdate(TeamUpdateRequestDto updateRequestDto, HttpServletRequest request) {
        TeamUpdateDto updateDto = TeamUpdateDto.builder()
                .teamId(updateRequestDto.getTeamId())
                .user1Id(updateRequestDto.getUser1Id())
                .user2Id(updateRequestDto.getUser2Id())
                .headCount(updateRequestDto.getHeadCount())
                .score(updateRequestDto.getScore())
                .win(updateRequestDto.getWin())
                .teamPpp(updateRequestDto.getTeamPpp())
                .build();
        teamService.updateTeamByAdmin(updateDto);
    }

    @Override
    @DeleteMapping(value = "/team/{teamId}")
    public void teamDelete(Integer teamId, HttpServletRequest request) {
        teamService.deleteTeamByAdmin(TeamDeleteDto.builder().teamId(teamId).build());
    }

    @Override
    @GetMapping(value = "/team/all")
    public List<TeamDto> teamAll(Pageable pageable, HttpServletRequest request) {
        List<TeamDto> teamDtos = teamService.findTeamByAdmin(pageable);
        return teamDtos;
    }
}
