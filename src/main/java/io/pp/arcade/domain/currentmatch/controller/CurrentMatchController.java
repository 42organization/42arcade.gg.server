package io.pp.arcade.domain.currentmatch.controller;

import io.pp.arcade.domain.currentmatch.dto.CurrentMatchResponseDto;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

public interface CurrentMatchController {
    CurrentMatchResponseDto currentMatchFind(HttpServletRequest request);
}
