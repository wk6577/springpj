package com.milestone.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberUpdateRequest {
    private String memberName;
    private String memberNickname;
    private String memberIntroduce;
    private String resetProfileImage; // 프로필 이미지 초기화 여부
}