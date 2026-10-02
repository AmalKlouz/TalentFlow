
package tn.esprit.talentflow.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import tn.esprit.talentflow.dto.ApplicationRequest;
import tn.esprit.talentflow.entity.*;
import tn.esprit.talentflow.repository.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ApplicationService applicationService;

    @AfterEach
    void cleanSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private void mockCurrentUser(String email, User user) {
        when(securityContext.getAuthentication())
                .thenReturn(authentication);
        when(authentication.getName()).thenReturn(email);

        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));
    }

    @Test
    void shouldGetMyApplications() {
        User candidate = User.builder()
                .id(1L)
                .firstName("Amine")
                .lastName("Khelifi")
                .email("amine@test.com")
                .role(Role.CANDIDATE)
                .skills("")
                .build();

        Job job = Job.builder()
                .id(1L)
                .title("Développeur Java")
                .build();

        Application application = Application.builder()
                .id(1L)
                .candidate(candidate)
                .job(job)
                .status(ApplicationStatus.PENDING)
                .build();

        mockCurrentUser("amine@test.com", candidate);

        when(applicationRepository.findByCandidateId(1L))
                .thenReturn(List.of(application));

        var result = applicationService.getMyApplications();

        assertEquals(1, result.size());
        assertEquals("Développeur Java", result.get(0).getJobTitle());
        assertEquals(ApplicationStatus.PENDING, result.get(0).getStatus());
    }

    @Test
    void shouldRejectUnauthorizedCandidateAccess() {
        User owner = User.builder()
                .id(1L)
                .firstName("Amine")
                .lastName("Khelifi")
                .email("amine@test.com")
                .role(Role.CANDIDATE)
                .skills("")
                .build();

        User otherCandidate = User.builder()
                .id(2L)
                .firstName("Nour")
                .lastName("Mansouri")
                .email("nour@test.com")
                .role(Role.CANDIDATE)
                .skills("")
                .build();

        Job job = Job.builder()
                .id(1L)
                .title("Développeur Java")
                .build();

        Application application = Application.builder()
                .id(1L)
                .candidate(owner)
                .job(job)
                .status(ApplicationStatus.PENDING)
                .build();

        mockCurrentUser("nour@test.com", otherCandidate);

        when(applicationRepository.findById(1L))
                .thenReturn(Optional.of(application));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> applicationService.getApplicationById(1L)
        );

        assertEquals(
                "Vous n'êtes pas autorisé à consulter cette candidature",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectApplicationWhenCandidateHasWrongRole() {
        User rh = User.builder()
                .id(1L)
                .firstName("Sara")
                .lastName("RH")
                .email("sara@test.com")
                .role(Role.RH)
                .skills("")
                .build();

        mockCurrentUser("sara@test.com", rh);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> applicationService.apply(
                        ApplicationRequest.builder()
                                .jobId(1L)
                                .build()
                )
        );

        assertEquals(
                "Seuls les candidats peuvent postuler",
                exception.getMessage()
        );

        verifyNoInteractions(jobRepository);
    }

    @Test
    void shouldRejectApplicationForUnpublishedJob() {
        User candidate = User.builder()
                .id(1L)
                .firstName("Amine")
                .lastName("Khelifi")
                .email("amine@test.com")
                .role(Role.CANDIDATE)
                .skills("")
                .build();

        Job draftJob = Job.builder()
                .id(1L)
                .title("Développeur Java")
                .status(JobStatus.DRAFT)
                .build();

        mockCurrentUser("amine@test.com", candidate);

        when(jobRepository.findById(1L))
                .thenReturn(Optional.of(draftJob));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> applicationService.apply(
                        ApplicationRequest.builder()
                                .jobId(1L)
                                .build()
                )
        );

        assertEquals(
                "Cette offre n'est pas publiée",
                exception.getMessage()
        );

        verifyNoInteractions(fileStorageService);
        verify(applicationRepository, never())
                .save(any(Application.class));
    }

    @Test
    void shouldUpdateApplicationStatusToReviewing() {
        User candidate = User.builder()
                .id(1L)
                .firstName("Amine")
                .lastName("Khelifi")
                .email("amine@test.com")
                .role(Role.CANDIDATE)
                .skills("")
                .build();

        Job job = Job.builder()
                .id(1L)
                .title("Développeur Java")
                .build();

        Application application = Application.builder()
                .id(1L)
                .candidate(candidate)
                .job(job)
                .status(ApplicationStatus.PENDING)
                .build();

        when(applicationRepository.findById(1L))
                .thenReturn(Optional.of(application));

        when(applicationRepository.save(any(Application.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var result = applicationService.updateStatus(
                1L,
                ApplicationStatus.REVIEWING
        );

        assertEquals(ApplicationStatus.REVIEWING, result.getStatus());

        verify(notificationService).createNotification(
                eq(candidate),
                eq(NotificationType.APPLICATION_STATUS_CHANGED),
                contains("REVIEWING")
        );
    }

    @Test
    void shouldRejectInvalidStatusTransition() {
        User candidate = User.builder()
                .id(1L)
                .firstName("Amine")
                .lastName("Khelifi")
                .email("amine@test.com")
                .role(Role.CANDIDATE)
                .skills("")
                .build();

        Job job = Job.builder()
                .id(1L)
                .title("Développeur Java")
                .build();

        Application application = Application.builder()
                .id(1L)
                .candidate(candidate)
                .job(job)
                .status(ApplicationStatus.PENDING)
                .build();

        when(applicationRepository.findById(1L))
                .thenReturn(Optional.of(application));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> applicationService.updateStatus(
                        1L,
                        ApplicationStatus.ACCEPTED
                )
        );

        assertTrue(
                exception.getMessage().contains("Transition impossible")
        );

        verify(applicationRepository, never())
                .save(any(Application.class));

        verifyNoInteractions(notificationService);
    }
}
