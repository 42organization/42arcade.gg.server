package io.pp.arcade.domain.noti.controller;

import io.pp.arcade.domain.noti.NotiService;
import io.pp.arcade.domain.noti.dto.*;
import io.pp.arcade.domain.security.jwt.TokenService;
import io.pp.arcade.domain.team.TeamService;
import io.pp.arcade.domain.team.dto.TeamPosDto;
import io.pp.arcade.domain.user.dto.UserDto;
import io.pp.arcade.global.type.NotiType;
import io.pp.arcade.global.util.HeaderUtil;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping(value = "/pingpong")
public class NotiControllerImpl implements NotiController {
    private final NotiService notiService;
    private final TeamService teamService;
    private final TokenService tokenService;

    @Override
    @GetMapping(value = "/notifications")
    public NotiResponseDto notiFindByUser(HttpServletRequest request) {
        UserDto user = tokenService.findUserByAccessToken(HeaderUtil.getAccessToken(request));
        NotiFindDto notiFindDto = NotiFindDto.builder()
                .user(user).build();
        List<NotiDto> notis = notiService.findNotiByUser(notiFindDto);
        List<Object> notiDtos = new ArrayList<>();
        notis.forEach(noti -> {
            if (noti.getType().equals(NotiType.ANNOUNCE)) {
                notiDtos.add(NotiAnnounceDto.builder()
                        .id(noti.getId())
                        .type(noti.getType())
                        .isChecked(noti.getIsChecked())
                        .message(noti.getMessage())
                        .createdAt(noti.getCreatdDate())
                        .build());
            } else if (noti.getType().equals(NotiType.MATCHED)) {
                notiDtos.add(NotiMatchedDto.builder()
                        .id(noti.getId())
                        .type(noti.getType())
                        .time(noti.getSlot().getTime())
                        .isChecked(noti.getIsChecked())
                        .createdAt(noti.getCreatdDate())
                        .build());
            } else if (noti.getType().equals(NotiType.IMMINENT)) {
                TeamPosDto teamPosDto = teamService.getTeamPosNT(user, noti.getSlot().getTeam1(), noti.getSlot().getTeam2());
                List<String> myTeam = new ArrayList<>();
                List<String> enemyTeam = new ArrayList<>();
                if (teamPosDto.getMyTeam().getUser1() != null) {
                    myTeam.add(teamPosDto.getMyTeam().getUser1().getIntraId());
                }
                if (teamPosDto.getMyTeam().getUser2() != null) {
                    myTeam.add(teamPosDto.getMyTeam().getUser2().getIntraId());
                }
                if (teamPosDto.getEnemyTeam().getUser1() != null) {
                    enemyTeam.add(teamPosDto.getEnemyTeam().getUser1().getIntraId());
                }
                if (teamPosDto.getEnemyTeam().getUser2() != null) {
                    enemyTeam.add(teamPosDto.getEnemyTeam().getUser2().getIntraId());
                }
                notiDtos.add(NotiImminentMatchDto.builder()
                        .id(noti.getId())
                        .type(noti.getType())
                        .time(noti.getSlot().getTime())
                        .isChecked(noti.getIsChecked())
                        .myTeam(myTeam)
                        .enemyTeam(enemyTeam)
                        .createdAt(noti.getCreatdDate())
                        .build());
            } else if (noti.getType().equals(NotiType.CANCELEDBYMAN) || noti.getType().equals(NotiType.CANCELEDBYTIME)) {
                notiDtos.add(NotiCanceledDto.builder()
                        .id(noti.getId())
                        .type(noti.getType())
                        .time(noti.getSlot().getTime())
                        .isChecked(noti.getIsChecked())
                        .createdAt(noti.getCreatdDate())
                        .build());
            }
        });
        NotiModifyDto modifyDto = NotiModifyDto.builder()
                .user(user).build();
        notiService.modifyNotiChecked(modifyDto);
        NotiResponseDto responseDto = NotiResponseDto.builder()
                .notifications(notiDtos)
                .build();
        return responseDto;
    }

    @Override
    @DeleteMapping(value = "notifications/{notiId}")
    public void notiRemoveOne(Integer notiId, HttpServletRequest request) {
        UserDto user = tokenService.findUserByAccessToken(HeaderUtil.getAccessToken(request));
        notiService.findNotiByIdAndUser(NotiFindDto.builder().notiId(notiId).user(user).build());
        NotiDeleteDto deleteDto = NotiDeleteDto.builder()
                .notiId(notiId)
                .build();
        notiService.removeNotiById(deleteDto);
    }

    @Override
    @DeleteMapping(value = "/notifications")
    public void notiRemoveAll(HttpServletRequest request) {
        UserDto user = tokenService.findUserByAccessToken(HeaderUtil.getAccessToken(request));
        NotiDeleteDto deleteDto = NotiDeleteDto.builder()
                        .user(user)
                        .build();
        notiService.removeAllNotisByUser(deleteDto);
    }
}
