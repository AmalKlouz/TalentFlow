        package tn.esprit.talentflow.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import tn.esprit.talentflow.dto.MatchingResponse;
import tn.esprit.talentflow.service.MatchingService;

@RestController
@RequestMapping("/api/matching")
@RequiredArgsConstructor
public class MatchingController {

    private final MatchingService matchingService;

    @GetMapping("/candidate/{candidateId}/job/{jobId}")
    @PreAuthorize("hasAnyRole('RH', 'RECRUITER', 'ADMIN')")
    public ResponseEntity<MatchingResponse> calculateMatching(
            @PathVariable Long candidateId,
            @PathVariable Long jobId) {

        return ResponseEntity.ok(
                matchingService.calculateMatching(
                        candidateId,
                        jobId
                )
        );
    }
}
