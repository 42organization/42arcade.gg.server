package io.pp.arcade.domain.feedback;

import io.pp.arcade.domain.feedback.dto.FeedbackAddDto;
import io.pp.arcade.domain.feedback.dto.FeedbackDto;
import io.pp.arcade.domain.admin.dto.update.FeedbackUpdateDto;
import io.pp.arcade.domain.user.User;
import io.pp.arcade.domain.user.UserRepository;
import io.pp.arcade.global.exception.BusinessException;
import io.pp.arcade.global.type.FeedbackType;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class FeedbackService {
    private FeedbackRepository feedbackRepository;
    private UserRepository userRepository;

    @Transactional
    public void addFeedback(FeedbackAddDto addDto) {
        User user = userRepository.findById(addDto.getUserId()).orElseThrow(() -> new BusinessException("E0001"));
        Feedback feedback = Feedback.builder()
                .user(user)
                .category(addDto.getCategory())
                .content(addDto.getContent())
                .build();
        feedbackRepository.save(feedback);
    }

    @Transactional
    public FeedbackDto findFeedbackById(Integer feedbackId) {
        Feedback feedback = feedbackRepository.findById(feedbackId).orElseThrow(() -> new BusinessException("E0001"));
        FeedbackDto feedbackDto = FeedbackDto.builder()
                .id(feedback.getId())
                .category(feedback.getCategory())
                .content(feedback.getContent())
                .isSolved(feedback.getIsSolved())
                .build();
        return feedbackDto;
    }

    @Transactional
    public void updateFeedbackByAdmin(FeedbackUpdateDto updateDto) {
        Feedback feedback = feedbackRepository.findById(updateDto.getFeedbackId()).orElseThrow(() -> new BusinessException("E0001"));
        feedback.setIsSolved(updateDto.getIsSolved());
    }

    @Transactional
    public void deleteFeedbackByAdmin(FeedbackDto feedbackDto) {
        Feedback feedback = feedbackRepository.findById(feedbackDto.getId()).orElseThrow(() -> new BusinessException("E0001"));
        feedbackRepository.delete(feedback);
    }

    @Transactional
    public List<FeedbackDto> findFeedbackByAdmin(Pageable pageable) {
        Page<Feedback> feedbacks = feedbackRepository.findAll(pageable);
        List<FeedbackDto> feedbackDtos = feedbacks.stream().map(FeedbackDto::from).collect(Collectors.toList());
        return feedbackDtos;
    }

    @Transactional
    public List<FeedbackDto> feedbackFindByCategoryByAdmin(FeedbackType category, Pageable pageable) {
        Page<Feedback> feedbacks = feedbackRepository.findAllByCategory(category, pageable);
        List<FeedbackDto> feedbackDtos = feedbacks.stream().map(FeedbackDto::from).collect(Collectors.toList());
        return feedbackDtos;
    }

    @Transactional
    public List<FeedbackDto> feedbackFindByIsSolvedByAdmin(Boolean isSolved, Pageable pageable) {
        Page<Feedback> feedbacks = feedbackRepository.findAllByIsSolved(isSolved, pageable);
        List<FeedbackDto> feedbackDtos = feedbacks.stream().map(FeedbackDto::from).collect(Collectors.toList());
        return feedbackDtos;
    }

    @Transactional
    public List<FeedbackDto> feedbackFindByCategoryAndIsSolvedByAdmin(FeedbackType category, Boolean isSolved, Pageable pageable) {
        Page<Feedback> feedbacks = feedbackRepository.findAllByCategoryAndIsSolved(category, isSolved, pageable);
        List<FeedbackDto> feedbackDtos = feedbacks.stream().map(FeedbackDto::from).collect(Collectors.toList());
        return feedbackDtos;
    }
}
