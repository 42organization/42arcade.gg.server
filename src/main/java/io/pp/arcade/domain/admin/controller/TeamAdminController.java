package io.pp.arcade.domain.admin.controller;

import io.pp.arcade.domain.admin.dto.create.TeamCreateRequestDto;
import io.pp.arcade.domain.admin.dto.update.TeamUpdateRequestDto;
import io.pp.arcade.domain.team.dto.TeamDto;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface TeamAdminController {
    void teamCreate(@RequestBody TeamCreateRequestDto teamCreateDto, HttpServletRequest request);
    void teamUpdate(@RequestBody TeamUpdateRequestDto teamUpdateDto, HttpServletRequest request);
    void teamDelete(@PathVariable Integer teamId, HttpServletRequest request);
    List<TeamDto> teamAll(Pageable pageable, HttpServletRequest request);
}
