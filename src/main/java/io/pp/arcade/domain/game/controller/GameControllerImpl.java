package io.pp.arcade.domain.game.controller;

import io.pp.arcade.domain.currentmatch.CurrentMatchService;
import io.pp.arcade.domain.currentmatch.dto.CurrentMatchDto;
import io.pp.arcade.domain.currentmatch.dto.CurrentMatchFindDto;
import io.pp.arcade.domain.currentmatch.dto.CurrentMatchRemoveDto;
import io.pp.arcade.domain.game.GameService;
import io.pp.arcade.domain.game.dto.*;
import io.pp.arcade.domain.pchange.PChangeService;
import io.pp.arcade.domain.pchange.dto.PChangeAddDto;
import io.pp.arcade.domain.pchange.dto.PChangeDto;
import io.pp.arcade.domain.pchange.dto.PChangeFindDto;
import io.pp.arcade.domain.pchange.dto.PChangePageDto;
import io.pp.arcade.domain.rank.service.RankRedisService;
import io.pp.arcade.domain.rank.dto.RankFindDto;
import io.pp.arcade.domain.rank.dto.RankModifyDto;
import io.pp.arcade.domain.rank.dto.RankUserDto;
import io.pp.arcade.domain.security.jwt.TokenService;
import io.pp.arcade.domain.team.TeamService;
import io.pp.arcade.domain.team.dto.TeamDto;
import io.pp.arcade.domain.team.dto.TeamModifyGameResultDto;
import io.pp.arcade.domain.team.dto.TeamPosDto;
import io.pp.arcade.domain.user.UserService;
import io.pp.arcade.domain.user.dto.UserDto;
import io.pp.arcade.domain.user.dto.UserModifyPppDto;
import io.pp.arcade.global.exception.BusinessException;
import io.pp.arcade.global.type.GameType;
import io.pp.arcade.global.type.StatusType;
import io.pp.arcade.global.util.EloRating;
import io.pp.arcade.global.util.HeaderUtil;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
@RequestMapping(value = "/pingpong")
public class GameControllerImpl implements GameController {
    private final GameService gameService;
    private final TeamService teamService;
    private final UserService userService;
    private final PChangeService pChangeService;
    private final CurrentMatchService currentMatchService;
    private final RankRedisService rankRedisService;
    private final TokenService tokenService;

    @Override
    @GetMapping(value = "/games/result")
    public GameUserInfoResponseDto gameUserInfo(HttpServletRequest request) {
        UserDto user = tokenService.findUserByAccessToken(HeaderUtil.getAccessToken(request));
        CurrentMatchDto currentMatch = currentMatchService.findCurrentMatchByUser(user);

        checkIfUserHavePlayingGame(currentMatch);

        GameDto game = currentMatch.getGame();
        List<GameUserInfoDto> myTeams = new ArrayList<>();
        List<GameUserInfoDto> enemyTeams = new ArrayList<>();

        //figuring out team number for myteam and enemyteam, and put each team's user infos in the right team
        putUsersDataInTeams(game, user, myTeams, enemyTeams);

        //make Dto to return
        GameUserInfoResponseDto gameUserInfoResponseDto = GameUserInfoResponseDto.builder()
                .myTeam(myTeams)
                .enemyTeam(enemyTeams)
                .build();
        return gameUserInfoResponseDto;
    }

    @Override
    @PostMapping(value = "/games/result")
    public void gameResultSave(GameResultRequestDto requestDto, HttpServletRequest request) {
        UserDto user = tokenService.findUserByAccessToken(HeaderUtil.getAccessToken(request));
        CurrentMatchDto currentMatch = currentMatchService.findCurrentMatchByUser(user);
        validateInput(requestDto);
        // if the result already exists, throw 202 error
        if (currentMatch == null) {
            throw new ResponseStatusException(HttpStatus.ACCEPTED, "");
        }
        GameDto game = currentMatch.getGame();
        if (game == null) {
            throw new BusinessException("E0001");
        }
        removeCurrentMatch(game);
        TeamDto team1 = game.getTeam1();
        TeamDto team2 = game.getTeam2();
        // figuring out team number for myteam and enemyteam
        List<TeamModifyGameResultDto> eachTeamModifyDto = getTeamModifyDto(team1, team2, requestDto, user);
        // modify team with game result
        for (TeamModifyGameResultDto dto : eachTeamModifyDto) {
            teamService.modifyGameResultInTeam(dto);
        }
        // modify users with game result
        modifyUsersPppAndPChange(game, team1, team2);
        endGameStatus(game);
        // modify users' ranks with game result
        throw new ResponseStatusException(HttpStatus.CREATED, "");
    }

    private void validateInput(GameResultRequestDto requestDto) {
        requestDto.getMyTeamScore();
        requestDto.getEnemyTeamScore();
    }

    @Override
    @GetMapping(value = "/games")
    public GameResultResponseDto gameResultByGameIdAndCount(GameResultPageRequestDto requestDto, HttpServletRequest request) {
        tokenService.findUserByAccessToken(HeaderUtil.getAccessToken(request));
        Pageable pageable = PageRequest.of(0, requestDto.getCount());
        GameResultPageDto resultPageDto = gameService.findGamesAfterId(GameFindDto.builder().id(requestDto.getGameId()).status(requestDto.getStatus()).pageable(pageable).build());
        List<GameResultDto> gameResultList = new ArrayList<>();
        List<GameDto> gameLists = resultPageDto.getGameList();
        Integer lastGameId;
        if (gameLists.size() == 0) {
            lastGameId = 0;
        } else {
            lastGameId = gameLists.get(gameLists.size() - 1).getId();
        }
        putResultInGames(gameResultList, gameLists, null);

        GameResultResponseDto gameResultResponse = GameResultResponseDto.builder()
                .games(gameResultList)
                .lastGameId(lastGameId)
                .totalPage(resultPageDto.getTotalPage())
                .currentPage(resultPageDto.getCurrentPage() + 1)
                .build();
        return gameResultResponse;
    }

    @Override
    @GetMapping(value = "/users/{intraId}/games")
    public GameResultResponseDto gameResultByUserIdAndByGameIdAndCount(String intraId, GameResultUserPageRequestDto requestDto, HttpServletRequest request) {
        //Pageable
        /*
         * 1. PChange에서 유저별 게임 목록을 가져온다
         * 2. 얘네를 바탕으로 게임을 다 긁어온다.
         * 3. 얘네를 DTO로 만들어준다
         */
        tokenService.findUserByAccessToken(HeaderUtil.getAccessToken(request));
        Pageable pageable = PageRequest.of(0, requestDto.getCount());
        PChangeFindDto findDto = PChangeFindDto.builder()
                .userId(intraId)
                .gameId(requestDto.getGameId())
                .pageable(pageable)
                .build();
        PChangePageDto pChangePageDto = pChangeService.findPChangeByUserIdAfterGameId(findDto);
        List<PChangeDto> pChangeLists = pChangePageDto.getPChangeList();
        List<GameDto> gameLists = pChangeLists.stream().map(PChangeDto::getGame).collect(Collectors.toList());;
        List<GameResultDto> gameResultList = new ArrayList<>();
        Integer lastGameId;
        if (gameLists.size() == 0) {
            lastGameId = 0;
        } else {
            lastGameId = gameLists.get(gameLists.size() - 1).getId();
        }

        putResultInGames(gameResultList, gameLists, intraId);

        GameResultResponseDto gameResultResponse = GameResultResponseDto.builder()
                .games(gameResultList)
                .lastGameId(lastGameId)
                .totalPage(pChangePageDto.getTotalPage())
                .currentPage(pChangePageDto.getCurrentPage() + 1)
                .build();
        return gameResultResponse;
    }


    private void checkIfUserHavePlayingGame(CurrentMatchDto currentMatch) {
        if (currentMatch == null) {
            throw new BusinessException("E0001");
        }
        GameDto game = currentMatch.getGame();
        if (game == null) {
            throw new BusinessException("E0001");
        }
    }

    private void endGameStatus(GameDto game) {
        GameModifyStatusDto modifyStatusDto = GameModifyStatusDto.builder()
                .gameId(game.getId())
                .status(StatusType.END).build();
        gameService.modifyGameStatus(modifyStatusDto);
    }

    private List<TeamModifyGameResultDto> getTeamModifyDto(TeamDto team1, TeamDto team2, GameResultRequestDto requestDto, UserDto user) {
        Integer team1Score;
        Integer team2Score;
        if (user.equals(team1.getUser1()) || user.equals(team1.getUser2())) {
            team1Score = requestDto.getMyTeamScore();
            team2Score = requestDto.getEnemyTeamScore();
        } else if (user.equals(team2.getUser1()) || user.equals(team2.getUser2())) {
            team2Score = requestDto.getMyTeamScore();
            team1Score = requestDto.getEnemyTeamScore();
        } else {
            throw new BusinessException("E0001");
        }
        List<TeamModifyGameResultDto> modifyList = new ArrayList<>();
        modifyList.add(TeamModifyGameResultDto.builder()
                .teamId(team1.getId())
                .score(team1Score)
                .win(team1Score > team2Score)
                .build()
        );
        modifyList.add(TeamModifyGameResultDto.builder()
                .teamId(team2.getId())
                .score(team2Score)
                .win(team2Score > team1Score)
                .build()
        );
        return modifyList;
    }

    private void modifyUsersPppAndPChange(GameDto game, TeamDto team1, TeamDto team2) {
        TeamDto savedTeam1 = teamService.findById(team1.getId());
        TeamDto savedTeam2 = teamService.findById(team2.getId());
        Boolean isOneSide = Math.abs(savedTeam1.getScore() - savedTeam2.getScore()) == 2;
        modifyUserPppAndAddPchangeAndRank(game, team1.getUser1(), savedTeam2, isOneSide);
        modifyUserPppAndAddPchangeAndRank(game, team1.getUser2(), savedTeam2, isOneSide);
        modifyUserPppAndAddPchangeAndRank(game, team2.getUser1(), savedTeam1, isOneSide);
        modifyUserPppAndAddPchangeAndRank(game, team2.getUser2(), savedTeam1, isOneSide);
    }
    
    private void modifyUserPppAndAddPchangeAndRank(GameDto game, UserDto user, TeamDto enemyTeam, Boolean isOneSide) {
        if (user == null) {
            return;
        }
        Integer pppChange = EloRating.pppChange(user.getPpp(), enemyTeam.getTeamPpp(), !enemyTeam.getWin(), isOneSide);
        Integer ppp = (user.getPpp() + pppChange);
        Integer userPpp = ppp > 0 ? ppp : 0;
        UserModifyPppDto modifyPppDto = UserModifyPppDto.builder()
                .userId(user.getId())
                .ppp(userPpp)
                .build();
        userService.modifyUserPpp(modifyPppDto);
        PChangeAddDto pChangeAddDto = PChangeAddDto.builder()
                .gameId(game.getId())
                .userId(user.getId())
                .pppChange(pppChange)
                .pppResult(userPpp)
                .build();
        pChangeService.addPChange(pChangeAddDto);
        GameType gameType = gameService.findById(game.getId()).getType();
        RankModifyDto rankModifyDto =  RankModifyDto.builder()
                .gameType(gameType)
                .Ppp(userPpp)
                .intraId(user.getIntraId())
                .isWin(!enemyTeam.getWin())
                .build();
        rankRedisService.modifyUserPpp(rankModifyDto);
    }

    private void removeCurrentMatch(GameDto game) {
    CurrentMatchFindDto findDto = CurrentMatchFindDto.builder()
            .game(game)
            .build();
    List<CurrentMatchDto> currentMatchDtos = currentMatchService.findCurrentMatchByGame(findDto);
    currentMatchDtos.forEach(currentMatchDto -> {
            CurrentMatchRemoveDto removeDto = CurrentMatchRemoveDto.builder()
                    .userId(currentMatchDto.getUserId()).build();
            currentMatchService.removeCurrentMatch(removeDto);
        });
    }

    private void putUsersDataInTeams(GameDto game, UserDto user, List<GameUserInfoDto> myTeams, List<GameUserInfoDto> enemyTeams) {
        //figuring out team number for myteam and enemyteam
        TeamPosDto teamPos = teamService.getTeamPosNT(user, game.getTeam1(), game.getTeam2());

        TeamDto myTeam = teamPos.getMyTeam();
        TeamDto enemyTeam = teamPos.getEnemyTeam();
        UserDto myTeamUser1 = myTeam.getUser1();
        UserDto myTeamUser2 = myTeam.getUser2();
        UserDto enemyTeamUser1 = enemyTeam.getUser1();
        UserDto enemyTeamUser2 = enemyTeam.getUser2();
        if (myTeamUser1 != null) {
            myTeams.add(GameUserInfoDto.builder().intraId(myTeamUser1.getIntraId()).userImageUri(myTeamUser1.getImageUri()).build());
        }
        if (myTeamUser2 != null) {
            myTeams.add(GameUserInfoDto.builder().intraId(myTeamUser2.getIntraId()).userImageUri(myTeamUser2.getImageUri()).build());
        }
        if (enemyTeamUser1 != null) {
            enemyTeams.add(GameUserInfoDto.builder().intraId(enemyTeamUser1.getIntraId()).userImageUri(enemyTeamUser1.getImageUri()).build());
        }
        if (enemyTeamUser2 != null) {
            enemyTeams.add(GameUserInfoDto.builder().intraId(enemyTeamUser2.getIntraId()).userImageUri(enemyTeamUser2.getImageUri()).build());
        }
    }
  
    private void putResultInGames(List<GameResultDto> gameResultList, List<GameDto> gameLists, String curUserId) {
        TeamDto team1;
        TeamDto team2;
        GameTeamDto teamDto1;
        GameTeamDto teamDto2;
        for (GameDto game : gameLists) {
            team1 = game.getTeam1();
            team2 = game.getTeam2();

            /* 왼 쪽 정 렬 */
            if (curUserId != null &&
                    ((team2.getUser1() != null && curUserId.equals(team2.getUser1().getIntraId()))
                    || (team2.getUser2() != null && curUserId.equals(team2.getUser2().getIntraId())))) {
                team1 = game.getTeam2();
                team2 = game.getTeam1();
            }
            teamDto1 = getTeamDtoFromGamePlayers(team1, game);
            teamDto2 = getTeamDtoFromGamePlayers(team2, game);

            gameResultList.add(GameResultDto.builder()
                    .gameId(game.getId())
                    .team1(teamDto1)
                    .team2(teamDto2)
                    .type(game.getType())
                    .status(game.getStatus())
                    .time(game.getTime())
                    .build());
        }
    }

    private GameTeamDto getTeamDtoFromGamePlayers(TeamDto team, GameDto game) {
        GameTeamDto teamDto;
        List<GamePlayerDto> gamePlayerList;
        gamePlayerList = new ArrayList<>();
        putGamePlayerDto(game, team.getUser1(), gamePlayerList);
        putGamePlayerDto(game, team.getUser2(), gamePlayerList);
        teamDto = GameTeamDto.builder()
                .isWin(team.getWin())
                .score(team.getScore())
                .players(gamePlayerList)
                .build();
        return teamDto;
    }

    private void putGamePlayerDto(GameDto game, UserDto user, List<GamePlayerDto> gamePlayerList) {
        if (user == null) {
            return;
        } else {
            Integer pppChange;
            if (!StatusType.END.equals(game.getStatus())) {
                pppChange = null;
            } else {
                PChangeDto pChangeDto = pChangeService.findPChangeByUserAndGame(PChangeFindDto.builder().gameId(game.getId()).userId(user.getIntraId()).build());
                pppChange = pChangeDto.getPppChange();
            }
            RankUserDto userRankDto = rankRedisService.findRankById(RankFindDto.builder().intraId(user.getIntraId()).gameType(game.getType()).build());
            GamePlayerDto gamePlayerDto = GamePlayerDto.builder()
                    .intraId(user.getIntraId())
                    .userImageUri(user.getImageUri())
                    .wins(userRankDto.getWins())
                    .losses(userRankDto.getLosses())
                    .winRate(userRankDto.getWinRate())
                    .pppChange(pppChange)
                    .build();
            gamePlayerList.add(gamePlayerDto);
        }
    }
}
