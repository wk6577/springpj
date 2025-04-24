package com.milestone.repository;

import com.milestone.entity.Board;
import com.milestone.entity.BoardTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoardTagRepository extends JpaRepository<BoardTag, Long> {

    // 게시물별 태그 조회
    List<BoardTag> findByBoardBoardNo(Long boardNo);

    // 태그명으로 게시물 태그 조회
    List<BoardTag> findByTagName(String tagName);

    // 게시물별 태그 삭제
    void deleteByBoardBoardNo(Long boardNo);

    // 태그명별 게시물 태그 삭제
    void deleteByTagName(String tagName);

    // 가장 많이 사용된 태그 조회
    @Query(value = "SELECT tag_name FROM board_tag GROUP BY tag_name ORDER BY COUNT(tag_name) DESC LIMIT :limit", nativeQuery = true)
    List<String> findMostUsedTags(@Param("limit") int limit);

    // 특정 게시물에 대한 태그 존재 여부 확인
    boolean existsByBoardBoardNoAndTagName(Long boardNo, String tagName);
}