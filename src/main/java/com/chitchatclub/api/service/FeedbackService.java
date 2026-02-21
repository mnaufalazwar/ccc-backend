package com.chitchatclub.api.service;

import com.chitchatclub.api.dto.request.FeedbackRequest;
import com.chitchatclub.api.dto.response.FeedbackResponse;
import com.chitchatclub.api.entity.Feedback;
import com.chitchatclub.api.entity.Session;
import com.chitchatclub.api.entity.User;
import com.chitchatclub.api.exception.BadRequestException;
import com.chitchatclub.api.exception.ResourceNotFoundException;
import com.chitchatclub.api.repository.FeedbackRepository;
import com.chitchatclub.api.repository.RegistrationRepository;
import com.chitchatclub.api.repository.SessionRepository;
import com.chitchatclub.api.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final RegistrationRepository registrationRepository;
    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;

    public FeedbackService(FeedbackRepository feedbackRepository,
                           RegistrationRepository registrationRepository,
                           SessionRepository sessionRepository,
                           UserRepository userRepository) {
        this.feedbackRepository = feedbackRepository;
        this.registrationRepository = registrationRepository;
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
    }

    public FeedbackResponse submitFeedback(UUID sessionId, User fromUser, FeedbackRequest request) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + sessionId));

        if (!registrationRepository.existsBySessionIdAndUserId(sessionId, fromUser.getId())) {
            throw new BadRequestException("You are not registered for this session");
        }

        Feedback feedback = new Feedback();
        feedback.setSession(session);
        feedback.setFromUser(fromUser);
        feedback.setRating(request.rating());
        feedback.setText(request.text());
        feedback.setAnonymous(request.anonymous() != null && request.anonymous());

        if (request.toUserId() != null) {
            User toUser = userRepository.findById(request.toUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Target user not found"));

            if (!registrationRepository.existsBySessionIdAndUserId(sessionId, toUser.getId())) {
                throw new BadRequestException("Target user is not registered for this session");
            }
            feedback.setToUser(toUser);
        }

        feedback = feedbackRepository.save(feedback);
        return FeedbackResponse.fromEntity(feedback);
    }

    public List<FeedbackResponse> getMyFeedback(UUID sessionId, UUID userId) {
        return feedbackRepository.findBySessionIdAndFromUserId(sessionId, userId).stream()
                .map(FeedbackResponse::fromEntity)
                .toList();
    }

    public List<FeedbackResponse> getReceivedFeedback(UUID sessionId, UUID userId) {
        return feedbackRepository.findBySessionIdAndToUserId(sessionId, userId).stream()
                .map(FeedbackResponse::fromEntityHideAuthor)
                .toList();
    }

    public List<FeedbackResponse> getSessionFeedback(UUID sessionId) {
        return feedbackRepository.findBySessionIdAndToUserIsNull(sessionId).stream()
                .map(FeedbackResponse::fromEntityHideAuthor)
                .toList();
    }
}
