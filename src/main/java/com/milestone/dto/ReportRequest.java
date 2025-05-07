package com.milestone.dto;

public class ReportRequest {
    private Long boardId;
    private String reason;

    public ReportRequest() {
    }

    public Long getBoardId() {
        return boardId;
    }

    public void setBoardId(Long boardId) {
        this.boardId = boardId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
