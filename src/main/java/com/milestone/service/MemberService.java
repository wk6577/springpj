package com.milestone.service;

import com.milestone.dto.MemberJoinRequest;
import com.milestone.dto.MemberLoginRequest;
import com.milestone.dto.MemberResponse;
import com.milestone.dto.MemberUpdateRequest;
import com.milestone.entity.Member;
import com.milestone.repository.MemberRepository;
import com.milestone.util.PasswordUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MemberService {

    private static final Logger logger = LoggerFactory.getLogger(MemberService.class);
    private final MemberRepository memberRepository;
    private static final String SESSION_KEY = "LOGGED_IN_MEMBER";
    private static final String UPLOAD_DIR = "./src/main/resources/static/uploads/";
    private static final String DEFAULT_PROFILE_IMAGE = "/icon/profileimage.png";

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
            // 비밀번호 암호화
            String hashedPassword = PasswordUtils.hashPassword(request.getMemberPassword());

            // Member 엔티티 생성 및 저장
            Member member = Member.builder()
                    .memberName(request.getMemberName())
                    .memberNickname(request.getMemberNickname())
                    .memberEmail(request.getMemberEmail())
                    .memberPassword(hashedPassword)
                    .memberPhone(request.getMemberPhone())
                    .memberPhoto(DEFAULT_PROFILE_IMAGE)  // 기본 프로필 이미지 설정
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

            // 회원 상태 확인
            if ("inactive".equals(member.getMemberStatus())) {
                throw new IllegalArgumentException("탈퇴한 회원입니다.");
            }

            // 비밀번호 검증
            if (!PasswordUtils.verifyPassword(request.getMemberPassword(), member.getMemberPassword())) {
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

    /**
     * 회원 정보 수정
     */
    @Transactional
    public MemberResponse updateMember(MemberUpdateRequest request, MultipartFile profileImage, HttpSession session) {
        Long memberNo = (Long) session.getAttribute(SESSION_KEY);
        if (memberNo == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }

        Member member = memberRepository.findById(memberNo)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        // 닉네임 변경 시 중복 체크
        if (request.getMemberNickname() != null && !request.getMemberNickname().equals(member.getMemberNickname())) {
            if (memberRepository.existsByMemberNickname(request.getMemberNickname())) {
                throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
            }
            member.setMemberNickname(request.getMemberNickname());
        }

        // 이름 변경
        if (request.getMemberName() != null && !request.getMemberName().isEmpty()) {
            member.setMemberName(request.getMemberName());
        }

        // 자기소개 변경
        if (request.getMemberIntroduce() != null) {
            member.setMemberIntroduce(request.getMemberIntroduce());
        }

        // 계정 공개 범위 변경
        if (request.getMemberVisible() != null && !request.getMemberVisible().isEmpty()) {
            member.setMemberVisible(request.getMemberVisible());
        }

        // 알림 설정 변경
        if (request.getNotificationSettings() != null) {
            // 알림 설정을 저장하는 추가 로직
            // 예: 별도의 알림 설정 테이블이 있다면 여기서 처리
            logger.info("알림 설정 업데이트: {}", request.getNotificationSettings());
            // 현재는 프론트엔드에서만 관리하는 것으로 가정
        }

        // 프로필 이미지 초기화 요청 확인
        boolean resetToDefault = request.getResetProfileImage() != null &&
                request.getResetProfileImage().equals("true");

        if (resetToDefault) {
            // 기본 이미지로 설정
            member.setMemberPhoto(DEFAULT_PROFILE_IMAGE);
            logger.info("프로필 이미지 초기화: {}", member.getMemberNo());
        }
        // 새 프로필 이미지 업로드 요청 확인
        else if (profileImage != null && !profileImage.isEmpty()) {
            try {
                // 이미지 저장 경로 생성
                Path targetPath = Paths.get(UPLOAD_DIR);
                if (!Files.exists(targetPath)) {
                    Files.createDirectories(targetPath);
                }

                // 파일명 생성
                String fileName = "profile_" + UUID.randomUUID().toString() + "_" + profileImage.getOriginalFilename();
                Path filePath = targetPath.resolve(fileName);

                // 파일 저장
                Files.copy(profileImage.getInputStream(), filePath);

                // 경로 저장
                member.setMemberPhoto("/uploads/" + fileName);

                logger.info("프로필 이미지 업데이트: {}", fileName);
            } catch (IOException e) {
                logger.error("프로필 이미지 저장 중 오류: {}", e.getMessage(), e);
                throw new RuntimeException("프로필 이미지 저장 중 오류가 발생했습니다.", e);
            }
        }

        // 회원 정보 업데이트
        Member updatedMember = memberRepository.save(member);
        logger.info("회원 정보 업데이트 성공: ID={}", updatedMember.getMemberNo());

        return MemberResponse.fromEntity(updatedMember);
    }

    /**
     * 회원 탈퇴
     */
    @Transactional
    public void withdrawMember(HttpSession session) {
        Long memberNo = (Long) session.getAttribute(SESSION_KEY);
        if (memberNo == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }

        Member member = memberRepository.findById(memberNo)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        // 회원 상태를 "inactive"로 변경 (실제 삭제 대신 논리적 삭제)
        member.setMemberStatus("inactive");
        memberRepository.save(member);

        // 세션 무효화
        session.invalidate();

        logger.info("회원 탈퇴 완료: ID={}", memberNo);
    }

    /**
     * 닉네임으로 회원 정보 조회
     */
    @Transactional(readOnly = true)
    public MemberResponse getMemberByNickname(String nickname) {
        Member member = memberRepository.findByMemberNickname(nickname)
                .orElseThrow(() -> new IllegalArgumentException("해당 닉네임의 회원을 찾을 수 없습니다."));

        return MemberResponse.fromEntity(member);
    }
}