package tn.esprit.talentflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import tn.esprit.talentflow.entity.Job;
import tn.esprit.talentflow.entity.JobStatus;

import java.util.List;

public interface JobRepository extends JpaRepository<Job, Long> {

    List<Job> findByStatus(JobStatus status);

    List<Job> findByLocation(String location);

    List<Job> findByContractType(String contractType);

    List<Job> findByExperienceLevel(String experienceLevel);
}
