package io.pp.arcade.domain.team;


import io.pp.arcade.domain.admin.dto.create.TeamCreateRequestDto;
import io.pp.arcade.domain.admin.dto.delete.TeamDeleteDto;
import io.pp.arcade.domain.admin.dto.update.TeamUpdateDto;
import io.pp.arcade.domain.game.dto.GameDto;
import io.pp.arcade.domain.slot.dto.SlotDto;
import io.pp.arcade.domain.team.dto.*;
import io.pp.arcade.domain.user.User;
import io.pp.arcade.domain.user.UserRepository;
import io.pp.arcade.domain.user.dto.UserDto;
import io.pp.arcade.global.exception.BusinessException;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class TeamService {
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    @Transactional
    public TeamDto findById(Integer id) {
        Team team = teamRepository.findById(id).orElseThrow(() -> new BusinessException("E0001"));
        return TeamDto.from(team);
    }

    @Transactional
    public void addUserInTeam(TeamAddUserDto dto) {
        User user = userRepository.findById(dto.getUserId()).orElseThrow(() -> new BusinessException("E0001"));
        Team team = teamRepository.findById(dto.getTeamId()).orElseThrow(() -> new BusinessException("E0001"));

        if (team.getUser1() == null) {
            team.setUser1(user);
        } else {
            team.setUser2(user);
        }
        team.setTeamPpp((user.getPpp() + team.getTeamPpp()) / (team.getHeadCount() + 1));
        team.setHeadCount(team.getHeadCount() + 1);
    }

    @Transactional
    public void removeUserInTeam(TeamRemoveUserDto dto) {
        User user = userRepository.findById(dto.getUserId()).orElseThrow(() -> new BusinessException("E0001"));
        Team team = teamRepository.findById(dto.getTeamId()).orElseThrow(() -> new BusinessException("E0001"));

        if (user.equals(team.getUser1())) {
            team.setUser1(null);
        } else {
            team.setUser2(null);
        } // id가 user1이나 user2 둘중 하나로 validation 거쳐서 들어옴
        Integer headCountResult = team.getHeadCount() - 1; // entity라 반영이 안되어서 미리 뺀 값을 써줘야함
        if (headCountResult == 0) {
            team.setTeamPpp(0);
        } else {
            team.setTeamPpp((team.getTeamPpp() * team.getHeadCount() - user.getPpp()) / headCountResult);
        }
        team.setHeadCount(headCountResult);
    }


    @Transactional
    public void saveGameResultInTeam(TeamSaveGameResultDto dto) {
        Team team = teamRepository.findById(dto.getTeamId()).orElseThrow(() -> new BusinessException("E0001"));

        team.setScore(dto.getScore());
        team.setWin(dto.getWin());
    }

    @Transactional
    public void modifyGameResultInTeam(TeamModifyGameResultDto dto) {
        Team team = teamRepository.findById(dto.getTeamId()).orElseThrow(() -> new BusinessException("E0001"));

        team.setScore(dto.getScore());
        team.setWin(dto.getWin());
    }

    public TeamPosDto getTeamPosNT(UserDto curUser, TeamDto team1, TeamDto team2) {
        TeamDto myTeamDto;
        TeamDto enemyTeamDto;

        if (curUser.equals(team1.getUser1()) || curUser.equals(team1.getUser2())) {
            myTeamDto = team1;
            enemyTeamDto = team2;
        } else if (curUser.equals(team2.getUser1()) || curUser.equals(team2.getUser2())) {
            myTeamDto = team2;
            enemyTeamDto = team1;
        } else {
            throw new BusinessException("E0001");
        }
        TeamPosDto dto = TeamPosDto.builder()
                .myTeam(myTeamDto)
                .enemyTeam(enemyTeamDto)
                .build();
        return dto;
    }

    @Transactional
    public void createTeamByAdmin(TeamCreateRequestDto teamCreateDto) {
        teamRepository.save(Team.builder()
                .teamPpp(teamCreateDto.getTeamPpp())
                .headCount(teamCreateDto.getHeadCount())
                .score(teamCreateDto.getScore())
                .build());
    }

    @Transactional
    public void updateTeamByAdmin(TeamUpdateDto teamUpdateDto) {
        Team team = teamRepository.findById(teamUpdateDto.getTeamId()).orElse(null);
        team.setUser1(userRepository.findById(teamUpdateDto.getUser1Id()).orElse(null));
        team.setUser2(userRepository.findById(teamUpdateDto.getUser2Id()).orElse(null));
        team.setTeamPpp(teamUpdateDto.getTeamPpp());
        team.setHeadCount(teamUpdateDto.getHeadCount());
        team.setScore(teamUpdateDto.getScore());
        team.setWin(teamUpdateDto.getWin());
    }

    @Transactional
    public List<TeamDto> findTeamByAdmin(Pageable pageable) {
        Page<Team> teams = teamRepository.findAllByOrderByIdDesc(pageable);
        List<TeamDto> teamDtos = teams.stream().map(TeamDto::from).collect(Collectors.toList());
        return teamDtos;
    }

    @Transactional
    public void deleteTeamByAdmin(TeamDeleteDto teamDeleteDto) {
        teamRepository.deleteById(teamDeleteDto.getTeamId());
    }

    /* 아이디를 통한 팀 추가 */
}
