package tn.esprit.talentflow.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import tn.esprit.talentflow.dto.AuthResponse;
import tn.esprit.talentflow.dto.LoginRequest;
import tn.esprit.talentflow.dto.RegisterRequest;
import tn.esprit.talentflow.dto.UserResponse;
import tn.esprit.talentflow.entity.Role;
import tn.esprit.talentflow.service.AuthService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        UserResponse user = authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(user);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {

        AuthResponse response = authService.login(request);

        return ResponseEntity.ok(response);
    }
    @PostMapping("/admin/create-rh")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> createRh(
            @Valid @RequestBody RegisterRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.createUser(request, Role.RH));
    }
    @PostMapping("/admin/create-recruiter")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> createRecruiter(
            @Valid @RequestBody RegisterRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.createUser(request, Role.RECRUITER));
    }
}
