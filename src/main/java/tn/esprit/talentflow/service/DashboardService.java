package tn.esprit.talentflow.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import tn.esprit.talentflow.dto.DashboardResponse;
import tn.esprit.talentflow.entity.ApplicationStatus;
import tn.esprit.talentflow.entity.JobStatus;
import tn.esprit.talentflow.repository.ApplicationRepository;
import tn.esprit.talentflow.repository.InterviewRepository;
import tn.esprit.talentflow.repository.JobRepository;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final InterviewRepository interviewRepository;

    public DashboardResponse getStatistics() {

        long totalApplications =
                applicationRepository.count();

        long pendingApplications =
                applicationRepository
                        .findByStatus(ApplicationStatus.PENDING)
                        .size();

        long reviewingApplications =
                applicationRepository
                        .findByStatus(ApplicationStatus.REVIEWING)
                        .size();

        long shortlistedApplications =
                applicationRepository
                        .findByStatus(ApplicationStatus.SHORTLISTED)
                        .size();

        long interviewApplications =
                applicationRepository
                        .findByStatus(ApplicationStatus.INTERVIEW)
                        .size();

        long acceptedApplications =
                applicationRepository
                        .findByStatus(ApplicationStatus.ACCEPTED)
                        .size();

        long rejectedApplications =
                applicationRepository
                        .findByStatus(ApplicationStatus.REJECTED)
                        .size();

        long totalJobs =
                jobRepository.count();

        long publishedJobs =
                jobRepository
                        .findByStatus(JobStatus.PUBLISHED)
                        .size();

        long totalInterviews =
                interviewRepository.count();

        return DashboardResponse.builder()
                .totalApplications(totalApplications)
                .pendingApplications(pendingApplications)
                .reviewingApplications(reviewingApplications)
                .shortlistedApplications(shortlistedApplications)
                .interviewApplications(interviewApplications)
                .acceptedApplications(acceptedApplications)
                .rejectedApplications(rejectedApplications)
                .totalJobs(totalJobs)
                .publishedJobs(publishedJobs)
                .totalInterviews(totalInterviews)
                .build();
    }
}