package tn.esprit.talentflow.controller;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import tn.esprit.talentflow.dto.JobRequest;
import tn.esprit.talentflow.dto.JobResponse;
import tn.esprit.talentflow.service.JobService;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    // =========================================================
    // CREATE
    // ADMIN / RH / RECRUITER
    // =========================================================

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RH', 'RECRUITER')")
    public ResponseEntity<JobResponse> createJob(
            @Valid @RequestBody JobRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(jobService.createJob(request));
    }

    // =========================================================
    // GET ALL
    // ADMIN / RH / RECRUITER
    // =========================================================

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RH', 'RECRUITER')")
    public ResponseEntity<List<JobResponse>> getAllJobs() {

        return ResponseEntity.ok(
                jobService.getAllJobs()
        );
    }

    // =========================================================
    // GET PUBLISHED
    // ALL AUTHENTICATED USERS
    // =========================================================

    @GetMapping("/published")
    public ResponseEntity<List<JobResponse>> getPublishedJobs() {

        return ResponseEntity.ok(
                jobService.getPublishedJobs()
        );
    }

    // =========================================================
    // GET BY ID
    // ALL AUTHENTICATED USERS
    // =========================================================

    @GetMapping("/{id}")
    public ResponseEntity<JobResponse> getJobById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                jobService.getJobById(id)
        );
    }

    // =========================================================
    // UPDATE
    // ADMIN / RH
    // =========================================================

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RH')")
    public ResponseEntity<JobResponse> updateJob(
            @PathVariable Long id,
            @Valid @RequestBody JobRequest request) {

        return ResponseEntity.ok(
                jobService.updateJob(id, request)
        );
    }

    // =========================================================
    // PUBLISH
    // ADMIN / RH
    // =========================================================

    @PatchMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('ADMIN', 'RH')")
    public ResponseEntity<JobResponse> publishJob(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                jobService.publishJob(id)
        );
    }

    // =========================================================
    // CLOSE
    // ADMIN / RH
    // =========================================================

    @PatchMapping("/{id}/close")
    @PreAuthorize("hasAnyRole('ADMIN', 'RH')")
    public ResponseEntity<JobResponse> closeJob(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                jobService.closeJob(id)
        );
    }

    // =========================================================
    // DELETE
    // ADMIN
    // =========================================================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteJob(
            @PathVariable Long id) {

        jobService.deleteJob(id);

        return ResponseEntity.noContent().build();
    }
}
