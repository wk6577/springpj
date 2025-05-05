package com.milestone.service;

import com.milestone.dto.PopupNoticeRequest;
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

    public void saveNotice(PopupNoticeRequest request) {
        PopupNotice newNotice = PopupNotice.builder()
                .content(request.getContent())
                .createdDate(LocalDateTime.now())
                .build();

        popupNoticeRepository.save(newNotice);
    }

    public PopupNotice getLatestNotice() {
        return popupNoticeRepository.findTopByOrderByCreatedDateDesc();
    }
}