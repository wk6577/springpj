package com.milestone.repository;

import com.milestone.entity.PopupNotice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PopupNoticeRepository extends JpaRepository<PopupNotice, Long> {

    // 최신 공지사항 하나만 가져오기
    PopupNotice findTopByOrderByCreatedAtDesc(); // createdAt 이름 정확히 사용!
}
