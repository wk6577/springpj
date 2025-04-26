package com.milestone.controller;

import com.milestone.entity.BoardImage;
import com.milestone.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {

    private static final Logger logger = LoggerFactory.getLogger(ImageController.class);
    private final BoardService boardService;

    /**
     * 이미지 데이터 제공 API
     * 이미지 ID를 받아 해당 이미지의 바이너리 데이터를 응답으로 전송
     */
    @GetMapping("/{imageId}")
    public ResponseEntity<byte[]> getImage(@PathVariable Long imageId) {
        try {
            // 이미지 정보 조회
            BoardImage image = boardService.getBoardImageInfo(imageId);

            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();

            // Content-Type 설정 (이미지 MIME 타입)
            headers.setContentType(MediaType.parseMediaType(image.getBoardImageType()));

            // 이미지 바이너리 데이터와 함께 응답 반환
            return new ResponseEntity<>(image.getBoardImageData(), headers, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            logger.warn("이미지 조회 실패: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("이미지 조회 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 이미지 파일명으로 제공하는 API
     * 기존 URL 형식을 유지하기 위한 추가 엔드포인트
     */
    @GetMapping("/file/{filename:.+}")
    public ResponseEntity<byte[]> getImageByFilename(@PathVariable String filename) {
        try {
            // 파일명으로 이미지 조회
            BoardImage image = boardService.getBoardImageByFilename(filename);

            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(image.getBoardImageType()));

            return new ResponseEntity<>(image.getBoardImageData(), headers, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("이미지 파일명으로 조회 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}