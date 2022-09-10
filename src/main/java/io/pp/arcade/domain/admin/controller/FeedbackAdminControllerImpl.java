package io.pp.arcade.domain.admin.controller;

import io.pp.arcade.domain.feedback.FeedbackService;
import io.pp.arcade.domain.feedback.dto.FeedbackDto;
import io.pp.arcade.domain.feedback.dto.FeedbackFindRequestDto;
import io.pp.arcade.domain.admin.dto.update.FeedbackUpdateDto;
import io.pp.arcade.domain.admin.dto.update.FeedbackUpdateRequestDto;
import io.pp.arcade.global.type.FeedbackType;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping(value = "/admin")
public class FeedbackAdminControllerImpl implements FeedbackAdminController {
    private final FeedbackService feedbackService;

    /* 관리자 측에서 처리 하고 isSolved = true로 바꿔주기 */
    @Override
    @PutMapping(value = "/feedback")
    public void feedbackUpdate(FeedbackUpdateRequestDto updateRequestDto, HttpServletRequest request) {
        FeedbackDto feedbackDto = feedbackService.findFeedbackById(updateRequestDto.getFeedbackId());
        FeedbackUpdateDto updateDto = FeedbackUpdateDto.builder()
                .feedbackId(updateRequestDto.getFeedbackId())
                .isSolved(updateRequestDto.getIsSolved())
                .build();
        feedbackService.updateFeedbackByAdmin(updateDto);
    }

    @Override
    @DeleteMapping(value = "/feedback/{feedbackId}")
    public void feedbackDelete(Integer feedbackId, HttpServletRequest request) {
        FeedbackDto feedbackDto = feedbackService.findFeedbackById(feedbackId);
        feedbackService.deleteFeedbackByAdmin(feedbackDto);
    }

    @Override
    @GetMapping(value = "/feedback/all")
    public List<FeedbackDto> feedbackAll(Pageable pageable, HttpServletRequest request) {
        List<FeedbackDto> feedbackDtos = feedbackService.findFeedbackByAdmin(pageable);
        return feedbackDtos;
    }

    @Override
    @GetMapping(value = "/feedback/{category}")
    public List<FeedbackDto> feedbackFindByCategory(FeedbackType category, Pageable pageable, HttpServletRequest request) {
        List<FeedbackDto> feedbackDtos = feedbackService.feedbackFindByCategoryByAdmin(category, pageable);
        return feedbackDtos;
    }

    @Override
    @GetMapping(value = "/feedback/{isSolved}")
    public List<FeedbackDto> feedbackFindByIsSolved(Boolean isSolved, Pageable pageable, HttpServletRequest request) {
        List<FeedbackDto> feedbackDtos = feedbackService.feedbackFindByIsSolvedByAdmin(isSolved, pageable);
        return feedbackDtos;
    }

    @Override
    @GetMapping(value = "/feedback/categorized")
    public List<FeedbackDto> feedbackFindByCategoryAndIsSolved(FeedbackFindRequestDto findRequestDto, Pageable pageable, HttpServletRequest request) {
        FeedbackType category = findRequestDto.getCategory();
        Boolean isSolved = findRequestDto.getIsSolved();
        List<FeedbackDto> feedbackDtos = feedbackService.feedbackFindByCategoryAndIsSolvedByAdmin(category, isSolved, pageable);
        return feedbackDtos;
    }
}
