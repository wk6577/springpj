// src/main/java/com/milestone/dto/PopupNoticeResponse.java
package com.milestone.dto;

import com.milestone.entity.PopupNotice;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PopupNoticeResponse {

    private Long id;
    private String content;
    private LocalDateTime createdDate;

    public static PopupNoticeResponse fromEntity(PopupNotice notice) {
        return PopupNoticeResponse.builder()
                .id(notice.getId())
                .content(notice.getContent())
                .createdDate(notice.getCreatedDate())
                .build();
    }
}
