package com.milestone.repository;

import com.milestone.entity.Report;
import com.milestone.entity.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
    /** 상태별 신고 조회 */
    List<Report> findByStatus(ReportStatus status);
}
