// src/main/java/com/milestone/service/PopupNoticeService.java
package com.milestone.service;

import com.milestone.dto.PopupNoticeResponse;
import com.milestone.entity.PopupNotice;
import com.milestone.repository.PopupNoticeRepository;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PopupNoticeService {

    private final PopupNoticeRepository popupNoticeRepository;

    public void saveNotice(String content) {
        PopupNotice newNotice = PopupNotice.builder()
                .notice(content)
                .createdAt(LocalDateTime.now())
                .build();
        popupNoticeRepository.save(newNotice);
    }

    public PopupNotice getLatestNotice() {
        return popupNoticeRepository.findTopByOrderByCreatedAtDesc();
    }
}
