package com.milestone.dto;

import java.time.LocalDateTime;

public class ReportResponse {
    private Long id;
    private Long boardId;
    private Long reporterId;
    private String reason;
    private String status;
    private LocalDateTime createdAt;

    public ReportResponse() {
    }

    public ReportResponse(Long id,
            Long boardId,
            Long reporterId,
            String reason,
            String status,
            LocalDateTime createdAt) {
        this.id = id;
        this.boardId = boardId;
        this.reporterId = reporterId;
        this.reason = reason;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBoardId() {
        return boardId;
    }

    public void setBoardId(Long boardId) {
        this.boardId = boardId;
    }

    public Long getReporterId() {
        return reporterId;
    }

    public void setReporterId(Long reporterId) {
        this.reporterId = reporterId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
