package com.milestone.controller; // 실제 경로에 맞춰 변경

import com.milestone.dto.ProcessReportRequest;
import com.milestone.dto.ReportResponse;
import com.milestone.entity.Report;
import com.milestone.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/reports")
public class AdminReportController {

    private final ReportService reportService;

    public AdminReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /** 모든 신고 리스트 조회 */
    @GetMapping
    public ResponseEntity<List<ReportResponse>> listAll() {
        List<ReportResponse> list = reportService.getAllReports().stream()
                .map(r -> new ReportResponse(
                        r.getReportId(),
                        r.getReportedBoardNo(),
                        r.getReporterMemberNo(),
                        r.getReason(),
                        r.getStatus().name(),
                        r.getCreatedAt()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    /** 신고 처리 (숨기기, 삭제, 계정 정지) */
    @PostMapping("/process")
    public ResponseEntity<Void> process(@RequestBody ProcessReportRequest req) {
        // 1) 신고 정보 조회
        Report rpt = reportService.getReport(req.getReportId());
        Long boardNo = rpt.getReportedBoardNo();

        switch (req.getAction()) {
            case "HIDE":
                reportService.hideBoard(boardNo);
                break;

            case "DELETE":
                reportService.deleteBoard(boardNo);
                break;

            case "SUSPEND":
                // 신고 대상자 = 신고한 글의 작성자
                Long offenderId = rpt.getReporterMemberNo();
                reportService.suspendMember(
                        offenderId,
                        req.getSuspendUntil(),
                        req.getSuspendReason());
                break;

            default:
                throw new IllegalArgumentException("알 수 없는 action: " + req.getAction());
        }

        // 2) 신고 상태를 RESOLVED 로 변경
        reportService.resolveReport(req.getReportId());
        return ResponseEntity.ok().build();
    }
}
