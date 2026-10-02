package tn.esprit.talentflow.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import tn.esprit.talentflow.dto.InterviewRequest;
import tn.esprit.talentflow.dto.InterviewResponse;
import tn.esprit.talentflow.entity.InterviewStatus;
import tn.esprit.talentflow.service.InterviewService;

import java.util.List;

@RestController
@RequestMapping("/api/interviews")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;


    // ==============================
    // RH / RECRUITER / ADMIN
    // Créer un entretien
    // ==============================

    @PostMapping
    @PreAuthorize("hasAnyRole('RH', 'RECRUITER', 'ADMIN')")
    public ResponseEntity<InterviewResponse> createInterview(
            @Valid @RequestBody InterviewRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        interviewService.createInterview(request)
                );
    }


    // ==============================
    // RH / RECRUITER / ADMIN
    // Voir tous les entretiens
    // ==============================

    @GetMapping
    @PreAuthorize("hasAnyRole('RH', 'RECRUITER', 'ADMIN')")
    public ResponseEntity<List<InterviewResponse>>
    getAllInterviews() {

        return ResponseEntity.ok(
                interviewService.getAllInterviews()
        );
    }


    // ==============================
    // RH / RECRUITER / ADMIN
    // Voir les entretiens d'une candidature
    // ==============================

    @GetMapping("/application/{applicationId}")
    @PreAuthorize("hasAnyRole('RH', 'RECRUITER', 'ADMIN')")
    public ResponseEntity<List<InterviewResponse>>
    getInterviewsByApplication(
            @PathVariable Long applicationId) {

        return ResponseEntity.ok(
                interviewService.getInterviewsByApplication(
                        applicationId
                )
        );
    }


    // ==============================
    // CANDIDATE
    // Voir ses propres entretiens
    // ==============================

    @GetMapping("/my")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<List<InterviewResponse>>
    getMyInterviews() {

        return ResponseEntity.ok(
                interviewService.getMyInterviews()
        );
    }


    // ==============================
    // RH / RECRUITER / ADMIN
    // Voir les entretiens d'un candidat
    // ==============================

    @GetMapping("/candidate/{candidateId}")
    @PreAuthorize("hasAnyRole('RH', 'RECRUITER', 'ADMIN')")
    public ResponseEntity<List<InterviewResponse>>
    getInterviewsByCandidate(
            @PathVariable Long candidateId) {

        return ResponseEntity.ok(
                interviewService.getInterviewsByCandidate(
                        candidateId
                )
        );
    }


    // ==============================
    // RH / RECRUITER / ADMIN
    // Modifier le statut
    // ==============================

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('RH', 'RECRUITER', 'ADMIN')")
    public ResponseEntity<InterviewResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam InterviewStatus status) {

        return ResponseEntity.ok(
                interviewService.updateStatus(
                        id,
                        status
                )
        );
    }


    // ==============================
    // RH / RECRUITER / ADMIN
    // Supprimer
    // ==============================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('RH', 'RECRUITER', 'ADMIN')")
    public ResponseEntity<Void> deleteInterview(
            @PathVariable Long id) {

        interviewService.deleteInterview(id);

        return ResponseEntity.noContent().build();
    }
}