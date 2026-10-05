package tn.esprit.talentflow.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private boolean enabled = true;

    @ElementCollection @CollectionTable( name = "user_skills",
            joinColumns = @JoinColumn(name = "user_id") )
    @Column(name = "skill") @Builder.Default
    private List<String> skills = new ArrayList<>();

    @Column
    private String resetCode;

    @Column
    private LocalDateTime resetCodeExpiration;
}
