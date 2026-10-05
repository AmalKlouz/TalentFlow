package tn.esprit.talentflow.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import tn.esprit.talentflow.dto.*;
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
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        authService.forgotPassword(request);

        return ResponseEntity.ok(
                "Un code de réinitialisation a été envoyé"
        );
    }


    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        authService.resetPassword(request);

        return ResponseEntity.ok(
                "Mot de passe réinitialisé avec succès"
        );
    }


}
