package io.pp.arcade.domain.admin.controller;

import io.pp.arcade.domain.admin.dto.create.CurrentMatchCreateRequestDto;
import io.pp.arcade.domain.admin.dto.update.CurrentMatchUpdateRequestDto;
import io.pp.arcade.domain.currentmatch.dto.CurrentMatchDto;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface CurrentMatchAdminController {
    void currentMatchCreate(@RequestBody CurrentMatchCreateRequestDto currentMatchCreateRequestDto, HttpServletRequest request);
    void currentMatchUpdate(@RequestBody CurrentMatchUpdateRequestDto currentMatchUpdateRequestDto, HttpServletRequest request);
    void currentMatchDelete(@PathVariable Integer currentMatchId, HttpServletRequest request);
    List<CurrentMatchDto> currentMatchAll(Pageable pageable, HttpServletRequest request);
}
