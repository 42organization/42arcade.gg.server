package io.pp.arcade.domain.rank.controller;

import io.pp.arcade.domain.rank.dto.RankListResponseDto;
import io.pp.arcade.global.type.GameType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.PositiveOrZero;

public interface RankController {
        RankListResponseDto rankList(@SortDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable, @RequestParam(required = false, defaultValue = "20") Integer count, @PathVariable GameType gametype, HttpServletRequest request);
}
