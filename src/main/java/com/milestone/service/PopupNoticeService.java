package com.milestone.service;

import com.milestone.dto.PopupNoticeRequest;
import com.milestone.dto.PopupNoticeResponse;
import com.milestone.entity.PopupNotice;
import com.milestone.repository.PopupNoticeRepository;
import lombok.RequiredArgsConstructor;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PopupNoticeService {

    private final PopupNoticeRepository popupNoticeRepository;

    public void saveNotice(PopupNoticeRequest request) {
        System.out.println("📥 받은 공지사항 요청");
        System.out.println("내용: " + request.getContent());
        System.out.println("시작일: " + request.getStartDate());
        System.out.println("종료일: " + request.getEndDate());

        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

        PopupNotice newNotice = PopupNotice.builder()
                .content(request.getContent())
                .startDate(LocalDateTime.parse(request.getStartDate(), formatter)) // String → LocalDateTime
                .endDate(LocalDateTime.parse(request.getEndDate(), formatter)) // String → LocalDateTime
                .createdAt(LocalDateTime.now())
                .build();

        popupNoticeRepository.save(newNotice);
    }

    public PopupNotice getLatestNotice() {
        return popupNoticeRepository.findTopByOrderByCreatedAtDesc();
    }

}