package com.milestone.controller;

import com.milestone.service.ReportService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    @Autowired
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping
    public ResponseEntity<?> createReport(@RequestBody Map<String, Object> payload, HttpSession session) {
        try {
            Long reporterMemberNo = (Long) session.getAttribute("memberNo"); // 현재 로그인된 사용자 ID
            Long reportedBoardNo = Long.valueOf(payload.get("reportedBoardNo").toString());
            String reason = payload.getOrDefault("reason", "").toString();

            reportService.createReport(reportedBoardNo, reporterMemberNo, reason);

            return ResponseEntity.ok("신고가 정상적으로 접수되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("신고 처리 중 오류가 발생했습니다.");
        }
    }
}
