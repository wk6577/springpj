package com.milestone.repository;

import com.milestone.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {

    // 게시물 최신순 정렬 조회
    List<Board> findAllByOrderByBoardInputdateDesc();

    // 타입별 게시물 조회
    List<Board> findByBoardTypeOrderByBoardInputdateDesc(String boardType);

    // 회원별 게시물 조회
    List<Board> findByMemberMemberNoOrderByBoardInputdateDesc(Long memberNo);

    // 카테고리별 게시물 조회
    List<Board> findByBoardCategoryOrderByBoardInputdateDesc(String category);

    // 공개 범위별 게시물 조회
    List<Board> findByBoardVisibleOrderByBoardInputdateDesc(String visible);

    // 멤버와 타입으로 게시물 조회
    List<Board> findByMemberMemberNoAndBoardTypeOrderByBoardInputdateDesc(Long memberNo, String boardType);

    // ID 목록으로 게시물 조회
    List<Board> findByBoardNoIn(List<Long> boardIds);
}