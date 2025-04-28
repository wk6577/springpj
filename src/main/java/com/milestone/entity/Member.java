package com.milestone.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "member")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_no")
    private Long memberNo;

    @Column(name = "member_name", nullable = false, length = 20)
    private String memberName;

    @Column(name = "member_nickname", nullable = false, length = 20, unique = true)
    private String memberNickname;

    @Column(name = "member_email", nullable = false, length = 30, unique = true)
    private String memberEmail;

    @Column(name = "member_password", nullable = false, length = 100)
    private String memberPassword;

    @Column(name = "member_phone", length = 20)
    private String memberPhone;

    @Column(name = "member_photo", length = 1000)
    private String memberPhoto;

    // 이미지 바이너리 데이터 추가
    @Lob
    @Column(name = "member_photo_data", columnDefinition = "LONGBLOB")
    private byte[] memberPhotoData;

    // 이미지 MIME 타입 추가
    @Column(name = "member_photo_type", length = 100)
    private String memberPhotoType;

    @Column(name = "member_introduce", length = 500)
    private String memberIntroduce;

    @Column(name = "member_visible", nullable = false, columnDefinition = "VARCHAR(10) DEFAULT 'public'")
    private String memberVisible = "public";

    @Column(name = "member_qualify", length = 10)
    private String memberQualify;

    @Column(name = "member_status", nullable = false, columnDefinition = "VARCHAR(10) DEFAULT 'active'")
    private String memberStatus = "active";

    @CreationTimestamp
    @Column(name = "member_joindate", nullable = false, updatable = false)
    private LocalDateTime memberJoindate;

    @Column(name = "member_lastlogin")
    private LocalDateTime memberLastlogin;
}