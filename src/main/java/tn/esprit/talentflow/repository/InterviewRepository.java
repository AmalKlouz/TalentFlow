package tn.esprit.talentflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.talentflow.entity.Interview;

import java.util.List;

public interface InterviewRepository
        extends JpaRepository<Interview, Long> {

    boolean existsByApplicationId(Long applicationId);

    List<Interview> findByApplicationCandidateId(Long candidateId);

    List<Interview> findAllByOrderByScheduledAtAsc();

    List<Interview> findByApplicationId(Long applicationId);
}