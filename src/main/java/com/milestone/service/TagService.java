package com.milestone.service;

import com.milestone.entity.Board;
import com.milestone.entity.BoardTag;
import com.milestone.entity.Tag;
import com.milestone.repository.BoardTagRepository;
import com.milestone.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagService {

    private static final Logger logger = LoggerFactory.getLogger(TagService.class);
    private final TagRepository tagRepository;
    private final BoardTagRepository boardTagRepository;

    /**
     * 게시물에 태그 저장
     */
    @Transactional
    public void saveBoardTags(Board board, List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return;
        }

        for (String tagName : tagNames) {
            // 태그 저장 또는 기존 태그 조회
            Tag tag = saveOrGetTag(tagName);

            // 게시물-태그 연결 저장
            BoardTag boardTag = BoardTag.builder()
                    .board(board)
                    .tagName(tag.getTagName())
                    .build();

            boardTagRepository.save(boardTag);
        }
    }

    /**
     * 게시물의 태그 업데이트
     */
    @Transactional
    public void updateBoardTags(Board board, List<String> newTagNames) {
        // 기존 태그 삭제
        deleteBoardTags(board);

        // 새 태그 저장
        saveBoardTags(board, newTagNames);
    }

    /**
     * 게시물의 태그 삭제
     */
    @Transactional
    public void deleteBoardTags(Board board) {
        boardTagRepository.deleteByBoardBoardNo(board.getBoardNo());
    }

    /**
     * 특정 게시물의 태그 목록 조회
     */
    @Transactional(readOnly = true)
    public List<String> getBoardTags(Long boardNo) {
        List<BoardTag> boardTags = boardTagRepository.findByBoardBoardNo(boardNo);
        return boardTags.stream()
                .map(BoardTag::getTagName)
                .collect(Collectors.toList());
    }

    /**
     * 태그 저장 또는 기존 태그 조회
     */
    @Transactional
    public Tag saveOrGetTag(String tagName) {
        return tagRepository.findByTagName(tagName)
                .orElseGet(() -> {
                    Tag newTag = Tag.builder()
                            .tagName(tagName)
                            .build();
                    return tagRepository.save(newTag);
                });
    }

    /**
     * 인기 태그 목록 조회
     */
    @Transactional(readOnly = true)
    public List<String> getPopularTags(int limit) {
        return boardTagRepository.findMostUsedTags(limit);
    }

    /**
     * JSON 문자열에서 태그 목록 추출 유틸리티 메서드
     */
    public List<String> parseTagsFromJson(String tagsJson) {
        if (tagsJson == null || tagsJson.isEmpty()) {
            return new ArrayList<>();
        }

        // JSON 배열 형식의 문자열 파싱 ("["tag1","tag2"]" 형식)
        try {
            // 따옴표, 대괄호 제거 후 쉼표로 분리
            tagsJson = tagsJson.replaceAll("[\\[\\]\"]", "");
            String[] tagArray = tagsJson.split(",");

            return java.util.Arrays.stream(tagArray)
                    .map(String::trim)
                    .filter(tag -> !tag.isEmpty())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("태그 파싱 중 오류 발생: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
}