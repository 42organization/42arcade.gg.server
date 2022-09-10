package io.pp.arcade.domain.currentmatch.controller;

import io.pp.arcade.domain.currentmatch.CurrentMatchService;
import io.pp.arcade.domain.currentmatch.dto.CurrentMatchDto;
import io.pp.arcade.domain.currentmatch.dto.CurrentMatchResponseDto;
import io.pp.arcade.domain.security.jwt.TokenService;
import io.pp.arcade.domain.slot.dto.SlotDto;
import io.pp.arcade.domain.team.TeamService;
import io.pp.arcade.domain.team.dto.TeamDto;
import io.pp.arcade.domain.team.dto.TeamPosDto;
import io.pp.arcade.domain.user.dto.UserDto;
import io.pp.arcade.global.util.HeaderUtil;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping(value = "/pingpong")
public class CurrentMatchControllerImpl implements CurrentMatchController {
    private final CurrentMatchService currentMatchService;
    private final TeamService teamService;
    private final TokenService tokenService;

    @Override
    @GetMapping(value = "/match/current")
    public CurrentMatchResponseDto currentMatchFind(HttpServletRequest request) {
        UserDto user = tokenService.findUserByAccessToken(HeaderUtil.getAccessToken(request));
        CurrentMatchDto currentMatch = currentMatchService.findCurrentMatchByUser(user);;
        CurrentMatchResponseDto responseDto = getCurrentMatchResponseDto(currentMatch, user);
        return responseDto;
    }

    private CurrentMatchResponseDto getCurrentMatchResponseDto(CurrentMatchDto currentMatch, UserDto curUser) {
         /*
          유저의 예약 경기가 있는가?
           - 유저의 예약 경기 정보를 반환한다.
             ㄴ 유저의 경기는 5분전인가?
               - 5분 전 아님, 슬롯 시간 정보만 반환한다.(time, isMatch, slotId)
               - 5분 전, 모든 팀의 정보를 반환한다.    (time, isMatch, slotId, myteam, enemyTeam)
             ㄴ 유저의 경기는 시작되었는가?
               - 시작 됨, 관련된 모든 정보를 반환한다.  (time, isMatch, slotId, myteam, enemyTeam, gameId)
        */
        List<String> myTeam = new ArrayList<>();
        List<String> enemyTeam = new ArrayList<>();
        LocalDateTime slotTime = null;
        boolean isMatch = false;
        Integer slotId = null;

        if (currentMatch != null)
        {
            SlotDto slot = currentMatch.getSlot();
            slotId = slot.getId();
            slotTime = slot.getTime();
            isMatch = currentMatch.getIsMatched();
            // 경기는 5분전이고 매치가 성사되었는가?
            if (currentMatch.getMatchImminent() && isMatch){
                TeamPosDto teamPosDto = teamService.getTeamPosNT(curUser, slot.getTeam1(), slot.getTeam2());
                myTeam = getTeamUsersIntraIdList(teamPosDto.getMyTeam());
                enemyTeam = getTeamUsersIntraIdList(teamPosDto.getEnemyTeam());
            }
        }
        CurrentMatchResponseDto responseDto = CurrentMatchResponseDto.builder()
                .time(slotTime)
                .isMatched(isMatch)
                .slotId(slotId)
                .myTeam(myTeam)
                .enemyTeam(enemyTeam)
                .build();
        return responseDto;
    }

    private List<String> getTeamUsersIntraIdList(TeamDto teamDto) {
        List<String> teamUsers = new ArrayList<>();
        UserDto user1 = teamDto.getUser1();
        UserDto user2 = teamDto.getUser2();
        teamUsers.add(user1.getIntraId());
        if (user2 != null) {
            teamUsers.add(user2.getIntraId());
        }
        return teamUsers;
    }
}
