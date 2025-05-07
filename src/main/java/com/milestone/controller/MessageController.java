package com.milestone.controller;

import com.milestone.dto.MemberResponse;
import com.milestone.dto.MessageRequest;
import com.milestone.entity.Member;
import com.milestone.entity.Message;
import com.milestone.service.MemberService;
import com.milestone.service.MessageService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MemberService memberService;
    private final MessageService messageService;

    @PostMapping
    public ResponseEntity<?> sendMessage(@RequestBody MessageRequest requestDTO,HttpSession session) {
        try {
            // 현재 로그인한 사용자 정보 가져오기
            Optional<MemberResponse> login = memberService.getCurrentMember(session);
            if (!login.isPresent()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "로그인이 필요합니다."));
            }

            MemberResponse loginUser = login.get();
            Member sender = memberService.findByMemberNo(loginUser.getMemberNo());


            // 메시지 내용 유효성 검사
            if (requestDTO.getContent() == null || requestDTO.getContent().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                        Map.of("success", false, "message", "메시지 내용은 필수입니다.")
                );
            }

            List<Long> receivers = requestDTO.getRecipients();

            // 수신자 목록 유효성 검사
            if (receivers == null || receivers.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "수신자는 최소 1명 이상이어야 합니다."));
            }

            // 각 수신자에게 메시지 전송
            List<Message> savedMessages = new ArrayList<>();

            for (Long receiverNo : receivers) {
                Member receiver = memberService.findByMemberNo(receiverNo);

                if (receiver != null) {
                    // Message 엔티티 생성
                    Message message = Message.builder()
                            .messageFrom(sender)
                            .messageTo(receiver)
                            .messageContent(requestDTO.getContent())
                            .messageToVisible(true)
                            .messageFromVisible(true)
                            .messageFromCheck(false)
                            .messageInputdate(LocalDateTime.now())
                            .build();

                    savedMessages.add(messageService.saveMessage(message));
                }
            }

            return ResponseEntity.ok(
                    Map.of(
                            "success", true,
                            "message", "메시지가 성공적으로 전송되었습니다.",
                            "count", savedMessages.size()
                    )
            );

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "메시지 전송 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }
}
