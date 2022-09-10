package io.pp.arcade.domain.admin.controller;

import io.pp.arcade.domain.admin.dto.create.GameCreateDto;
import io.pp.arcade.domain.admin.dto.create.GameCreateRequestDto;
import io.pp.arcade.domain.admin.dto.delete.GameDeleteDto;
import io.pp.arcade.domain.admin.dto.update.GameUpdateDto;
import io.pp.arcade.domain.admin.dto.update.GameUpdateRequestDto;
import io.pp.arcade.domain.admin.dto.update.PChangeUpdateDto;
import io.pp.arcade.domain.admin.dto.update.TeamUpdateDto;
import io.pp.arcade.domain.game.Game;
import io.pp.arcade.domain.game.GameService;
import io.pp.arcade.domain.game.dto.GameDto;
import io.pp.arcade.domain.pchange.PChange;
import io.pp.arcade.domain.pchange.PChangeRepository;
import io.pp.arcade.domain.pchange.PChangeService;
import io.pp.arcade.domain.pchange.dto.PChangeDto;
import io.pp.arcade.domain.pchange.dto.PChangeFindDto;
import io.pp.arcade.domain.rank.dto.RankModifyDto;
import io.pp.arcade.domain.rank.service.RankRedisService;
import io.pp.arcade.domain.slot.SlotService;
import io.pp.arcade.domain.slot.dto.SlotDto;
import io.pp.arcade.domain.team.Team;
import io.pp.arcade.domain.team.TeamService;
import io.pp.arcade.domain.team.dto.TeamDto;
import io.pp.arcade.domain.team.dto.TeamModifyGameResultDto;
import io.pp.arcade.domain.user.User;
import io.pp.arcade.domain.user.UserService;
import io.pp.arcade.domain.user.dto.UserDto;
import io.pp.arcade.domain.user.dto.UserModifyPppDto;
import io.pp.arcade.global.type.GameType;
import io.pp.arcade.global.util.EloRating;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@AllArgsConstructor
@RequestMapping(value = "/admin")
public class GameAdminControllerImpl implements GameAdminController {
    private final GameService gameService;
    private final TeamService teamService;
    private final PChangeService pChangeService;
    private final UserService userService;
    private final SlotService slotService;
    private final RankRedisService rankRedisService;

    @Override
    @PostMapping(value = "/game")
    public void gameCreate(GameCreateRequestDto createRequestDto, HttpServletRequest request) {
        SlotDto slotDto = slotService.findSlotById(createRequestDto.getSlotId());
        GameCreateDto createDto = GameCreateDto.builder()
                .slotId(slotDto.getId())
                .team1Id(slotDto.getTeam1().getId())
                .team2Id(slotDto.getTeam2().getId())
                .type(slotDto.getType())
                .time(slotDto.getTime())
                .seasonId(createRequestDto.getSeasonId())
                .status(createRequestDto.getStatus())
                .build();
        gameService.createGameByAdmin(createDto);
    }

    @Override
    @PutMapping(value = "/game")
    public void gameUpdate(GameUpdateRequestDto updateRequestDto, HttpServletRequest request) {
        /* 해당 게임의 팀 스코어랑 승리여부 수정 */
        Integer team1Score = updateRequestDto.getTeam1Score();
        Integer team2Score = updateRequestDto.getTeam2Score();
        GameDto game = gameService.findById(updateRequestDto.getGameId());
        TeamDto team1 = game.getTeam1();
        TeamDto team2 = game.getTeam2();

        TeamModifyGameResultDto modifyTeam1GameResultDto = TeamModifyGameResultDto.builder()
                .teamId(team1.getId())
                .score(team1Score)
                .win(team1Score > team2Score)
                .build();
        TeamModifyGameResultDto modifyTeam2GameResultDto = TeamModifyGameResultDto.builder()
                .teamId(team2.getId())
                .score(team2Score)
                .win(team2Score > team1Score)
                .build();
        teamService.modifyGameResultInTeam(modifyTeam1GameResultDto);
        teamService.modifyGameResultInTeam(modifyTeam2GameResultDto);
        /* 게임 결과가 수정된 팀 유저들의 ppp 수정 */
        modifyUsersInfo(game);
    }

    /* 유저 찾기
    -> 각 유저의 해당 게임 pChange 찾기
    -> 예전 ppp(pppResult - pppChange)에, 해당 게임의 수정된 결과를 이용해 Elorating 재적용
    -> pChange까지 수정
    -> rank 수정
     */
    private void modifyUsersInfo(GameDto gameDto) {
        TeamDto updatedTeam1 = teamService.findById(gameDto.getTeam1().getId());
        TeamDto updatedTeam2 = teamService.findById(gameDto.getTeam2().getId());
        Boolean isOneSide = Math.abs(updatedTeam1.getScore() - updatedTeam2.getScore()) == 2;
        modifyUserPPPAndPChangeAndRank(gameDto, updatedTeam1.getUser1(), updatedTeam2, isOneSide);
        modifyUserPPPAndPChangeAndRank(gameDto, updatedTeam2.getUser2(), updatedTeam2, isOneSide);
        modifyUserPPPAndPChangeAndRank(gameDto, updatedTeam1.getUser2(), updatedTeam1, isOneSide);
        modifyUserPPPAndPChangeAndRank(gameDto, updatedTeam2.getUser1(), updatedTeam1, isOneSide);
    }

    private void modifyUserPPPAndPChangeAndRank(GameDto game, UserDto userDto, TeamDto enemyTeamDto, Boolean isOneSide) {
        if (userDto == null) {
            return;
        }
        PChangeDto pChangeDto = pChangeService.findPChangeByUserAndGame(PChangeFindDto.builder()
                .gameId(game.getId())
                .userId(userDto.getIntraId())
                .build());
        Integer userPreviousPpp = pChangeDto.getPppResult() - pChangeDto.getPppChange();

        Integer newPppChange = EloRating.pppChange(userPreviousPpp, enemyTeamDto.getTeamPpp(),!enemyTeamDto.getWin(), isOneSide);
        Integer tmpPpp = userPreviousPpp + newPppChange;
        Integer userFinalPpp = tmpPpp > 0 ? tmpPpp : 0;
        UserModifyPppDto modifyPppDto = UserModifyPppDto.builder()
                .userId(userDto.getId())
                .ppp(userFinalPpp)
                .build();
        userService.modifyUserPpp(modifyPppDto);

        Integer pChangeId = pChangeService.findPChangeIdByUserAndGame(PChangeFindDto.builder()
                .gameId(game.getId())
                .userId(userDto.getIntraId())
                .build());
        pChangeService.updatePChangeByAdmin(pChangeId, PChangeUpdateDto.builder()
                .gameId(game.getId())
                .userId(userDto.getId())
                .pppChange(userFinalPpp - userPreviousPpp)
                .pppResult(userFinalPpp)
                .build());
        GameType gameType = gameService.findById(game.getId()).getType();
        RankModifyDto rankModifyDto =  RankModifyDto.builder()
                .gameType(gameType)
                .Ppp(userFinalPpp)
                .intraId(userDto.getIntraId())
                .isWin(!enemyTeamDto.getWin())
                .build();
        rankRedisService.modifyUserPpp(rankModifyDto);
    }

    @Override
    @DeleteMapping(value = "/game/{gameId}")
    public void gameDelete(Integer gameId, HttpServletRequest request) {
        GameDeleteDto deleteDto = GameDeleteDto.builder().gameId(gameId).build();
        gameService.deleteGameByAdmin(deleteDto);
    }

    @Override
    @GetMapping(value = "/game/all")
    public List<GameDto> gameAll(Pageable pageable, HttpServletRequest request) {
        List<GameDto> games = gameService.findGameByAdmin(pageable);
        return games;
    }
}
