package com.milestone.controller;

import com.milestone.entity.Notice;
import com.milestone.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);
    private final NotificationService notificationService;

    /**
     * 현재 로그인한 사용자의 알림 목록 조회 API
     */
    @GetMapping
    public ResponseEntity<List<Notice>> getNotifications(HttpSession session) {
        logger.info("알림 목록 조회 요청");
        List<Notice> notifications = notificationService.getNotifications(session);
        return ResponseEntity.ok(notifications);
    }

    /**
     * 알림 읽음 처리 API
     */
    @PutMapping("/{noticeNo}/read")
    public ResponseEntity<Object> markNotificationAsRead(@PathVariable Long noticeNo, HttpSession session) {
        try {
            logger.info("알림 읽음 처리 요청 - 알림 ID: {}", noticeNo);
            notificationService.markNotificationAsRead(noticeNo, session);

            Map<String, String> response = new HashMap<>();
            response.put("message", "알림이 읽음 처리되었습니다.");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("알림 읽음 처리 실패: {}", e.getMessage());

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 알림 삭제 API
     */
    @DeleteMapping("/{noticeNo}")
    public ResponseEntity<Object> deleteNotification(@PathVariable Long noticeNo, HttpSession session) {
        try {
            logger.info("알림 삭제 요청 - 알림 ID: {}", noticeNo);
            notificationService.deleteNotification(noticeNo, session);

            Map<String, String> response = new HashMap<>();
            response.put("message", "알림이 삭제되었습니다.");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("알림 삭제 실패: {}", e.getMessage());

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 모든 알림 읽음 처리 API
     */
    @PutMapping("/read-all")
    public ResponseEntity<Object> markAllNotificationsAsRead(HttpSession session) {
        try {
            logger.info("모든 알림 읽음 처리 요청");
            notificationService.markAllNotificationsAsRead(session);

            Map<String, String> response = new HashMap<>();
            response.put("message", "모든 알림이 읽음 처리되었습니다.");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("모든 알림 읽음 처리 실패: {}", e.getMessage());

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 읽지 않은 알림 개수 조회 API
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Object> getUnreadNotificationCount(HttpSession session) {
        try {
            logger.info("읽지 않은 알림 개수 조회 요청");
            Long memberNo = (Long) session.getAttribute("LOGGED_IN_MEMBER");

            if (memberNo == null) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "로그인이 필요합니다.");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            long unreadCount = notificationService.getUnreadNotificationCount(memberNo);

            Map<String, Long> response = new HashMap<>();
            response.put("unreadCount", unreadCount);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("읽지 않은 알림 개수 조회 실패: {}", e.getMessage(), e);

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "읽지 않은 알림 개수 조회 중 오류가 발생했습니다.");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}