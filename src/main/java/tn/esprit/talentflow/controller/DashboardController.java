package tn.esprit.talentflow.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tn.esprit.talentflow.dto.DashboardResponse;
import tn.esprit.talentflow.service.DashboardService;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    @PreAuthorize("hasAnyRole('RH', 'RECRUITER', 'ADMIN')")
    public ResponseEntity<DashboardResponse> getStatistics() {

        return ResponseEntity.ok(
                dashboardService.getStatistics()
        );
    }
}