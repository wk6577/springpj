package com.milestone.dto;

import lombok.Data;

<<<<<<< HEAD

import java.time.LocalDateTime;



@Data
public class SuspendMemberRequest {
    private LocalDateTime suspendUntil; // 정지 해제 날짜
    private String reason; // 🔥 정지 사유 필드 추가
=======
import java.time.LocalDateTime;

@Data
public class SuspendMemberRequest {
    private LocalDateTime suspendUntil; // 정지 해제 날짜
>>>>>>> e6af618a5dc17b79dd6e8793d684fa98a8eff71b
}
