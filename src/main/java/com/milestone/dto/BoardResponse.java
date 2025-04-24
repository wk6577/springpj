package com.milestone.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardResponse {

    private Long boardNo;

    private Long memberNo;

    private String memberNickname;

    private String boardType; // "daily" or "study"

    private String boardCategory;

    private String boardTitle;

    private String boardContent;

    private Long boardLike;

    private Long boardScrap;

    private Long boardReadhit;

    private String boardVisible; // "public", "logined", "follow"

    private LocalDateTime boardInputdate;

    private String boardImage; // 대표 이미지 URL

    private List<String> tags; // 태그 목록

    private boolean isLiked; // 현재 사용자의 좋아요 상태

    private boolean isScraped; // 현재 사용자의 스크랩 상태
}