package com.milestone.repository;

import com.milestone.entity.PopupNotice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PopupNoticeRepository extends JpaRepository<PopupNotice, Long> {

    PopupNotice findTopByOrderByCreatedDateDesc();
}