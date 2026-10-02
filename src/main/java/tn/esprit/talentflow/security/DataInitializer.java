package tn.esprit.talentflow.security;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import tn.esprit.talentflow.entity.Role;
import tn.esprit.talentflow.entity.User;
import tn.esprit.talentflow.repository.UserRepository;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner initAdmin() {

        return args -> {

            if (!userRepository.existsByEmail(
                    "admin@talentflow.com")) {

                User admin = User.builder()
                        .firstName("Admin")
                        .lastName("TalentFlow")
                        .email("admin@talentflow.com")
                        .password(
                                passwordEncoder.encode("Admin123")
                        )
                        .role(Role.ADMIN)
                        .enabled(true)
                        .skills("")
                        .build();

                userRepository.save(admin);

                System.out.println(
                        "================================="
                );
                System.out.println(
                        "ADMIN TALENTFLOW CREE"
                );
                System.out.println(
                        "Email : admin@talentflow.com"
                );
                System.out.println(
                        "Mot de passe : Admin123"
                );
                System.out.println(
                        "================================="
                );
            }
        };
    }
}
