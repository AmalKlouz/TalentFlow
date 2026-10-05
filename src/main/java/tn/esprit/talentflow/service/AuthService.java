package tn.esprit.talentflow.service;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import tn.esprit.talentflow.dto.AuthResponse;
import tn.esprit.talentflow.dto.ForgotPasswordRequest;
import tn.esprit.talentflow.dto.LoginRequest;
import tn.esprit.talentflow.dto.RegisterRequest;
import tn.esprit.talentflow.dto.ResetPasswordRequest;
import tn.esprit.talentflow.dto.UserResponse;

import tn.esprit.talentflow.entity.Role;
import tn.esprit.talentflow.entity.User;

import tn.esprit.talentflow.exception.ResourceNotFoundException;

import tn.esprit.talentflow.repository.UserRepository;
import tn.esprit.talentflow.security.JwtService;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final EmailService emailService;

    private final SecureRandom secureRandom = new SecureRandom();


    // =========================================================
    // REGISTER CANDIDATE
    // =========================================================

    public UserResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {

            throw new IllegalArgumentException(
                    "Cet email est déjà utilisé"
            );
        }

        User user = User.builder()

                .firstName(
                        request.getFirstName()
                )

                .lastName(
                        request.getLastName()
                )

                .email(
                        request.getEmail()
                )

                .password(
                        passwordEncoder.encode(
                                request.getPassword()
                        )
                )

                .role(
                        Role.CANDIDATE
                )

                .enabled(true)

                .skills(
                        request.getSkills()
                )

                .build();

        User savedUser =
                userRepository.save(user);

        return toUserResponse(savedUser);
    }


    // =========================================================
    // LOGIN
    // =========================================================

    public AuthResponse login(LoginRequest request) {

        User user =
                userRepository
                        .findByEmail(request.getEmail())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Email ou mot de passe incorrect"
                                )
                        );

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            throw new IllegalArgumentException(
                    "Email ou mot de passe incorrect"
            );
        }

        String token =
                JwtService.generateToken(
                        user.getEmail()
                );

        return AuthResponse.builder()

                .token(token)

                .userId(user.getId())

                .firstName(user.getFirstName())

                .lastName(user.getLastName())

                .email(user.getEmail())

                .role(user.getRole())

                .build();
    }


    // =========================================================
    // CREATE RH / RECRUITER
    // =========================================================

    public UserResponse createUser(
            RegisterRequest request,
            Role role) {

        if (userRepository.existsByEmail(
                request.getEmail())) {

            throw new IllegalArgumentException(
                    "Cet email est déjà utilisé"
            );
        }

        User user = User.builder()

                .firstName(
                        request.getFirstName()
                )

                .lastName(
                        request.getLastName()
                )

                .email(
                        request.getEmail()
                )

                .password(
                        passwordEncoder.encode(
                                request.getPassword()
                        )
                )

                .role(role)

                .enabled(true)

                .skills(
                        request.getSkills()
                )

                .build();

        User savedUser =
                userRepository.save(user);

        return toUserResponse(savedUser);
    }


    // =========================================================
    // FORGOT PASSWORD
    // =========================================================

    public void forgotPassword(
            ForgotPasswordRequest request) {

        User user =
                userRepository
                        .findByEmail(request.getEmail())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Aucun compte associé à cet email"
                                )
                        );

        String code =
                String.format(
                        "%06d",
                        secureRandom.nextInt(1_000_000)
                );

        user.setResetCode(code);

        user.setResetCodeExpiration(
                LocalDateTime.now().plusMinutes(10)
        );

        userRepository.save(user);

        emailService.sendResetCode(
                user.getEmail(),
                code
        );
    }


    // =========================================================
    // RESET PASSWORD
    // =========================================================

    public void resetPassword(
            ResetPasswordRequest request) {

        User user =
                userRepository
                        .findByEmail(request.getEmail())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Utilisateur introuvable"
                                )
                        );

        if (user.getResetCode() == null
                || !user.getResetCode()
                .equals(request.getCode())) {

            throw new IllegalArgumentException(
                    "Code de réinitialisation invalide"
            );
        }

        if (user.getResetCodeExpiration() == null
                || user.getResetCodeExpiration()
                .isBefore(LocalDateTime.now())) {

            throw new IllegalArgumentException(
                    "Le code de réinitialisation a expiré"
            );
        }

        user.setPassword(
                passwordEncoder.encode(
                        request.getNewPassword()
                )
        );

        user.setResetCode(null);

        user.setResetCodeExpiration(null);

        userRepository.save(user);
    }


    // =========================================================
    // ENTITY → DTO
    // =========================================================

    public UserResponse toUserResponse(
            User user) {

        return UserResponse.builder()

                .id(user.getId())

                .firstName(
                        user.getFirstName()
                )

                .lastName(
                        user.getLastName()
                )

                .email(
                        user.getEmail()
                )

                .role(
                        user.getRole()
                )

                .enabled(
                        user.isEnabled()
                )
                .skills(user.getSkills())


                .build();
    }
}
