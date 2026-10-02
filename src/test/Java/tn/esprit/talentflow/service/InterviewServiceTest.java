package tn.esprit.talentflow.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import tn.esprit.talentflow.dto.InterviewRequest;
import tn.esprit.talentflow.dto.InterviewResponse;
import tn.esprit.talentflow.entity.Application;
import tn.esprit.talentflow.entity.ApplicationStatus;
import tn.esprit.talentflow.entity.Interview;
import tn.esprit.talentflow.entity.InterviewStatus;
import tn.esprit.talentflow.entity.InterviewType;
import tn.esprit.talentflow.entity.Job;
import tn.esprit.talentflow.entity.NotificationType;
import tn.esprit.talentflow.entity.Role;
import tn.esprit.talentflow.entity.User;
import tn.esprit.talentflow.exception.ResourceNotFoundException;
import tn.esprit.talentflow.repository.ApplicationRepository;
import tn.esprit.talentflow.repository.InterviewRepository;
import tn.esprit.talentflow.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InterviewServiceTest {

    @Mock
    private InterviewRepository interviewRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private InterviewService interviewService;

    private User candidate;
    private Job job;
    private Application application;
    private Interview interview;

    @BeforeEach
    void setUp() {

        candidate = User.builder()
                .id(1L)
                .firstName("Amal")
                .lastName("Candidate")
                .email("amal.candidate@talentflow.com")
                .role(Role.CANDIDATE)
                .enabled(true)
                .build();

        job = Job.builder()
                .id(10L)
                .title("Développeur Java")
                .build();

        application = Application.builder()
                .id(100L)
                .candidate(candidate)
                .job(job)
                .status(ApplicationStatus.SHORTLISTED)
                .build();

        interview = Interview.builder()
                .id(200L)
                .application(application)
                .scheduledAt(
                        LocalDateTime.now().plusDays(2)
                )
                .duration(60)
                .type(InterviewType.ONLINE)
                .location("Google Meet")
                .notes("Entretien technique")
                .status(InterviewStatus.SCHEDULED)
                .build();
    }

    @Test
    void shouldCreateInterviewSuccessfully() {

        InterviewRequest request = InterviewRequest.builder()
                .applicationId(100L)
                .scheduledAt(
                        LocalDateTime.now().plusDays(2)
                )
                .duration(60)
                .type(InterviewType.ONLINE)
                .location("Google Meet")
                .notes("Entretien technique")
                .build();

        when(applicationRepository.findById(100L))
                .thenReturn(Optional.of(application));

        when(interviewRepository.existsByApplicationId(100L))
                .thenReturn(false);

        when(interviewRepository.save(any(Interview.class)))
                .thenReturn(interview);

        InterviewResponse response =
                interviewService.createInterview(request);

        assertNotNull(response);
        assertEquals(200L, response.getId());
        assertEquals(100L, response.getApplicationId());
        assertEquals(1L, response.getCandidateId());
        assertEquals(
                "Amal Candidate",
                response.getCandidateName()
        );
        assertEquals(
                "amal.candidate@talentflow.com",
                response.getCandidateEmail()
        );
        assertEquals(10L, response.getJobId());
        assertEquals(
                "Développeur Java",
                response.getJobTitle()
        );
        assertEquals(InterviewType.ONLINE, response.getType());
        assertEquals(60, response.getDuration());
        assertEquals(InterviewStatus.SCHEDULED, response.getStatus());

        assertEquals(
                ApplicationStatus.INTERVIEW,
                application.getStatus()
        );

        verify(applicationRepository)
                .save(application);

        verify(interviewRepository)
                .save(any(Interview.class));

        verify(notificationService).createNotification(
                candidate,
                NotificationType.INTERVIEW_SCHEDULED,
                "Un entretien a été planifié pour votre candidature "
                        + "au poste : Développeur Java"
        );
    }

    @Test
    void shouldThrowExceptionWhenApplicationDoesNotExist() {

        InterviewRequest request = InterviewRequest.builder()
                .applicationId(999L)
                .scheduledAt(
                        LocalDateTime.now().plusDays(2)
                )
                .duration(60)
                .type(InterviewType.ONLINE)
                .build();

        when(applicationRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> interviewService.createInterview(request)
        );

        verify(interviewRepository, never())
                .save(any(Interview.class));
    }

    @Test
    void shouldThrowExceptionWhenApplicationIsNotShortlistedOrInterview() {

        application.setStatus(ApplicationStatus.PENDING);

        InterviewRequest request = InterviewRequest.builder()
                .applicationId(100L)
                .scheduledAt(
                        LocalDateTime.now().plusDays(2)
                )
                .duration(60)
                .type(InterviewType.ONLINE)
                .build();

        when(applicationRepository.findById(100L))
                .thenReturn(Optional.of(application));

        assertThrows(
                IllegalArgumentException.class,
                () -> interviewService.createInterview(request)
        );

        verify(interviewRepository, never())
                .save(any(Interview.class));
    }

    @Test
    void shouldThrowExceptionWhenInterviewAlreadyExists() {

        InterviewRequest request = InterviewRequest.builder()
                .applicationId(100L)
                .scheduledAt(
                        LocalDateTime.now().plusDays(2)
                )
                .duration(60)
                .type(InterviewType.ONLINE)
                .build();

        when(applicationRepository.findById(100L))
                .thenReturn(Optional.of(application));

        when(interviewRepository.existsByApplicationId(100L))
                .thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> interviewService.createInterview(request)
        );

        verify(interviewRepository, never())
                .save(any(Interview.class));
    }

    @Test
    void shouldGetAllInterviews() {

        when(interviewRepository.findAllByOrderByScheduledAtAsc())
                .thenReturn(List.of(interview));

        List<InterviewResponse> responses =
                interviewService.getAllInterviews();

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(200L, responses.get(0).getId());
        assertEquals(
                "Amal Candidate",
                responses.get(0).getCandidateName()
        );
        assertEquals(
                "Développeur Java",
                responses.get(0).getJobTitle()
        );
        assertEquals(
                InterviewType.ONLINE,
                responses.get(0).getType()
        );
    }

    @Test
    void shouldGetInterviewsByApplication() {

        when(applicationRepository.existsById(100L))
                .thenReturn(true);

        when(interviewRepository.findByApplicationId(100L))
                .thenReturn(List.of(interview));

        List<InterviewResponse> responses =
                interviewService.getInterviewsByApplication(100L);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(200L, responses.get(0).getId());

        verify(interviewRepository)
                .findByApplicationId(100L);
    }

    @Test
    void shouldThrowExceptionWhenApplicationNotFoundForSearch() {

        when(applicationRepository.existsById(999L))
                .thenReturn(false);

        assertThrows(
                ResourceNotFoundException.class,
                () -> interviewService
                        .getInterviewsByApplication(999L)
        );

        verify(interviewRepository, never())
                .findByApplicationId(anyLong());
    }

    @Test
    void shouldGetMyInterviews() {

        try {
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(
                            "amal.candidate@talentflow.com",
                            null
                    )
            );

            when(userRepository.findByEmail(
                    "amal.candidate@talentflow.com"
            )).thenReturn(Optional.of(candidate));

            when(
                    interviewRepository
                            .findByApplicationCandidateId(1L)
            ).thenReturn(List.of(interview));

            List<InterviewResponse> responses =
                    interviewService.getMyInterviews();

            assertNotNull(responses);
            assertEquals(1, responses.size());
            assertEquals(200L, responses.get(0).getId());
            assertEquals(
                    InterviewType.ONLINE,
                    responses.get(0).getType()
            );

            verify(interviewRepository)
                    .findByApplicationCandidateId(1L);

        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void shouldGetInterviewsByCandidate() {

        when(userRepository.existsById(1L))
                .thenReturn(true);

        when(
                interviewRepository
                        .findByApplicationCandidateId(1L)
        ).thenReturn(List.of(interview));

        List<InterviewResponse> responses =
                interviewService.getInterviewsByCandidate(1L);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(200L, responses.get(0).getId());
        assertEquals(
                "Amal Candidate",
                responses.get(0).getCandidateName()
        );
    }

    @Test
    void shouldUpdateInterviewStatusToCompleted() {

        when(interviewRepository.findById(200L))
                .thenReturn(Optional.of(interview));

        when(interviewRepository.save(interview))
                .thenReturn(interview);

        InterviewResponse response =
                interviewService.updateStatus(
                        200L,
                        InterviewStatus.COMPLETED
                );

        assertNotNull(response);
        assertEquals(
                InterviewStatus.COMPLETED,
                interview.getStatus()
        );
        assertEquals(
                InterviewStatus.COMPLETED,
                response.getStatus()
        );

        verify(interviewRepository)
                .save(interview);

        verify(notificationService).createNotification(
                candidate,
                NotificationType.INTERVIEW_STATUS_CHANGED,
                "Le statut de votre entretien pour le poste \""
                        + "Développeur Java"
                        + "\" a été changé vers : "
                        + InterviewStatus.COMPLETED
        );
    }

    @Test
    void shouldUpdateInterviewStatusToCancelled() {

        when(interviewRepository.findById(200L))
                .thenReturn(Optional.of(interview));

        when(interviewRepository.save(interview))
                .thenReturn(interview);

        InterviewResponse response =
                interviewService.updateStatus(
                        200L,
                        InterviewStatus.CANCELLED
                );

        assertNotNull(response);
        assertEquals(
                InterviewStatus.CANCELLED,
                response.getStatus()
        );
    }

    @Test
    void shouldRejectInvalidStatusTransition() {

        when(interviewRepository.findById(200L))
                .thenReturn(Optional.of(interview));

        assertThrows(
                IllegalArgumentException.class,
                () -> interviewService.updateStatus(
                        200L,
                        InterviewStatus.SCHEDULED
                )
        );

        verify(interviewRepository, never())
                .save(any(Interview.class));
    }

    @Test
    void shouldRejectStatusChangeFromCompletedInterview() {

        interview.setStatus(InterviewStatus.COMPLETED);

        when(interviewRepository.findById(200L))
                .thenReturn(Optional.of(interview));

        assertThrows(
                IllegalArgumentException.class,
                () -> interviewService.updateStatus(
                        200L,
                        InterviewStatus.CANCELLED
                )
        );

        verify(interviewRepository, never())
                .save(any(Interview.class));
    }

    @Test
    void shouldDeleteInterviewAndReturnApplicationToShortlisted() {

        application.setStatus(ApplicationStatus.INTERVIEW);

        when(interviewRepository.findById(200L))
                .thenReturn(Optional.of(interview));

        interviewService.deleteInterview(200L);

        verify(interviewRepository)
                .delete(interview);

        assertEquals(
                ApplicationStatus.SHORTLISTED,
                application.getStatus()
        );

        verify(applicationRepository)
                .save(application);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistingInterview() {

        when(interviewRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> interviewService.deleteInterview(999L)
        );

        verify(interviewRepository, never())
                .delete(any(Interview.class));
    }
}
