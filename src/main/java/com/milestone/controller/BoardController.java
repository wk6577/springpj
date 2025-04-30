package com.milestone.controller;

import com.milestone.dto.BoardRequest;
import com.milestone.dto.BoardResponse;
import com.milestone.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {

    private static final Logger logger = LoggerFactory.getLogger(BoardController.class);
    private final BoardService boardService;

    /**
     * 모든 게시물 조회 API
     */
    @GetMapping
    public ResponseEntity<List<BoardResponse>> getAllBoards() {
        logger.info("모든 게시물 조회 요청");
        List<BoardResponse> boards = boardService.getAllBoards();
        return ResponseEntity.ok(boards);
    }

    /**
     * 특정 게시물 조회 API
     */
    @GetMapping("/{boardNo}")
    public ResponseEntity<BoardResponse> getBoardById(@PathVariable Long boardNo, HttpSession session) {
        logger.info("게시물 조회 요청 - ID: {}", boardNo);
        BoardResponse board = boardService.getBoardById(boardNo, session);
        return ResponseEntity.ok(board);
    }

    /**
     * 스터디 게시물 조회 API
     */
    @GetMapping("/study")
    public ResponseEntity<List<BoardResponse>> getStudyBoards() {
        logger.info("스터디 게시물 조회 요청");
        List<BoardResponse> studyBoards = boardService.getBoardsByType("study");
        return ResponseEntity.ok(studyBoards);
    }

    /**
     * 게시물 작성 API
     */
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<Object> createBoard(
            @RequestPart("board") @Valid BoardRequest boardRequest,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            HttpSession session) {

        try {
            logger.info("게시물 작성 요청 - 타입: {}, 제목: {}", boardRequest.getBoardType(), boardRequest.getBoardTitle());
            logger.debug("게시물 내용: {}", boardRequest.getBoardContent());
            logger.debug("게시물 공개 범위: {}", boardRequest.getBoardVisible());
            logger.debug("태그: {}", boardRequest.getTags());
            
            if (images != null) {
                logger.debug("이미지 개수: {}", images.size());
                for (MultipartFile image : images) {
                    logger.debug("이미지 정보 - 이름: {}, 크기: {}, 타입: {}", 
                        image.getOriginalFilename(), 
                        image.getSize(), 
                        image.getContentType());
                }
            }

            // 이미지 리스트를 DTO에 수동 주입 (RequestPart 분리 방식일 때 필요)
            boardRequest.setImages(images);

            BoardResponse response = boardService.createBoard(boardRequest, session);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            logger.warn("게시물 작성 실패 - 잘못된 요청: {}", e.getMessage());
            logger.debug("상세 에러: ", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);

        } catch (Exception e) {
            logger.error("게시물 작성 실패 - 서버 오류: {}", e.getMessage(), e);
            logger.debug("상세 에러: ", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "게시물 작성 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


    /**
     * 게시물 수정 API
     */
    @PutMapping("/{boardNo}")
    public ResponseEntity<Object> updateBoard(
            @PathVariable Long boardNo,
            @RequestBody @Valid BoardRequest boardRequest,
            HttpSession session) {

        try {
            logger.info("게시물 수정 요청 - ID: {}", boardNo);
            BoardResponse response = boardService.updateBoard(boardNo, boardRequest, session);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.warn("게시물 수정 실패 - 잘못된 요청: {}", e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);

        } catch (Exception e) {
            logger.error("게시물 수정 실패 - 서버 오류: {}", e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "게시물 수정 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 게시물 삭제 API
     */
    @DeleteMapping("/{boardNo}")
    public ResponseEntity<Object> deleteBoard(@PathVariable Long boardNo, HttpSession session) {
        try {
            logger.info("게시물 삭제 요청 - ID: {}", boardNo);
            boardService.deleteBoard(boardNo, session);

            Map<String, String> response = new HashMap<>();
            response.put("message", "게시물이 삭제되었습니다.");
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.warn("게시물 삭제 실패 - 권한 없음: {}", e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);

        } catch (Exception e) {
            logger.error("게시물 삭제 실패 - 서버 오류: {}", e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "게시물 삭제 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 특정 사용자의 게시물 조회 API
     */
    @GetMapping("/member/{memberNo}")
    public ResponseEntity<List<BoardResponse>> getBoardsByMember(@PathVariable Long memberNo) {
        logger.info("사용자별 게시물 조회 요청 - 회원 ID: {}", memberNo);
        List<BoardResponse> boards = boardService.getBoardsByMember(memberNo);
        return ResponseEntity.ok(boards);
    }
}