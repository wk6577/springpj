// src/main/java/com/milestone/controller/PopupNoticeController.java
package com.milestone.controller;

import com.milestone.entity.PopupNotice;
import com.milestone.service.PopupNoticeService;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notice")
@RequiredArgsConstructor
public class PopupNoticeController {

    private final PopupNoticeService popupNoticeService;

    // 공지사항 등록 (관리자 전용)
    @PostMapping("/register")
    public ResponseEntity<String> registerPopupNotice(@RequestBody String content) {
        popupNoticeService.saveNotice(content);
        return ResponseEntity.ok("공지사항이 등록되었습니다.");
    }

    // 최신 공지사항 조회

    @GetMapping("/latest")
    public ResponseEntity<?> getLatestNotice() {
        PopupNotice notice = popupNoticeService.getLatestNotice();
        if (notice == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(notice);
    }
}
