package com.milestone.util;

import com.milestone.entity.BoardImage;
import com.milestone.repository.BoardImageRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * 이미지 데이터 마이그레이션 유틸리티
 * 기존 파일 시스템의 이미지를 데이터베이스로 마이그레이션
 *
 * 주의: 이 클래스는 마이그레이션 일회성 작업 후 삭제하거나 비활성화해야 함
 */
@Component
@RequiredArgsConstructor
public class ImageMigrationUtility implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(ImageMigrationUtility.class);
    private final BoardImageRepository boardImageRepository;

    // 기존 이미지 파일이 저장된 경로
    private static final String UPLOAD_DIR = "./src/main/resources/static/uploads/";

    // 마이그레이션 활성화 여부 (일회성 작업 후 false로 설정)
    private static final boolean MIGRATION_ENABLED = true;

    @Override
    @Transactional
    public void run(String... args) {
        if (!MIGRATION_ENABLED) {
            logger.info("이미지 마이그레이션이 비활성화되어 있습니다.");
            return;
        }

        logger.info("이미지 마이그레이션 시작...");

        try {
            // 모든 이미지 레코드 조회
            List<BoardImage> images = boardImageRepository.findAll();
            logger.info("마이그레이션할 이미지 수: {}", images.size());

            int successCount = 0;
            int failCount = 0;

            for (BoardImage image : images) {
                try {
                    // 이미지 경로에서 파일명 추출
                    String imagePath = image.getBoardImagePath();
                    if (imagePath == null || !imagePath.startsWith("/uploads/")) {
                        logger.warn("잘못된 이미지 경로: {}, 이미지 ID: {}", imagePath, image.getBoardImageNo());
                        failCount++;
                        continue;
                    }

                    String fileName = imagePath.substring("/uploads/".length());
                    Path filePath = Paths.get(UPLOAD_DIR, fileName);

                    // 파일이 존재하는지 확인
                    if (!Files.exists(filePath)) {
                        logger.warn("파일을 찾을 수 없음: {}, 이미지 ID: {}", filePath, image.getBoardImageNo());
                        failCount++;
                        continue;
                    }

                    // 파일 데이터 읽기
                    byte[] imageData = Files.readAllBytes(filePath);

                    // MIME 타입 결정
                    String contentType = determineContentType(fileName);

                    // DB에 저장
                    image.setBoardImageData(imageData);
                    image.setBoardImageType(contentType);

                    // 이미지 경로 업데이트 (API 경로로)
                    image.setBoardImagePath("/api/images/file/" + fileName);

                    boardImageRepository.save(image);

                    successCount++;
                    logger.info("이미지 마이그레이션 성공: ID={}, 파일={}", image.getBoardImageNo(), fileName);

                } catch (Exception e) {
                    failCount++;
                    logger.error("이미지 마이그레이션 실패: ID={}, 오류={}", image.getBoardImageNo(), e.getMessage(), e);
                }
            }

            logger.info("이미지 마이그레이션 완료: 성공={}, 실패={}", successCount, failCount);

        } catch (Exception e) {
            logger.error("이미지 마이그레이션 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    /**
     * 파일 확장자를 기반으로 MIME 타입 결정
     */
    private String determineContentType(String fileName) {
        String lowerFileName = fileName.toLowerCase();

        if (lowerFileName.endsWith(".jpg") || lowerFileName.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowerFileName.endsWith(".png")) {
            return "image/png";
        } else if (lowerFileName.endsWith(".gif")) {
            return "image/gif";
        } else if (lowerFileName.endsWith(".webp")) {
            return "image/webp";
        } else if (lowerFileName.endsWith(".svg")) {
            return "image/svg+xml";
        } else if (lowerFileName.endsWith(".bmp")) {
            return "image/bmp";
        }

        // 기본값
        return "application/octet-stream";
    }
}