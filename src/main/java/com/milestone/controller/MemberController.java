package com.milestone.controller;

import com.milestone.dto.MemberJoinRequest;
import com.milestone.dto.MemberLoginRequest;
import com.milestone.dto.MemberResponse;
import com.milestone.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private static final Logger logger = LoggerFactory.getLogger(MemberController.class);
    private final MemberService memberService;

    /**
     * 회원 가입 API
     */
    @PostMapping("/join")
    public ResponseEntity<Object> join(@RequestBody @Valid MemberJoinRequest request) {
        try {
            logger.info("회원가입 요청 - 이메일: {}, 닉네임: {}", request.getMemberEmail(), request.getMemberNickname());
            MemberResponse response = memberService.join(request);
            logger.info("회원가입 성공 - ID: {}", response.getMemberNo());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            logger.warn("회원가입 실패 - 잘못된 요청: {}", e.getMessage());

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            logger.error("회원가입 실패 - 서버 오류: {}", e.getMessage(), e);

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "회원가입 처리 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 로그인 API
     */
    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody @Valid MemberLoginRequest request,
                                        HttpSession session) {
        try {
            logger.info("로그인 요청 - 이메일: {}", request.getMemberEmail());
            MemberResponse response = memberService.login(request, session);
            logger.info("로그인 성공 - ID: {}", response.getMemberNo());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("로그인 실패 - 인증 오류: {}", e.getMessage());

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        } catch (Exception e) {
            logger.error("로그인 실패 - 서버 오류: {}", e.getMessage(), e);

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "로그인 처리 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 로그아웃 API
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpSession session) {
        memberService.logout(session);

        Map<String, String> response = new HashMap<>();
        response.put("message", "로그아웃 되었습니다.");

        return ResponseEntity.ok(response);
    }

    /**
     * 현재 로그인한 회원 정보 조회 API
     */
    @GetMapping("/me")
    public ResponseEntity<Object> getCurrentMember(HttpSession session) {
        Optional<MemberResponse> currentMember = memberService.getCurrentMember(session);

        if (currentMember.isPresent()) {
            return ResponseEntity.ok(currentMember.get());
        }

        // 로그인되지 않은 경우
        Map<String, String> response = new HashMap<>();
        response.put("message", "로그인이 필요합니다.");
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    /**
     * 서버 상태 확인 API (디버깅용)
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "ok");
        response.put("message", "서버가 정상적으로 동작 중입니다.");
        return ResponseEntity.ok(response);
    }
}