package tn.esprit.talentflow.dto;

import lombok.*;

import tn.esprit.talentflow.entity.Role;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long id;

    private String firstName;

    private String lastName;

    private String email;

    private Role role;

    private boolean enabled;
}
