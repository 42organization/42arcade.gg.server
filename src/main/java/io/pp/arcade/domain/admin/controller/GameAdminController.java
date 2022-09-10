package io.pp.arcade.domain.admin.controller;

import io.pp.arcade.domain.admin.dto.create.GameCreateRequestDto;
import io.pp.arcade.domain.admin.dto.update.GameUpdateRequestDto;
import io.pp.arcade.domain.game.dto.GameDto;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface GameAdminController {
    void gameCreate(@RequestBody GameCreateRequestDto gameCreateRequestDto, HttpServletRequest request);
    void gameUpdate(@RequestBody GameUpdateRequestDto gameUpdateRequestDto, HttpServletRequest request);
    void gameDelete(@PathVariable Integer gameId, HttpServletRequest request);
    List<GameDto> gameAll(Pageable pageable, HttpServletRequest request);
}
