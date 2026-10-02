package tn.esprit.talentflow.dto;

import lombok.*;
import tn.esprit.talentflow.entity.Role;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String token;
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private Role role;
}
