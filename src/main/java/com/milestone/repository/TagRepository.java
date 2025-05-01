package com.milestone.repository;

import com.milestone.entity.Board;
import com.milestone.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    // 게시물별 태그 조회
    List<Tag> findByBoardBoardNo(Long boardNo);

    // 태그명과 게시물로 존재 여부 확인
    boolean existsByBoardBoardNoAndTagName(Long boardNo, String tagName);

    // 태그명 존재 여부 확인 (전체 태그 중에서)
    boolean existsByTagName(String tagName);

    // 게시물별 태그 삭제
    void deleteByBoardBoardNo(Long boardNo);

    // 태그명으로 태그 목록 조회
    List<Tag> findByTagName(String tagName);
}