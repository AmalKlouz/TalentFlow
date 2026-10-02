package tn.esprit.talentflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.talentflow.entity.Application;
import tn.esprit.talentflow.entity.ApplicationStatus;

import java.util.List;

public interface ApplicationRepository
        extends JpaRepository<Application, Long> {

    List<Application> findByCandidateId(Long candidateId);

    List<Application> findByJobId(Long jobId);

    boolean existsByCandidateIdAndJobId(
            Long candidateId,
            Long jobId
    );

    List<Application> findByStatus(ApplicationStatus status);
}
