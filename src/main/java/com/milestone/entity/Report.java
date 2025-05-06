package com.milestone.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "report")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportId;

    @Column(nullable = false)
    private Long reportedBoardNo;

    @Column(nullable = false)
    private Long reporterMemberNo;

    @Column(length = 255)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status = ReportStatus.PENDING;

    public ReportStatus getStatus() {
        return status;
    }

    public void setStatus(ReportStatus status) {
        this.status = status;
    }

    @CreationTimestamp
    private LocalDateTime createdAt;

    // getters and setters

    public Long getReportId() {
        return reportId;
    }

    public void setReportId(Long reportId) {
        this.reportId = reportId;
    }

    public Long getReportedBoardNo() {
        return reportedBoardNo;
    }

    public void setReportedBoardNo(Long reportedBoardNo) {
        this.reportedBoardNo = reportedBoardNo;
    }

    public Long getReporterMemberNo() {
        return reporterMemberNo;
    }

    public void setReporterMemberNo(Long reporterMemberNo) {
        this.reporterMemberNo = reporterMemberNo;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
