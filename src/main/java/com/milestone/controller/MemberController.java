package com.milestone.controller;

import com.milestone.dto.MemberJoinRequest;
import com.milestone.dto.MemberLoginRequest;
import com.milestone.dto.MemberResponse;
import com.milestone.service.MemberService;
import lombok.RequiredArgsConstructor;
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

    private final MemberService memberService;

    /**
     * 회원 가입 API
     */
    @PostMapping("/join")
    public ResponseEntity<MemberResponse> join(@RequestBody @Valid MemberJoinRequest request) {
        MemberResponse response = memberService.join(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 로그인 API
     */
    @PostMapping("/login")
    public ResponseEntity<MemberResponse> login(@RequestBody @Valid MemberLoginRequest request,
                                                HttpSession session) {
        MemberResponse response = memberService.login(request, session);
        return ResponseEntity.ok(response);
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
}