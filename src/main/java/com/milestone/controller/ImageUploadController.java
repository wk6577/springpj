package com.milestone.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/images")
public class ImageUploadController {

    // 프로젝트 루트 디렉토리의 uploads 폴더를 사용
    private static final String UPLOAD_DIR = "uploads";

    @PostConstruct
    public void init() {
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR).toAbsolutePath();
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            System.out.println("Upload directory created at: " + uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory!", e);
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadEditorImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("파일이 비어 있습니다.");
        }

        try {
            // 파일 이름 처리
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = UUID.randomUUID().toString() + extension;

            // 절대 경로 생성
            Path uploadPath = Paths.get(UPLOAD_DIR).toAbsolutePath();
            Path savePath = uploadPath.resolve(filename);

            // 파일 저장
            Files.copy(file.getInputStream(), savePath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("File saved at: " + savePath);

            // 반환할 URL 경로 (상대 경로)
            String imageUrl = "/uploads/" + filename;
            System.out.println("Image URL: " + imageUrl);

            return ResponseEntity.ok(imageUrl);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("이미지 저장 중 오류 발생: " + e.getMessage());
        }
    }
}