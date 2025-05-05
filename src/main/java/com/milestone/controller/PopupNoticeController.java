package com.milestone.controller;

import com.milestone.dto.PopupNoticeRequest;
import com.milestone.entity.PopupNotice;
import com.milestone.service.PopupNoticeService;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notice")
@RequiredArgsConstructor
public class PopupNoticeController {

    private final PopupNoticeService popupNoticeService;

    // 공지사항 등록 (관리자 전용)
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> registerPopupNotice(@RequestBody PopupNoticeRequest request) {
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "공지사항 내용이 필요합니다.");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        popupNoticeService.saveNotice(request);

        Map<String, String> response = new HashMap<>();
        response.put("message", "공지사항이 성공적으로 등록되었습니다.");
        return ResponseEntity.ok(response);
    }

    // 최신 공지사항 조회
    @GetMapping("/latest")
    public ResponseEntity<?> getLatestNotice() {
        PopupNotice notice = popupNoticeService.getLatestNotice();
        if (notice == null) {
            return ResponseEntity.noContent().build();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("id", notice.getId());
        response.put("content", notice.getContent());
        response.put("createdDate", notice.getCreatedDate());

        return ResponseEntity.ok(response);
    }
}