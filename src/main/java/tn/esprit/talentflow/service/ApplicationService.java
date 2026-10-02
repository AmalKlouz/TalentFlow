
package tn.esprit.talentflow.service;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import tn.esprit.talentflow.dto.ApplicationRequest;
import tn.esprit.talentflow.dto.ApplicationResponse;
import tn.esprit.talentflow.entity.Application;
import tn.esprit.talentflow.entity.ApplicationStatus;
import tn.esprit.talentflow.entity.Job;
import tn.esprit.talentflow.entity.JobStatus;
import tn.esprit.talentflow.entity.NotificationType;
import tn.esprit.talentflow.entity.Role;
import tn.esprit.talentflow.entity.User;
import tn.esprit.talentflow.repository.ApplicationRepository;
import tn.esprit.talentflow.repository.JobRepository;
import tn.esprit.talentflow.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final FileStorageService fileStorageService;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final NotificationService notificationService;


    // =========================================================
    // CREATE APPLICATION
    // =========================================================

    public ApplicationResponse apply(ApplicationRequest request) {

        User candidate = getCurrentUser();

        if (candidate.getRole() != Role.CANDIDATE) {

            throw new RuntimeException(
                    "Seuls les candidats peuvent postuler"
            );
        }

        Job job = jobRepository.findById(request.getJobId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Offre introuvable"
                        )
                );

        if (job.getStatus() != JobStatus.PUBLISHED) {

            throw new RuntimeException(
                    "Cette offre n'est pas publiée"
            );
        }

        if (applicationRepository.existsByCandidateIdAndJobId(
                candidate.getId(),
                job.getId())) {

            throw new RuntimeException(
                    "Vous avez déjà postulé à cette offre"
            );
        }

        Application application = Application.builder()
                .candidate(candidate)
                .job(job)
                .status(ApplicationStatus.PENDING)
                .appliedAt(LocalDateTime.now())
                .coverLetter(request.getCoverLetter())
                .cvPath(
                        fileStorageService.saveCv(
                                request.getCv()
                        )
                )
                .build();

        Application savedApplication =
                applicationRepository.save(application);


        // =====================================================
        // NOTIFICATION RH
        // =====================================================

        List<User> rhUsers =
                userRepository.findByRole(Role.RH);

        for (User rh : rhUsers) {

            notificationService.createNotification(
                    rh,
                    NotificationType.APPLICATION_SUBMITTED,
                    "Nouvelle candidature reçue pour le poste : "
                            + job.getTitle()
            );
        }


        return toResponse(savedApplication);
    }


    // =========================================================
    // GET MY APPLICATIONS
    // =========================================================

    public List<ApplicationResponse> getMyApplications() {

        User candidate = getCurrentUser();

        if (candidate.getRole() != Role.CANDIDATE) {

            throw new RuntimeException(
                    "Seuls les candidats peuvent consulter "
                            + "leurs candidatures"
            );
        }

        return applicationRepository
                .findByCandidateId(candidate.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }


    // =========================================================
    // GET ALL APPLICATIONS
    // =========================================================

    public List<ApplicationResponse> getAllApplications() {

        return applicationRepository
                .findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }


    // =========================================================
    // GET APPLICATION BY ID
    // =========================================================

    public ApplicationResponse getApplicationById(Long id) {

        Application application =
                applicationRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Candidature introuvable"
                                )
                        );

        User currentUser = getCurrentUser();


        // RH / RECRUITER / ADMIN
        // peuvent consulter toutes les candidatures

        if (currentUser.getRole() == Role.RH
                || currentUser.getRole() == Role.RECRUITER
                || currentUser.getRole() == Role.ADMIN) {

            return toResponse(application);
        }


        // CANDIDATE
        // peut uniquement consulter sa propre candidature

        if (currentUser.getRole() == Role.CANDIDATE
                && application.getCandidate()
                .getId()
                .equals(currentUser.getId())) {

            return toResponse(application);
        }


        throw new RuntimeException(
                "Vous n'êtes pas autorisé à consulter "
                        + "cette candidature"
        );
    }


    // =========================================================
    // GET APPLICATIONS FOR A JOB
    // =========================================================

    public List<ApplicationResponse> getApplicationsByJob(
            Long jobId) {

        if (!jobRepository.existsById(jobId)) {

            throw new RuntimeException(
                    "Offre introuvable"
            );
        }

        return applicationRepository
                .findByJobId(jobId)
                .stream()
                .map(this::toResponse)
                .toList();
    }


    // =========================================================
    // UPDATE STATUS
    // =========================================================

    public ApplicationResponse updateStatus(
            Long id,
            ApplicationStatus newStatus) {

        Application application =
                applicationRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Candidature introuvable"
                                )
                        );

        ApplicationStatus currentStatus =
                application.getStatus();


        // =====================================================
        // VALID STATUS TRANSITIONS
        // =====================================================

        boolean validTransition = switch (currentStatus) {

            case PENDING ->
                    newStatus == ApplicationStatus.REVIEWING
                            || newStatus == ApplicationStatus.REJECTED;

            case REVIEWING ->
                    newStatus == ApplicationStatus.SHORTLISTED
                            || newStatus == ApplicationStatus.REJECTED;

            case SHORTLISTED ->
                    newStatus == ApplicationStatus.INTERVIEW
                            || newStatus == ApplicationStatus.REJECTED;

            case INTERVIEW ->
                    newStatus == ApplicationStatus.ACCEPTED
                            || newStatus == ApplicationStatus.REJECTED;

            case ACCEPTED, REJECTED -> false;
        };


        if (!validTransition) {

            throw new RuntimeException(
                    "Transition impossible de "
                            + currentStatus
                            + " vers "
                            + newStatus
            );
        }


        application.setStatus(newStatus);

        Application updatedApplication =
                applicationRepository.save(application);


        // =====================================================
        // NOTIFICATION CANDIDATE
        // =====================================================

        notificationService.createNotification(
                application.getCandidate(),
                NotificationType.APPLICATION_STATUS_CHANGED,
                "Le statut de votre candidature pour le poste \""
                        + application.getJob().getTitle()
                        + "\" a été changé vers : "
                        + newStatus
        );


        return toResponse(updatedApplication);
    }


    // =========================================================
    // GET CV
    // =========================================================

    public byte[] getCv(Long applicationId) {

        Application application =
                applicationRepository.findById(applicationId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Candidature introuvable"
                                )
                        );

        User currentUser = getCurrentUser();


        // RH / RECRUITER / ADMIN

        if (currentUser.getRole() == Role.RH
                || currentUser.getRole() == Role.RECRUITER
                || currentUser.getRole() == Role.ADMIN) {

            return readCv(application);
        }


        // CANDIDATE
        // uniquement son propre CV

        if (currentUser.getRole() == Role.CANDIDATE
                && application.getCandidate()
                .getId()
                .equals(currentUser.getId())) {

            return readCv(application);
        }


        throw new RuntimeException(
                "Vous n'êtes pas autorisé à consulter ce CV"
        );
    }


    // =========================================================
    // READ CV
    // =========================================================

    private byte[] readCv(Application application) {

        if (application.getCvPath() == null) {

            throw new RuntimeException(
                    "Aucun CV associé à cette candidature"
            );
        }

        return fileStorageService.getCv(
                application.getCvPath()
        );
    }


    // =========================================================
    // CURRENT USER
    // =========================================================

    private User getCurrentUser() {

        String email =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName();

        return userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Utilisateur connecté introuvable"
                        )
                );
    }


    // =========================================================
    // ENTITY → DTO
    // =========================================================

    private ApplicationResponse toResponse(
            Application application) {

        User candidate =
                application.getCandidate();

        Job job =
                application.getJob();

        return ApplicationResponse.builder()

                .id(application.getId())

                .candidateId(candidate.getId())

                .candidateName(
                        candidate.getFirstName()
                                + " "
                                + candidate.getLastName()
                )

                .candidateEmail(
                        candidate.getEmail()
                )

                .jobId(job.getId())

                .jobTitle(job.getTitle())

                .status(application.getStatus())

                .appliedAt(application.getAppliedAt())

                .coverLetter(application.getCoverLetter())

                .cvPath(application.getCvPath())

                .build();
    }
}