package tn.esprit.talentflow.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardResponse {

    private long totalApplications;

    private long pendingApplications;

    private long reviewingApplications;

    private long shortlistedApplications;

    private long interviewApplications;

    private long acceptedApplications;

    private long rejectedApplications;

    private long totalJobs;

    private long publishedJobs;

    private long totalInterviews;
}