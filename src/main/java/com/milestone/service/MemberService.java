package com.milestone.service;

import com.milestone.dto.MemberJoinRequest;
import com.milestone.dto.MemberLoginRequest;
import com.milestone.dto.MemberResponse;
import com.milestone.entity.Member;
import com.milestone.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private static final Logger logger = LoggerFactory.getLogger(MemberService.class);
    private final MemberRepository memberRepository;
    private static final String SESSION_KEY = "LOGGED_IN_MEMBER";

    /**
     * 회원 가입
     */
    @Transactional
    public MemberResponse join(MemberJoinRequest request) {
        logger.info("회원가입 시도: {}", request.getMemberEmail());

        // 이메일 중복 검사
        if (memberRepository.existsByMemberEmail(request.getMemberEmail())) {
            logger.warn("이메일 중복: {}", request.getMemberEmail());
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // 닉네임 중복 검사
        if (memberRepository.existsByMemberNickname(request.getMemberNickname())) {
            logger.warn("닉네임 중복: {}", request.getMemberNickname());
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        try {
            // Member 엔티티 생성 및 저장
            Member member = Member.builder()
                    .memberName(request.getMemberName())
                    .memberNickname(request.getMemberNickname())
                    .memberEmail(request.getMemberEmail())
                    .memberPassword(request.getMemberPassword()) // 실제로는 암호화 처리 필요
                    .memberPhone(request.getMemberPhone())
                    .memberPhoto(request.getMemberPhoto())
                    .memberIntroduce(request.getMemberIntroduce())
                    .memberVisible(request.getMemberVisible())
                    .memberStatus("active")
                    .build();

            Member savedMember = memberRepository.save(member);
            logger.info("회원가입 성공: ID={}", savedMember.getMemberNo());

            // 저장된 엔티티를 DTO로 변환하여 반환
            return MemberResponse.fromEntity(savedMember);
        } catch (Exception e) {
            logger.error("회원가입 실패: {}", e.getMessage(), e);
            throw new RuntimeException("회원가입 처리 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 로그인
     */
    @Transactional
    public MemberResponse login(MemberLoginRequest request, HttpSession session) {
        logger.info("로그인 시도: {}", request.getMemberEmail());

        try {
            // 이메일로 회원 조회
            Member member = memberRepository.findByMemberEmail(request.getMemberEmail())
                    .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 일치하지 않습니다."));

            // 비밀번호 검증 (실제로는 암호화된 비밀번호 비교 필요)
            if (!member.getMemberPassword().equals(request.getMemberPassword())) {
                logger.warn("비밀번호 불일치: {}", request.getMemberEmail());
                throw new IllegalArgumentException("이메일 또는 비밀번호가 일치하지 않습니다.");
            }

            // 마지막 로그인 시간 업데이트
            member.setMemberLastlogin(LocalDateTime.now());
            memberRepository.save(member);

            // 세션에 로그인 정보 저장
            session.setAttribute(SESSION_KEY, member.getMemberNo());
            logger.info("로그인 성공: ID={}", member.getMemberNo());

            // 사용자 정보를 DTO로 변환하여 반환
            return MemberResponse.fromEntity(member);
        } catch (IllegalArgumentException e) {
            logger.warn("로그인 실패: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("로그인 처리 중 오류: {}", e.getMessage(), e);
            throw new RuntimeException("로그인 처리 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 로그아웃
     */
    public void logout(HttpSession session) {
        Long memberNo = (Long) session.getAttribute(SESSION_KEY);
        if (memberNo != null) {
            logger.info("로그아웃: ID={}", memberNo);
        }
        // 세션 무효화
        session.invalidate();
    }

    /**
     * 현재 로그인한 회원 정보 조회
     */
    @Transactional(readOnly = true)
    public Optional<MemberResponse> getCurrentMember(HttpSession session) {
        Long memberNo = (Long) session.getAttribute(SESSION_KEY);

        if (memberNo == null) {
            return Optional.empty();
        }

        return memberRepository.findById(memberNo)
                .map(MemberResponse::fromEntity);
    }
}