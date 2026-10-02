package tn.esprit.talentflow.service;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import tn.esprit.talentflow.dto.JobRequest;
import tn.esprit.talentflow.dto.JobResponse;
import tn.esprit.talentflow.entity.Job;
import tn.esprit.talentflow.entity.JobStatus;
import tn.esprit.talentflow.entity.User;
import tn.esprit.talentflow.repository.JobRepository;
import tn.esprit.talentflow.repository.UserRepository;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final UserRepository userRepository;

    // =========================================================
    // CREATE
    // ========================================================
    public JobResponse createJob(JobRequest request) {

        User currentUser = getCurrentUser();

        Job job = Job.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .location(request.getLocation())
                .contractType(request.getContractType())
                .experienceLevel(request.getExperienceLevel())
                .requiredSkills(request.getRequiredSkills())
                .publicationDate(LocalDate.now())
                .deadline(request.getDeadline())
                .status(JobStatus.DRAFT)
                .createdBy(currentUser)
                .build();

        Job savedJob = jobRepository.save(job);

        return toResponse(savedJob);
    }

    // =========================================================
    // GET ALL
    // =========================================================

    public List<JobResponse> getAllJobs() {

        return jobRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // =========================================================
    // GET BY ID
    // =========================================================

    public JobResponse getJobById(Long id) {

        Job job = jobRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Offre introuvable"));

        return toResponse(job);
    }

    // =========================================================
    // GET PUBLISHED JOBS
    // =========================================================
    public List<JobResponse> getPublishedJobs() {

        return jobRepository.findByStatus(JobStatus.PUBLISHED)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // =========================================================
    // UPDATE
    // =========================================================

    public JobResponse updateJob(
            Long id,
            JobRequest request) {

        Job job = jobRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Offre introuvable"));

        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setLocation(request.getLocation());
        job.setContractType(request.getContractType());
        job.setExperienceLevel(request.getExperienceLevel());
        job.setRequiredSkills(request.getRequiredSkills());
        job.setDeadline(request.getDeadline());

        Job updatedJob = jobRepository.save(job);

        return toResponse(updatedJob);
    }

    // =========================================================
    // PUBLISH
    // =========================================================

    public JobResponse publishJob(Long id) {

        Job job = jobRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Offre introuvable"));

        job.setStatus(JobStatus.PUBLISHED);

        if (job.getPublicationDate() == null) {
            job.setPublicationDate(LocalDate.now());
        }

        Job publishedJob = jobRepository.save(job);

        return toResponse(publishedJob);
    }

    // =========================================================
    // CLOSE
    // =========================================================

    public JobResponse closeJob(Long id) {

        Job job = jobRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Offre introuvable"));

        job.setStatus(JobStatus.CLOSED);

        Job closedJob = jobRepository.save(job);

        return toResponse(closedJob);
    }

    // =========================================================
    // DELETE
    // =========================================================

    public void deleteJob(Long id) {

        if (!jobRepository.existsById(id)) {
            throw new RuntimeException("Offre introuvable");
        }

        jobRepository.deleteById(id);
    }

    // =========================================================
    // CURRENT USER
    // =========================================================

    private User getCurrentUser() {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Utilisateur connecté introuvable"));
    }

    // =========================================================
    // ENTITY → DTO
    // =========================================================

    private JobResponse toResponse(Job job) {

        User creator = job.getCreatedBy();

        return JobResponse.builder()
                .id(job.getId())
                .title(job.getTitle())
                .description(job.getDescription())
                .location(job.getLocation())
                .contractType(job.getContractType())
                .experienceLevel(job.getExperienceLevel())
                .requiredSkills(job.getRequiredSkills())
                .publicationDate(job.getPublicationDate())
                .deadline(job.getDeadline())
                .status(job.getStatus())
                .createdById(
                        creator != null ? creator.getId() : null
                )
                .createdByName(
                        creator != null
                                ? creator.getFirstName()
                                + " "
                                + creator.getLastName()
                                : null
                )
                .build();
    }
}
