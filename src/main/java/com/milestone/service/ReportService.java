package com.milestone.service;

import com.milestone.entity.Report;
import com.milestone.entity.ReportStatus;
import com.milestone.repository.ReportRepository;
import com.milestone.repository.BoardRepository;
import com.milestone.repository.MemberRepository;
import com.milestone.entity.Board;
import com.milestone.entity.Member;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;

    public ReportService(ReportRepository reportRepository,
            BoardRepository boardRepository,
            MemberRepository memberRepository) {
        this.reportRepository = reportRepository;
        this.boardRepository = boardRepository;
        this.memberRepository = memberRepository;
    }

    public Report createReport(Long reportedBoardNo, Long reporterMemberNo, String reason) {
        Report report = new Report();
        report.setReportedBoardNo(reportedBoardNo);
        report.setReporterMemberNo(reporterMemberNo);
        report.setReason(reason);
        report.setStatus(ReportStatus.PENDING);
        return reportRepository.save(report);
    }

    public List<Report> getAllReports() {
        return reportRepository.findAll();
    }

    public Report getReport(Long reportId) {
        return reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("해당 신고가 존재하지 않습니다."));
    }

    public void hideBoard(Long boardNo) {
        Optional<Board> boardOpt = boardRepository.findById(boardNo);
        boardOpt.ifPresent(board -> {
            board.setVisible(false);
            boardRepository.save(board);
        });
    }

    public void deleteBoard(Long boardNo) {
        boardRepository.deleteById(boardNo);
    }

    public void suspendMember(Long memberNo, LocalDateTime suspendUntil, String reason) {
        Member member = memberRepository.findById(memberNo)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원이 존재하지 않습니다."));
        member.setMemberStatus("suspended");
        member.setMemberSuspendUntil(suspendUntil);
        member.setMemberSuspendReason(reason);
        memberRepository.save(member);
    }

    public void resolveReport(Long reportId) {
        Report report = getReport(reportId);
        report.setStatus(ReportStatus.RESOLVED);
        reportRepository.save(report);
    }
}
