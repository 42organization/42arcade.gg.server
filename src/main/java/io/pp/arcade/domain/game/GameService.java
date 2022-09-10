package io.pp.arcade.domain.game;

import io.pp.arcade.domain.admin.dto.create.GameCreateDto;
import io.pp.arcade.domain.admin.dto.delete.GameDeleteDto;
import io.pp.arcade.domain.game.dto.*;
import io.pp.arcade.domain.season.Season;
import io.pp.arcade.domain.season.SeasonRepository;
import io.pp.arcade.domain.slot.Slot;
import io.pp.arcade.domain.slot.SlotRepository;
import io.pp.arcade.domain.slot.dto.SlotDto;
import io.pp.arcade.domain.team.Team;
import io.pp.arcade.domain.team.TeamRepository;
import io.pp.arcade.global.exception.BusinessException;
import io.pp.arcade.global.type.GameType;
import io.pp.arcade.global.type.StatusType;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class GameService {
    private final GameRepository gameRepository;
    private final TeamRepository teamRepository;
    private final SlotRepository slotRepository;
    private final SeasonRepository seasonRepository;

    @Transactional
    public GameDto findById(Integer gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new BusinessException("E0001"));
        GameDto dto = GameDto.from(game);
        return dto;
    }

    @Transactional
    public void addGame(GameAddDto addDto) {
        SlotDto slotDto = addDto.getSlotDto();
        Season season = seasonRepository.findSeasonByStartTimeIsBeforeAndEndTimeIsAfter(LocalDateTime.now(), LocalDateTime.now()).orElse(null);
        Slot slot = slotRepository.findById(slotDto.getId()).orElseThrow(() -> new BusinessException("E0001"));
        Team team1 = teamRepository.findById(slotDto.getTeam1().getId()).orElseThrow(() -> new BusinessException("E0001"));
        Team team2 = teamRepository.findById(slotDto.getTeam2().getId()).orElseThrow(() -> new BusinessException("E0001"));
        gameRepository.save(Game.builder()
                .slot(slot)
                .team1(team1)
                .team2(team2)
                .type(slotDto.getType())
                .time(slotDto.getTime())
                .status(StatusType.LIVE)
                .season(season == null ? 1 : season.getId()) //season 추가
                .build()
        );
    }

    @Transactional
    public void modifyGameStatus(GameModifyStatusDto modifyStatusDto) {
        Game game = gameRepository.findById(modifyStatusDto.getGameId()).orElseThrow(() -> new BusinessException("E0001"));
        game.setStatus(modifyStatusDto.getStatus());
    }

    @Transactional
    public GameDto findBySlot(Integer slotId) {
        Slot slot = slotRepository.findById(slotId).orElseThrow(() -> new BusinessException("E0001"));
        GameDto game = GameDto.from(gameRepository.findBySlot(slot).orElseThrow(() -> new BusinessException("E0001")));
        return game;
    }

    @Transactional
    public GameDto findBySlotIdNullable(Integer slotId) {
        Game game = gameRepository.findBySlotId(slotId).orElse(null);
        GameDto gameDto = game == null ? null : GameDto.from(game);
        return gameDto;
    }

    @Transactional
    public GameResultPageDto findGamesAfterId(GameFindDto findDto) {
        Page<Game> games;
        if (findDto.getStatus() != null) {
            games = gameRepository.findByIdLessThanAndStatusOrderByTimeDesc(findDto.getId(), findDto.getStatus(), findDto.getPageable());
        } else {
            games = gameRepository.findByIdLessThanOrderByTimeDesc(findDto.getId(), findDto.getPageable());
        }
        List<GameDto> gameDtoList = games.stream().map(GameDto::from).collect(Collectors.toList());

        GameResultPageDto resultPageDto = GameResultPageDto.builder()
                .gameList(gameDtoList)
                .totalPage(games.getTotalPages())
                .currentPage(games.getNumber())
                .build();
        return resultPageDto;
    }

    @Transactional
    public void createGameByAdmin(GameCreateDto createDto) {
        Slot slot = slotRepository.findById(createDto.getSlotId()).orElseThrow(null);
        Game game = Game.builder()
                .slot(slot)
                .team1(slot.getTeam1())
                .team2(slot.getTeam2())
                .time(slot.getTime())
                .season(createDto.getSeasonId())
                .time(slot.getTime())
                .type(slot.getType())
                .status(createDto.getStatus()).build();
        gameRepository.save(game);
    }

    @Transactional
    public void deleteGameByAdmin(GameDeleteDto deleteDto){
        Game game = gameRepository.findById(deleteDto.getGameId()).orElseThrow(null);
        gameRepository.delete(game);
    }

    @Transactional
    public List<GameDto> findGameByAdmin(Pageable pageable) {
        Page<Game> games = gameRepository.findAll(pageable);
        List<GameDto> gameDtos = games.stream().map(GameDto::from).collect(Collectors.toList());
        return gameDtos;
    }

    @Transactional
    public List<GameDto> findGameByTypeByAdmin(Pageable pageable, GameType type) {
        Page<Game> games = gameRepository.findAllByTypeOrderByIdDesc(pageable, type);
        List<GameDto> gameDtos = games.stream().map(GameDto::from).collect(Collectors.toList());
        return gameDtos;
    }
}