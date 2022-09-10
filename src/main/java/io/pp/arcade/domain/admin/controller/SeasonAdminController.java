package io.pp.arcade.domain.admin.controller;

import io.pp.arcade.domain.admin.dto.create.SeasonCreateRequestDto;
import io.pp.arcade.domain.admin.dto.update.SeasonUpdateDto;
import io.pp.arcade.domain.season.dto.SeasonDto;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface SeasonAdminController {
    void seasonCreate(@RequestBody SeasonCreateRequestDto seasonCreateDto, HttpServletRequest request);
    void seasonDelete(@PathVariable Integer seasonId, HttpServletRequest request);
    void seasonUpdate(@RequestBody SeasonUpdateDto seasonUpdateDto, HttpServletRequest request);
    List<SeasonDto> seasonAll(Pageable pageable, HttpServletRequest request);
}
