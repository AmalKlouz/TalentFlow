package tn.esprit.talentflow.service;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import tn.esprit.talentflow.dto.InterviewRequest;
import tn.esprit.talentflow.dto.InterviewResponse;
import tn.esprit.talentflow.entity.Application;
import tn.esprit.talentflow.entity.ApplicationStatus;
import tn.esprit.talentflow.entity.Interview;
import tn.esprit.talentflow.entity.InterviewStatus;
import tn.esprit.talentflow.entity.NotificationType;
import tn.esprit.talentflow.entity.User;
import tn.esprit.talentflow.exception.ResourceNotFoundException;
import tn.esprit.talentflow.repository.ApplicationRepository;
import tn.esprit.talentflow.repository.InterviewRepository;
import tn.esprit.talentflow.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InterviewService {

    private final InterviewRepository interviewRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    // =========================================================
    // CREATE INTERVIEW
    // =========================================================

    public InterviewResponse createInterview(
            InterviewRequest request) {

        Application application =
                applicationRepository.findById(
                        request.getApplicationId()
                ).orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Candidature introuvable"
                        )
                );

        // L'entretien est possible uniquement
        // pour une candidature shortlistée
        // ou déjà en statut INTERVIEW

        if (application.getStatus() != ApplicationStatus.SHORTLISTED
                && application.getStatus() != ApplicationStatus.INTERVIEW) {

            throw new IllegalArgumentException(
                    "Un entretien peut uniquement être planifié "
                            + "pour une candidature shortlistée"
            );
        }

        // Une seule interview par candidature

        if (interviewRepository.existsByApplicationId(
                application.getId())) {

            throw new IllegalArgumentException(
                    "Un entretien existe déjà pour cette candidature"
            );
        }

        Interview interview = Interview.builder()
                .application(application)
                .scheduledAt(request.getScheduledAt())
                .duration(request.getDuration())
                .type(request.getType())
                .location(request.getLocation())
                .notes(request.getNotes())
                .status(InterviewStatus.SCHEDULED)
                .build();

        // La candidature passe automatiquement
        // au statut INTERVIEW

        application.setStatus(ApplicationStatus.INTERVIEW);
        applicationRepository.save(application);

        Interview savedInterview =
                interviewRepository.save(interview);

        // =====================================================
        // NOTIFICATION CANDIDATE
        // =====================================================

        notificationService.createNotification(
                application.getCandidate(),
                NotificationType.INTERVIEW_SCHEDULED,
                "Un entretien a été planifié pour votre candidature "
                        + "au poste : "
                        + application.getJob().getTitle()
        );

        return toResponse(savedInterview);
    }

    // =========================================================
    // GET ALL INTERVIEWS
    // =========================================================

    public List<InterviewResponse> getAllInterviews() {

        return interviewRepository
                .findAllByOrderByScheduledAtAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // =========================================================
    // GET INTERVIEWS BY APPLICATION
    // =========================================================

    public List<InterviewResponse> getInterviewsByApplication(
            Long applicationId) {

        if (!applicationRepository.existsById(applicationId)) {

            throw new ResourceNotFoundException(
                    "Candidature introuvable"
            );
        }

        return interviewRepository
                .findByApplicationId(applicationId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // =========================================================
    // GET MY INTERVIEWS - CANDIDATE
    // =========================================================

    public List<InterviewResponse> getMyInterviews() {

        User candidate = getCurrentUser();

        return interviewRepository
                .findByApplicationCandidateId(
                        candidate.getId()
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // =========================================================
    // GET INTERVIEWS BY CANDIDATE
    // =========================================================

    public List<InterviewResponse> getInterviewsByCandidate(
            Long candidateId) {

        if (!userRepository.existsById(candidateId)) {
            throw new ResourceNotFoundException(
                    "Candidat introuvable"
            );
        }

        return interviewRepository
                .findByApplicationCandidateId(candidateId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // =========================================================
    // UPDATE INTERVIEW STATUS
    // =========================================================

    public InterviewResponse updateStatus(
            Long id,
            InterviewStatus newStatus) {

        Interview interview =
                interviewRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Entretien introuvable"
                                )
                        );

        InterviewStatus currentStatus =
                interview.getStatus();

        // =====================================================
        // VALID STATUS TRANSITIONS
        // =====================================================

        boolean validTransition = switch (currentStatus) {

            case SCHEDULED ->
                    newStatus == InterviewStatus.COMPLETED
                            || newStatus == InterviewStatus.CANCELLED;

            case COMPLETED, CANCELLED ->
                    false;
        };

        if (!validTransition) {

            throw new IllegalArgumentException(
                    "Transition impossible de "
                            + currentStatus
                            + " vers "
                            + newStatus
            );
        }

        interview.setStatus(newStatus);

        Interview updatedInterview =
                interviewRepository.save(interview);

        // =====================================================
        // NOTIFICATION CANDIDATE
        // =====================================================

        notificationService.createNotification(
                interview.getApplication().getCandidate(),
                NotificationType.INTERVIEW_STATUS_CHANGED,
                "Le statut de votre entretien pour le poste \""
                        + interview.getApplication()
                        .getJob()
                        .getTitle()
                        + "\" a été changé vers : "
                        + newStatus
        );

        return toResponse(updatedInterview);
    }

    // =========================================================
    // DELETE INTERVIEW
    // =========================================================

    public void deleteInterview(Long id) {

        Interview interview =
                interviewRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Entretien introuvable"
                                )
                        );

        Application application =
                interview.getApplication();

        interviewRepository.delete(interview);

        // Si l'entretien est supprimé,
        // la candidature redevient shortlistée.

        if (application.getStatus() == ApplicationStatus.INTERVIEW) {
            application.setStatus(
                    ApplicationStatus.SHORTLISTED
            );

            applicationRepository.save(application);
        }
    }

    // =========================================================
    // CURRENT USER
    // =========================================================

    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String email = authentication.getName();

        return userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Utilisateur connecté introuvable"
                        )
                );
    }

    // =========================================================
    // ENTITY → DTO
    // =========================================================

    private InterviewResponse toResponse(
            Interview interview) {

        Application application =
                interview.getApplication();

        User candidate =
                application.getCandidate();

        var job =
                application.getJob();

        return InterviewResponse.builder()

                .id(interview.getId())

                .applicationId(
                        application.getId()
                )

                .candidateId(
                        candidate.getId()
                )

                .candidateName(
                        candidate.getFirstName()
                                + " "
                                + candidate.getLastName()
                )

                .candidateEmail(
                        candidate.getEmail()
                )

                .jobId(
                        job.getId()
                )

                .jobTitle(
                        job.getTitle()
                )

                .scheduledAt(
                        interview.getScheduledAt()
                )

                .duration(
                        interview.getDuration()
                )

                .type(
                        interview.getType()
                )

                .location(
                        interview.getLocation()
                )

                .notes(
                        interview.getNotes()
                )

                .status(
                        interview.getStatus()
                )

                .build();
    }
}