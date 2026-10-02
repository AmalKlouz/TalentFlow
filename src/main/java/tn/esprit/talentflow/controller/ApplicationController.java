package tn.esprit.talentflow.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import tn.esprit.talentflow.dto.ApplicationRequest;
import tn.esprit.talentflow.dto.ApplicationResponse;
import tn.esprit.talentflow.entity.ApplicationStatus;
import tn.esprit.talentflow.service.ApplicationService;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    // =========================================================
    // CANDIDATE : POSTULER À UNE OFFRE
    // =========================================================

    @PostMapping(consumes = "multipart/form-data")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ApplicationResponse> apply(
            @Valid @ModelAttribute ApplicationRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(applicationService.apply(request));
    }

    // =========================================================
    // CANDIDATE : VOIR SES CANDIDATURES
    // =========================================================

    @GetMapping("/my")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<List<ApplicationResponse>> getMyApplications() {

        return ResponseEntity.ok(
                applicationService.getMyApplications()
        );
    }

    // =========================================================
    // RH / RECRUITER / ADMIN :
    // VOIR LES CANDIDATURES D'UNE OFFRE
    // =========================================================

    @GetMapping("/job/{jobId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RH', 'RECRUITER')")
    public ResponseEntity<List<ApplicationResponse>> getApplicationsByJob(
            @PathVariable Long jobId) {

        return ResponseEntity.ok(
                applicationService.getApplicationsByJob(jobId)
        );
    }

    // =========================================================
    // CONSULTER UNE CANDIDATURE
    // =========================================================

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RH', 'RECRUITER', 'CANDIDATE')")
    public ResponseEntity<ApplicationResponse> getApplicationById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                applicationService.getApplicationById(id)
        );
    }

    // =========================================================
    // RH / RECRUITER / ADMIN :
    // MODIFIER LE STATUT
    // =========================================================

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'RH', 'RECRUITER')")
    public ResponseEntity<ApplicationResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam ApplicationStatus status) {

        return ResponseEntity.ok(
                applicationService.updateStatus(id, status)
        );
    }
}