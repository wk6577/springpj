<<<<<<< HEAD
package com.milestone.repository;

import com.milestone.entity.PopupNotice;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PopupNoticeRepository extends JpaRepository<PopupNotice, Long> {

    PopupNotice findTopByOrderByCreatedAtDesc();

    PopupNotice findTopByStartDateBeforeAndEndDateAfterOrderByStartDateDesc(LocalDateTime start, LocalDateTime end);

=======
package com.milestone.repository;

import com.milestone.entity.PopupNotice;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PopupNoticeRepository extends JpaRepository<PopupNotice, Long> {

    PopupNotice findTopByOrderByCreatedAtDesc();

    PopupNotice findTopByStartDateBeforeAndEndDateAfterOrderByStartDateDesc(LocalDateTime start, LocalDateTime end);

>>>>>>> e6af618a5dc17b79dd6e8793d684fa98a8eff71b
}