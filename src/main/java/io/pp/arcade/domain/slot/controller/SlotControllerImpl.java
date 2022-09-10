package io.pp.arcade.domain.slot.controller;

import io.pp.arcade.domain.currentmatch.CurrentMatchService;
import io.pp.arcade.domain.currentmatch.dto.CurrentMatchAddDto;
import io.pp.arcade.domain.currentmatch.dto.CurrentMatchDto;
import io.pp.arcade.domain.currentmatch.dto.CurrentMatchModifyDto;
import io.pp.arcade.domain.currentmatch.dto.CurrentMatchRemoveDto;
import io.pp.arcade.domain.noti.dto.NotiCanceledTypeDto;
import io.pp.arcade.domain.season.SeasonService;
import io.pp.arcade.domain.season.dto.SeasonDto;
import io.pp.arcade.domain.security.jwt.TokenService;
import io.pp.arcade.domain.slot.SlotService;
import io.pp.arcade.domain.slot.dto.*;
import io.pp.arcade.domain.team.TeamService;
import io.pp.arcade.domain.team.dto.TeamAddUserDto;
import io.pp.arcade.domain.team.dto.TeamDto;
import io.pp.arcade.domain.team.dto.TeamPosDto;
import io.pp.arcade.domain.team.dto.TeamRemoveUserDto;
import io.pp.arcade.domain.slot.dto.SlotStatusResponseDto;
import io.pp.arcade.domain.user.dto.UserDto;
import io.pp.arcade.global.exception.BusinessException;
import io.pp.arcade.global.redis.Key;
import io.pp.arcade.global.scheduler.SlotGenerator;
import io.pp.arcade.global.type.GameType;
import io.pp.arcade.global.type.NotiType;
import io.pp.arcade.global.type.SlotStatusType;
import io.pp.arcade.global.util.HeaderUtil;
import io.pp.arcade.global.util.NotiGenerater;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@AllArgsConstructor
@RequestMapping(value = "/pingpong")
public class SlotControllerImpl implements SlotController {
    private final SlotService slotService;
    private final TeamService teamService;
    private final CurrentMatchService currentMatchService;
    private final NotiGenerater notiGenerater;
    private final SeasonService seasonService;
    private final TokenService tokenService;
    private final RedisTemplate redisTemplate;
    private final SlotGenerator slotGenerator;

    @Override
    @GetMapping(value = "/match/tables/{tableId}/{type}")
    public SlotStatusResponseDto slotStatusList(Integer tableId, GameType type, HttpServletRequest request) {
        UserDto user = tokenService.findUserByAccessToken(HeaderUtil.getAccessToken(request));
        List<SlotStatusDto> slots;
        List<List<SlotStatusDto>> matchBoards;
        SlotFindStatusDto findDto = SlotFindStatusDto.builder()
                .userId(user.getId())
                .type(type)
                .currentTime(LocalDateTime.now())
                .build();
        slots = slotService.findSlotsStatus(findDto);
        matchBoards = groupingSlots(slots);
        SlotStatusResponseDto responseDto = SlotStatusResponseDto.builder().matchBoards(matchBoards).intervalMinute(slotGenerator.getInterval()).build();
        return responseDto;
    }

    @Override
    @PostMapping(value = "/match/tables/{tableId}/{type}")
    public void slotAddUser(Integer tableId, GameType type, SlotAddUserRequestDto addReqDto, HttpServletRequest request) throws MessagingException {
        UserDto user = tokenService.findUserByAccessToken(HeaderUtil.getAccessToken(request));
        doubleNotSupportedYet(type);
        Integer userId = user.getId();
        SlotDto slot = slotService.findSlotById(addReqDto.getSlotId());

        checkIfUserHaveCurrentMatch(user);
        checkIfUserHavePenalty(user);
        checkIfSlotAvailable(slot, type, user);

        //user가 들어갈 팀을 정한당
        TeamAddUserDto teamAddUserDto = getTeamAddUserDto(slot, user);

        //유저가 슬롯에 입장하면 currentMatch에 등록된다.
        CurrentMatchAddDto matchAddDto = CurrentMatchAddDto.builder()
                .slot(slot)
                .userId(userId)
                .build();
        currentMatchService.addCurrentMatch(matchAddDto);

        SlotAddUserDto addDto = SlotAddUserDto.builder()
                .slotId(addReqDto.getSlotId())
                .type(type)
                .joinUserPpp(user.getPpp())
                .build();
        slotService.addUserInSlot(addDto);
        teamService.addUserInTeam(teamAddUserDto);

        slot = slotService.findSlotById(slot.getId());

        //유저가 슬롯에 꽉 차면 currentMatch가 전부 바뀐다.
        modifyUsersCurrentMatchStatus(user, slot);
        notiGenerater.addMatchNotisBySlot(slot);
    }

    private void doubleNotSupportedYet(GameType type) {
        if (GameType.DOUBLE.equals(type)) {
            throw new BusinessException("SC004");
        }
    }

    @Override
    @DeleteMapping(value = "/match/slots/{slotId}")
    public void slotRemoveUser(Integer slotId, HttpServletRequest request) throws MessagingException {
        // slotId , tableId 유효성 검사
        UserDto user = tokenService.findUserByAccessToken(HeaderUtil.getAccessToken(request));
        // 유저 조회, 슬롯 조회, 팀 조회( 슬롯에 헤드 카운트 -, 팀에서 유저 퇴장 )
        CurrentMatchDto currentMatch = currentMatchService.findCurrentMatchByUser(user);
        checkIfCurrentMatchExists(currentMatch);
        SlotDto slot = currentMatch.getSlot();
        checkIfUserRemovable(currentMatch, slot);

        CurrentMatchRemoveDto currentMatchRemoveDto = CurrentMatchRemoveDto.builder()
                .userId(user.getId()).build();
        currentMatchService.removeCurrentMatch(currentMatchRemoveDto);
        teamService.removeUserInTeam(getTeamRemoveUserDto(slot, user));
        slotService.removeUserInSlot(getSlotRemoveUserDto(slot, user));
        slot = slotService.findSlotById(slot.getId());
        checkIsSlotMatched(user, currentMatch, slot);
    }

    private void checkIsSlotMatched(UserDto user, CurrentMatchDto currentMatch, SlotDto slot) throws MessagingException {
        if (currentMatch.getIsMatched() == true) {
            falsifyIsMatchedForRemainders(slot);
            redisTemplate.opsForValue().set(Key.PENALTY_USER + user.getIntraId(), "true", 60, TimeUnit.SECONDS);
            notiGenerater.addCancelNotisBySlot(NotiCanceledTypeDto.builder().slotDto(slot).notiType(NotiType.CANCELEDBYMAN).build());
        }
    }

    private void checkIfUserRemovable(CurrentMatchDto currentMatch, SlotDto slot) {
        if (currentMatch.getMatchImminent() && slot.getHeadCount() == (slot.getType().equals(GameType.SINGLE) ? 2 : 4)) {
            throw new BusinessException("SD002");
        }
    }

    private void checkIfCurrentMatchExists(CurrentMatchDto currentMatch) {
        if (currentMatch == null) {
            throw new BusinessException("SD001");
        }
    }

    private void falsifyIsMatchedForRemainders(SlotDto slot) {
        List<UserDto> users = new ArrayList<>();
        users.add(slot.getTeam1().getUser1());
        users.add(slot.getTeam1().getUser2());
        users.add(slot.getTeam2().getUser1());
        users.add(slot.getTeam2().getUser2());

        for (UserDto user : users) {
            if (user != null) {
                currentMatchService.modifyCurrentMatch(CurrentMatchModifyDto.builder()
                        .userId(user.getId())
                        .isMatched(false)
                        .matchImminent(false)
                        .build());
            }
        }
    }

    private List<List<SlotStatusDto>> groupingSlots(List<SlotStatusDto> slots) {
        List<List<SlotStatusDto>> slotGroups = new ArrayList<>();
        if (!slots.isEmpty()) {
            List<SlotStatusDto> oneGroup = new ArrayList<>();
            int groupTime = slots.get(0).getTime().getHour();

            for(SlotStatusDto slot: slots) {
                if (slot.getTime().getHour() == groupTime) {
                    oneGroup.add(slot);
                } else {
                    slotGroups.add(oneGroup);
                    oneGroup = new ArrayList<>(); //다음 그루핑을 위한 그룹 생성
                    groupTime = slot.getTime().getHour(); //시간 갱신
                    oneGroup.add(slot);
                }
            }
            slotGroups.add(oneGroup);
        }


        return slotGroups;
    }

    private void modifyUsersCurrentMatchStatus(UserDto user, SlotDto slot) {
        TeamDto team1 = slot.getTeam1();
        TeamDto team2 = slot.getTeam2();
        Integer maxSlotHeadCount = GameType.SINGLE.equals(slot.getType()) ? 2 : 4;
        Boolean isMatched = slot.getHeadCount().equals(maxSlotHeadCount);
        Boolean isImminent = slot.getTime().isBefore(LocalDateTime.now().plusMinutes(5));
        CurrentMatchModifyDto matchModifyDto = CurrentMatchModifyDto.builder()
                .userId(user.getId())
                .isMatched(isMatched)
                .matchImminent(isImminent)
                .build();
        modifyCurrentMatch(team1.getUser1(), matchModifyDto);
        modifyCurrentMatch(team1.getUser2(), matchModifyDto);
        modifyCurrentMatch(team2.getUser1(), matchModifyDto);
        modifyCurrentMatch(team2.getUser2(), matchModifyDto);
    }

    private TeamAddUserDto getTeamAddUserDto(SlotDto slot, UserDto user) {
        Integer teamId;
        TeamDto team1 = slot.getTeam1();
        TeamDto team2 = slot.getTeam2();
        GameType slotType = slot.getType();
        Integer maxTeamHeadCount = GameType.SINGLE.equals(slotType) ? 1 : 2;

        if (team1.getHeadCount() < maxTeamHeadCount) {
            teamId = team1.getId();
        } else {
            teamId = team2.getId();
        }
        TeamAddUserDto teamAddUserDto = TeamAddUserDto.builder()
                .teamId(teamId)
                .userId(user.getId())
                .build();
        return teamAddUserDto;
    }

    private void checkIfSlotAvailable(SlotDto slot, GameType gameType, UserDto user) {
        Integer pppGap = getPppGapFromSeason();

        SlotFilterDto slotFilterDto = SlotFilterDto.builder()
                .slotId(slot.getId())
                .slotTime(slot.getTime())
                .slotType(slot.getType())
                .gameType(gameType)
                .userPpp(user.getPpp())
                .gamePpp(slot.getGamePpp())
                .pppGap(pppGap)
                .headCount(slot.getHeadCount())
                .build();
        if (SlotStatusType.CLOSE.equals(slotService.getStatus(slotFilterDto))) {
            throw new BusinessException("SC001");
        }
    }

    private Integer getPppGapFromSeason() {
        Integer pppGap;
        SeasonDto season = seasonService.findCurrentSeason();
        if (season == null) {
            pppGap = 100;
        } else {
            pppGap = season.getPppGap();
        }
        return pppGap;
    }

    private TeamRemoveUserDto getTeamRemoveUserDto(SlotDto slot, UserDto user) {
        TeamPosDto teamPos = teamService.getTeamPosNT(user, slot.getTeam1(), slot.getTeam2());
        TeamRemoveUserDto teamRemoveUserDto = TeamRemoveUserDto.builder()
                .userId(user.getId())
                .teamId(teamPos.getMyTeam().getId())
                .build();
        return teamRemoveUserDto;
    }

    private SlotRemoveUserDto getSlotRemoveUserDto(SlotDto slot, UserDto user) {
        SlotRemoveUserDto slotRemoveUserDto = SlotRemoveUserDto.builder()
                .slotId(slot.getId())
                .userId(user.getIntraId())
                .exitUserPpp(user.getPpp())
                .build();
        return slotRemoveUserDto;
    }

    private void modifyCurrentMatch(UserDto user, CurrentMatchModifyDto modifyDto) {
        if (user != null) {
            CurrentMatchModifyDto matchModifyDto = CurrentMatchModifyDto.builder()
                    .userId(user.getId())
                    .isMatched(modifyDto.getIsMatched())
                    .matchImminent(modifyDto.getMatchImminent())
                    .build();
            currentMatchService.modifyCurrentMatch(matchModifyDto);
        }
    }

    private void checkIfUserHavePenalty(UserDto user) {
        if (redisTemplate.opsForValue().get(Key.PENALTY_USER + user.getIntraId()) != null) {
            throw new BusinessException("SC003");
        }
    }

        private void checkIfUserHaveCurrentMatch(UserDto user) {
        CurrentMatchDto matchDto = currentMatchService.findCurrentMatchByUser(user);
        if (matchDto != null) {
            throw new BusinessException("SC002");
        }
    }
}