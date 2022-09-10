package io.pp.arcade.domain.admin.management;

import io.pp.arcade.domain.currentmatch.CurrentMatchService;
import io.pp.arcade.domain.currentmatch.dto.CurrentMatchDto;
import io.pp.arcade.domain.feedback.FeedbackService;
import io.pp.arcade.domain.feedback.dto.FeedbackDto;
import io.pp.arcade.domain.game.GameService;
import io.pp.arcade.domain.game.dto.GameDto;
import io.pp.arcade.domain.noti.NotiService;
import io.pp.arcade.domain.noti.dto.NotiDto;
import io.pp.arcade.domain.pchange.PChangeService;
import io.pp.arcade.domain.pchange.dto.PChangeDto;
import io.pp.arcade.domain.rank.dto.RankDto;
import io.pp.arcade.domain.rank.service.RankService;
import io.pp.arcade.domain.season.SeasonService;
import io.pp.arcade.domain.season.dto.SeasonDto;
import io.pp.arcade.domain.slot.SlotService;
import io.pp.arcade.domain.slot.dto.SlotDto;
import io.pp.arcade.domain.team.TeamService;
import io.pp.arcade.domain.team.dto.TeamDto;
import io.pp.arcade.domain.user.UserService;
import io.pp.arcade.domain.user.dto.UserDto;
import io.pp.arcade.global.scheduler.CurrentMatchUpdater;
import io.pp.arcade.global.scheduler.GameGenerator;
import io.pp.arcade.global.scheduler.RankScheduler;
import io.pp.arcade.global.scheduler.SlotGenerator;
import io.pp.arcade.global.type.FeedbackType;
import io.pp.arcade.global.type.GameType;
import io.pp.arcade.global.type.RoleType;
import io.pp.arcade.global.util.HeaderUtil;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@AllArgsConstructor
public class ManagementController {
    private final UserService userService;
    private final TeamService teamService;
    private final SlotService slotService;
    private final GameService gameService;
    private final SeasonService seasonService;
    private final CurrentMatchService currentMatchService;
    private final NotiService notiService;
    private final PChangeService pChangeService;
    private final RankService rankService;
    private final FeedbackService feedbackService;
    private final CurrentMatchUpdater currentMatchUpdater;
    private final SlotGenerator slotGenerator;
    private final GameGenerator gameGenerator;
    private final RankScheduler rankScheduler;

    /* 관리자 관리 */
    @GetMapping("/admin/admin")
    public String adminPage(Model model, HttpServletRequest request) {
        List<UserDto> userList = userService.findAllByRoleType(RoleType.USER);
        List<UserDto> adminList = userService.findAllByRoleType(RoleType.ADMIN);


        model.addAttribute("adminList", adminList);
        model.addAttribute("userList", userList);
        model.addAttribute("token", HeaderUtil.getAccessToken(request));
        return "admin_management";
    }

    /* 게임 결과 관리 */
    @GetMapping("/admin/game")
    public String gameResultPage(Model model, HttpServletRequest request) {
        List<GameDto> singleGameList = gameService.findGameByTypeByAdmin(Pageable.unpaged(), GameType.SINGLE);
        List<GameDto> bungleGameList = gameService.findGameByTypeByAdmin(Pageable.unpaged(), GameType.DOUBLE);
        model.addAttribute("singleGameList", singleGameList);
        model.addAttribute("bungleGameList", bungleGameList);
        model.addAttribute("token", HeaderUtil.getAccessToken(request));
        return "game_management";
    }

    @GetMapping("/admin/game_result")
    public GameDto gameResultCheck(Model model, @RequestParam Integer gameId, HttpServletRequest request) {
        GameDto game = gameService.findById(gameId);
        return game;
    }

    /* 시즌 관리 */
    @GetMapping("/admin/season")
    public String seasonPage(Model model, HttpServletRequest request) {
        List<SeasonDto> seasonList = seasonService.findSeasonsByAdmin(Pageable.unpaged());

        model.addAttribute("seasonList", seasonList);
        model.addAttribute("token", HeaderUtil.getAccessToken(request));
        return "season_management";
    }

    @GetMapping("/admin/currentMatch")
    public String currentMatchPage(Model model, HttpServletRequest request) {
        List<CurrentMatchDto> currentMatchList = currentMatchService.findCurrentMatchByAdmin(Pageable.unpaged());

        model.addAttribute("currentMatchList", currentMatchList);
        model.addAttribute("token", HeaderUtil.getAccessToken(request));
        return "current_match_management";
    }

    @GetMapping("/admin/noti")
    public String notiPage(Model model, HttpServletRequest request) {
        List<NotiDto> notiList = notiService.findNotiByAdmin(Pageable.unpaged());

        model.addAttribute("notiList", notiList);
        model.addAttribute("token", HeaderUtil.getAccessToken(request));
        return "noti_management";
    }

    @GetMapping("/admin/pChange")
    public String pchangePage(Model model, HttpServletRequest request) {
        List<PChangeDto> pchangeList = pChangeService.findPChangeByAdmin(Pageable.unpaged());

        model.addAttribute("pchangeList", pchangeList);
        model.addAttribute("token", HeaderUtil.getAccessToken(request));
        return "pchange_management";
    }

    @GetMapping("/admin/rank")
    public String rankPage(Model model, HttpServletRequest request) {
        List<RankDto> rankList = rankService.findRankByAdmin(Pageable.unpaged());

        model.addAttribute("rankList", rankList);
        model.addAttribute("token", HeaderUtil.getAccessToken(request));
        return "rank_management";
    }

    /* 스케쥴러 관리 */
    @GetMapping("/admin/scheduler")
    public String schedularPage(Model model, HttpServletRequest request) {
        model.addAttribute("currentCron", currentMatchUpdater.getCron());
        model.addAttribute("slotCron", slotGenerator.getCron());
        model.addAttribute("gameCron", gameGenerator.getCron());
        model.addAttribute("rankCron", rankScheduler.getCron());
        model.addAttribute("token", HeaderUtil.getAccessToken(request));
        return "scheduler_management";
    }

    @GetMapping("/admin/slot")
    public String slotPage(Model model, HttpServletRequest request) {
        List<SlotDto> slotList = slotService.findSlotByAdmin(Pageable.unpaged());
        model.addAttribute("slotStartTime", slotGenerator.getStartTime());
        model.addAttribute("slotInterval", slotGenerator.getInterval());
        model.addAttribute("slotNum", slotGenerator.getSlotNum());
        model.addAttribute("slotList", slotList);
        model.addAttribute("token", HeaderUtil.getAccessToken(request));
        return "slot_management";
    }

    @GetMapping("/admin/user")
    public String userPage(Model model, HttpServletRequest request) {
        List<UserDto> userList = userService.findUserByAdmin(Pageable.unpaged());
        model.addAttribute("userList", userList);
        return "user_management";
    }

    @GetMapping("/admin/team")
    public String teamPage(Model model, HttpServletRequest request) {
        List<TeamDto> teamList = teamService.findTeamByAdmin(Pageable.unpaged());
        model.addAttribute("teamList", teamList);
        return "team_management";
    }

    @GetMapping("/admin/feedback")
    public String feedbackPage(Model model, HttpServletRequest request) {
        List<FeedbackDto> solvedFeedbackList = feedbackService.feedbackFindByIsSolvedByAdmin(true, Pageable.unpaged());
        List<FeedbackDto> unsolvedFeedbackList = feedbackService.feedbackFindByIsSolvedByAdmin(false, Pageable.unpaged());

        model.addAttribute("solvedFeedbackList", solvedFeedbackList);
        model.addAttribute("unsolvedFeedbackList", unsolvedFeedbackList);
        return "feedback_management";
    }

    @GetMapping("/admin/feedbackcategory")
    public String feedbackCategorizedPage(Model model, HttpServletRequest request) {
        List<FeedbackDto> bugFeedbackList = feedbackService.feedbackFindByCategoryByAdmin(FeedbackType.BUG, Pageable.unpaged());
        List<FeedbackDto> gameResultFeedbackList = feedbackService.feedbackFindByCategoryByAdmin(FeedbackType.GAMERESULT, Pageable.unpaged());
        List<FeedbackDto> complaintFeedbackList = feedbackService.feedbackFindByCategoryByAdmin(FeedbackType.COMPLAINT, Pageable.unpaged());
        List<FeedbackDto> cheersFeedbackList = feedbackService.feedbackFindByCategoryByAdmin(FeedbackType.CHEERS, Pageable.unpaged());
        List<FeedbackDto> opinionFeedbackList = feedbackService.feedbackFindByCategoryByAdmin(FeedbackType.OPINION, Pageable.unpaged());
        List<FeedbackDto> etcFeedbackList = feedbackService.feedbackFindByCategoryByAdmin(FeedbackType.ETC, Pageable.unpaged());

        model.addAttribute("bugFeedbackList", bugFeedbackList);
        model.addAttribute("gameResultFeedbackList", gameResultFeedbackList);
        model.addAttribute("complaintFeedbackList", complaintFeedbackList);
        model.addAttribute("cheersFeedbackList", cheersFeedbackList);
        model.addAttribute("opinionFeedbackList", opinionFeedbackList);
        model.addAttribute("etcFeedbackList", etcFeedbackList);
        return "feedback_list_by_category";
    }

    @GetMapping("/admin")
    public String mainPage(HttpServletRequest request) {
        return "layout/admin";
    }
}
