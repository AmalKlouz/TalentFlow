package tn.esprit.talentflow.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "interviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Interview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Candidature concernée
    @OneToOne(optional = false)
    @JoinColumn(name = "application_id", unique = true)
    private Application application;

    // Date et heure de l'entretien
    @Column(nullable = false)
    private LocalDateTime scheduledAt;

    // Durée en minutes
    @Column(nullable = false)
    private Integer duration;

    // Type : ONLINE ou ONSITE
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InterviewType type;

    // Lieu ou lien Google Meet / Teams...
    private String location;

    // Notes du RH / recruteur
    @Column(columnDefinition = "TEXT")
    private String notes;

    // Statut de l'entretien
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InterviewStatus status;
}