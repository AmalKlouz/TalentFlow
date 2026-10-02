package tn.esprit.talentflow.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/protected")
    public String protectedEndpoint() {
        return "JWT valide - accès autorisé";
    }

    @GetMapping("/candidate")
    @PreAuthorize("hasRole('CANDIDATE')")
    public String candidateEndpoint() {
        return "Bienvenue dans l'espace CANDIDATE";
    }

    @GetMapping("/rh")
    @PreAuthorize("hasRole('RH')")
    public String rhEndpoint() {
        return "Bienvenue dans l'espace RH";
    }

    @GetMapping("/recruiter")
    @PreAuthorize("hasRole('RECRUITER')")
    public String recruiterEndpoint() {
        return "Bienvenue dans l'espace RECRUITER";
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminEndpoint() {
        return "Bienvenue dans l'espace ADMIN";
    }
}
