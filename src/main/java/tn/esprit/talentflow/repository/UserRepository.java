package tn.esprit.talentflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.talentflow.entity.Role;
import tn.esprit.talentflow.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
    List<User> findByRole(Role role);
}
