package com.milestone.service;

import com.milestone.dto.BoardRequest;
import com.milestone.dto.BoardResponse;
import com.milestone.entity.Board;
import com.milestone.entity.BoardImage;
import com.milestone.entity.Member;
import com.milestone.repository.BoardImageRepository;
import com.milestone.repository.BoardRepository;
import com.milestone.repository.LikeRepository;
import com.milestone.repository.MemberRepository;
import com.milestone.repository.ReplyRepository;
import com.milestone.repository.ScrapRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardService {

    private static final Logger logger = LoggerFactory.getLogger(BoardService.class);
    private final BoardRepository boardRepository;
    private final BoardImageRepository boardImageRepository;
    private final MemberRepository memberRepository;
    private final LikeRepository likeRepository;
    private final ScrapRepository scrapRepository;
    private final ReplyRepository replyRepository;
    private final TagService tagService;
    private static final String SESSION_KEY = "LOGGED_IN_MEMBER";

    /**
     * 모든 게시물 조회
     */
    @Transactional(readOnly = true)
    public List<BoardResponse> getAllBoards() {
        List<Board> boards = boardRepository.findAllByOrderByBoardInputdateDesc();
        return boards.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 게시물 ID로 게시물 조회
     */
    @Transactional
    public BoardResponse getBoardById(Long boardNo, HttpSession session) {
        Board board = boardRepository.findById(boardNo)
                .orElseThrow(() -> new IllegalArgumentException("게시물을 찾을 수 없습니다: " + boardNo));

        // 조회수 증가
        board.setBoardReadhit(board.getBoardReadhit() + 1);
        boardRepository.save(board);

        Long currentMemberNo = (Long) session.getAttribute(SESSION_KEY);
        boolean isLiked = false;
        boolean isScraped = false;

        if (currentMemberNo != null) {
            isLiked = likeRepository.existsByMemberMemberNoAndLikeTypeAndLikeTypeNo(
                    currentMemberNo, "board", boardNo);
            isScraped = scrapRepository.existsByMemberMemberNoAndBoardBoardNo(
                    currentMemberNo, boardNo);
        }

        // 태그 목록 조회
        List<String> tags = tagService.getBoardTags(boardNo);

        // 대표 이미지 URL 가져오기
        String imageUrl = null;
        List<BoardImage> images = boardImageRepository.findByBoardBoardNoOrderByBoardImageOrderAsc(boardNo);
        if (!images.isEmpty()) {
            imageUrl = images.get(0).getBoardImagePath();
        }

        // 댓글 수 조회
        long replyCount = replyRepository.countByBoardBoardNoAndReplyStatus(boardNo, "active");

        return BoardResponse.fromEntity(board, tags, isLiked, isScraped, imageUrl, replyCount);
    }

    /**
     * ID 목록으로 게시물 조회
     */
    @Transactional(readOnly = true)
    public List<BoardResponse> getBoardsByIds(List<Long> boardIds, HttpSession session) {
        if (boardIds == null || boardIds.isEmpty()) {
            return new ArrayList<>();
        }

        Long currentMemberNo = (Long) session.getAttribute(SESSION_KEY);

        List<Board> boards = boardRepository.findByBoardNoIn(boardIds);

        // 결과 목록을 원래 ID 순서대로 정렬
        Map<Long, Board> boardMap = boards.stream()
                .collect(Collectors.toMap(Board::getBoardNo, board -> board));

        List<BoardResponse> result = new ArrayList<>();

        for (Long boardId : boardIds) {
            Board board = boardMap.get(boardId);
            if (board != null) {
                boolean isLiked = false;
                boolean isScraped = false;

                if (currentMemberNo != null) {
                    isLiked = likeRepository.existsByMemberMemberNoAndLikeTypeAndLikeTypeNo(
                            currentMemberNo, "board", boardId);
                    isScraped = scrapRepository.existsByMemberMemberNoAndBoardBoardNo(
                            currentMemberNo, boardId);
                }

                String imageUrl = null;
                List<BoardImage> images = boardImageRepository.findByBoardBoardNoOrderByBoardImageOrderAsc(boardId);
                if (!images.isEmpty()) {
                    imageUrl = images.get(0).getBoardImagePath();
                }

                List<String> tags = tagService.getBoardTags(boardId);
                long replyCount = replyRepository.countByBoardBoardNoAndReplyStatus(boardId, "active");

                result.add(BoardResponse.fromEntity(board, tags, isLiked, isScraped, imageUrl, replyCount));
            }
        }

        return result;
    }

    /**
     * 게시물 타입별 조회
     */
    @Transactional(readOnly = true)
    public List<BoardResponse> getBoardsByType(String boardType) {
        List<Board> boards = boardRepository.findByBoardTypeOrderByBoardInputdateDesc(boardType);
        return boards.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 회원별 게시물 조회
     */
    @Transactional(readOnly = true)
    public List<BoardResponse> getBoardsByMember(Long memberNo) {
        List<Board> boards = boardRepository.findByMemberMemberNoOrderByBoardInputdateDesc(memberNo);
        return boards.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 게시물 ID로 첫 번째 이미지 가져오기
     */
    @Transactional(readOnly = true)
    public BoardImage getBoardFirstImage(Long boardNo) {
        List<BoardImage> images = boardImageRepository.findByBoardBoardNoOrderByBoardImageOrderAsc(boardNo);
        if (images.isEmpty()) {
            return null;
        }
        return images.get(0);
    }

    /**
     * 게시물 작성 - 이미지를 DB에 저장하는 방식으로 변경
     */
    @Transactional
    public BoardResponse createBoard(BoardRequest request, MultipartFile image, HttpSession session) {
        Long memberNo = (Long) session.getAttribute(SESSION_KEY);
        if (memberNo == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }

        Member member = memberRepository.findById(memberNo)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        // 게시물 엔티티 생성
        Board board = Board.builder()
                .member(member)
                .boardType(request.getBoardType())
                .boardTitle(request.getBoardTitle())
                .boardContent(request.getBoardContent())
                .boardVisible(request.getBoardVisible())
                .boardLike(0L)
                .boardScrap(0L)
                .boardReadhit(0L)
                .build();

        // 게시물 저장
        Board savedBoard = boardRepository.save(board);
        logger.info("게시물 저장 성공 - ID: {}", savedBoard.getBoardNo());

        // 태그 처리
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            try {
                // JSON 문자열에서 태그 배열 추출
                List<String> tagList = tagService.parseTagsFromJson(request.getTags());
                tagService.saveBoardTags(savedBoard, tagList);
                logger.info("태그 저장 성공: {}", tagList);
            } catch (Exception e) {
                logger.error("태그 처리 중 오류 발생: {}", e.getMessage(), e);
            }
        }

        // 이미지 처리 - MySQL에 직접 저장하는 방식으로 변경
        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            try {
                // 이미지 파일명 생성
                String originalFilename = image.getOriginalFilename();
                String fileExtension = "";
                if (originalFilename != null && originalFilename.contains(".")) {
                    fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }
                String fileName = "board_" + savedBoard.getBoardNo() + "_" + UUID.randomUUID().toString() + fileExtension;

                // 이미지 바이너리 데이터 및 MIME 타입 얻기
                byte[] imageData = image.getBytes();
                String contentType = image.getContentType();

                // API 경로 생성 (이미지 접근 URL)
                String apiPath = "/api/images/" + savedBoard.getBoardNo();

                // 게시물 이미지 엔티티 생성 및 저장
                BoardImage boardImage = BoardImage.builder()
                        .board(savedBoard)
                        .boardImageName(fileName)
                        .boardImagePath(apiPath) // API 접근 경로로 설정
                        .boardImageData(imageData) // 바이너리 데이터 저장
                        .boardImageType(contentType != null ? contentType : "image/jpeg") // MIME 타입 저장
                        .boardImageOrder(0)
                        .build();

                BoardImage savedImage = boardImageRepository.save(boardImage);
                imageUrl = savedImage.getBoardImagePath();

                logger.info("이미지 저장 성공: {}", fileName);
            } catch (IOException e) {
                logger.error("이미지 처리 중 오류 발생: {}", e.getMessage(), e);
                throw new RuntimeException("이미지 저장 중 오류가 발생했습니다.", e);
            }
        }

        // 응답 DTO 생성
        return BoardResponse.builder()
                .boardNo(savedBoard.getBoardNo())
                .memberNo(member.getMemberNo())
                .memberName(member.getMemberName())
                .memberNickname(member.getMemberNickname())
                .memberPhoto(member.getMemberPhoto())
                .boardType(savedBoard.getBoardType())
                .boardCategory(savedBoard.getBoardCategory())
                .boardTitle(savedBoard.getBoardTitle())
                .boardContent(savedBoard.getBoardContent())
                .boardLike(savedBoard.getBoardLike())
                .boardScrap(savedBoard.getBoardScrap())
                .boardReadhit(savedBoard.getBoardReadhit())
                .boardVisible(savedBoard.getBoardVisible())
                .boardInputdate(savedBoard.getBoardInputdate())
                .boardImage(imageUrl)
                .tags(tagService.getBoardTags(savedBoard.getBoardNo()))
                .isLiked(false)
                .isScraped(false)
                .replyCount(0L)
                .build();
    }

    /**
     * 게시물 수정
     */
    @Transactional
    public BoardResponse updateBoard(Long boardNo, BoardRequest request, HttpSession session) {
        Long memberNo = (Long) session.getAttribute(SESSION_KEY);
        if (memberNo == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }

        Board board = boardRepository.findById(boardNo)
                .orElseThrow(() -> new IllegalArgumentException("게시물을 찾을 수 없습니다: " + boardNo));

        // 게시물 작성자 확인
        if (!board.getMember().getMemberNo().equals(memberNo)) {
            throw new IllegalArgumentException("게시물을 수정할 권한이 없습니다.");
        }

        // 게시물 정보 업데이트
        board.setBoardContent(request.getBoardContent());
        if (request.getBoardTitle() != null) {
            board.setBoardTitle(request.getBoardTitle());
        }
        if (request.getBoardVisible() != null) {
            board.setBoardVisible(request.getBoardVisible());
        }

        Board updatedBoard = boardRepository.save(board);

        // 태그 처리
        if (request.getTags() != null) {
            try {
                List<String> tagList = tagService.parseTagsFromJson(request.getTags());
                tagService.updateBoardTags(updatedBoard, tagList);
            } catch (Exception e) {
                logger.error("태그 업데이트 중 오류 발생: {}", e.getMessage(), e);
            }
        }

        return convertToDto(updatedBoard);
    }

    /**
     * 게시물 삭제
     */
    @Transactional
    public void deleteBoard(Long boardNo, HttpSession session) {
        Long memberNo = (Long) session.getAttribute(SESSION_KEY);
        if (memberNo == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }

        Board board = boardRepository.findById(boardNo)
                .orElseThrow(() -> new IllegalArgumentException("게시물을 찾을 수 없습니다: " + boardNo));

        // 게시물 작성자 확인
        if (!board.getMember().getMemberNo().equals(memberNo)) {
            throw new IllegalArgumentException("게시물을 삭제할 권한이 없습니다.");
        }

        // 연관된 태그 삭제
        tagService.deleteBoardTags(board);

        // 게시물 삭제 (외래 키 제약조건에 따라 관련 이미지, 좋아요, 스크랩 등이 자동 삭제됨)
        boardRepository.delete(board);
    }

    /**
     * 이미지 ID로 이미지 정보 조회
     */
    @Transactional(readOnly = true)
    public BoardImage getBoardImageInfo(Long imageId) {
        return boardImageRepository.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException("이미지를 찾을 수 없습니다: " + imageId));
    }

    /**
     * 파일명으로 이미지 정보 조회
     */
    @Transactional(readOnly = true)
    public BoardImage getBoardImageByFilename(String filename) {
        Optional<BoardImage> imageOptional = boardImageRepository.findByBoardImageName(filename);

        if (imageOptional.isPresent()) {
            return imageOptional.get();
        } else {
            // 기본 이미지 반환 또는 대체 처리
            logger.warn("이미지를 찾을 수 없음: {}", filename);
            return createDefaultImage();
        }
    }

    private BoardImage createDefaultImage() {
        // 기본 이미지 데이터 생성 (예: 프로필 기본 이미지)
        try {
            // 클래스패스에서 기본 이미지 로드
            Resource resource = new ClassPathResource("/static/icon/profileimage.png");
            byte[] imageData = Files.readAllBytes(resource.getFile().toPath());

            // 기본 이미지 정보로 BoardImage 객체 생성
            BoardImage defaultImage = new BoardImage();
            defaultImage.setBoardImageName("default_image.png");
            defaultImage.setBoardImageData(imageData);
            defaultImage.setBoardImageType("image/png");
            defaultImage.setBoardImagePath("/icon/profileimage.png");
            defaultImage.setBoardImageOrder(0);

            return defaultImage;
        } catch (IOException e) {
            logger.error("기본 이미지 로드 중 오류: {}", e.getMessage(), e);
            // 최소한의 빈 이미지 데이터 반환
            BoardImage emptyImage = new BoardImage();
            emptyImage.setBoardImageName("empty.png");
            emptyImage.setBoardImageData(new byte[0]);
            emptyImage.setBoardImageType("image/png");
            emptyImage.setBoardImagePath("/icon/profileimage.png");
            emptyImage.setBoardImageOrder(0);
            return emptyImage;
        }
    }

    /**
     * 게시물 엔티티를 DTO로 변환
     */
    private BoardResponse convertToDto(Board board) {
        // 사용자의 좋아요/스크랩 상태는 현재 세션 정보가 없으므로 false로 초기화
        boolean isLiked = false;
        boolean isScraped = false;

        // 게시물 이미지 URL 가져오기
        String imageUrl = null;
        List<BoardImage> images = boardImageRepository.findByBoardBoardNoOrderByBoardImageOrderAsc(board.getBoardNo());
        if (!images.isEmpty()) {
            imageUrl = images.get(0).getBoardImagePath();
        }

        // 태그 목록 가져오기
        List<String> tags = tagService.getBoardTags(board.getBoardNo());

        // 댓글 수 조회
        long replyCount = replyRepository.countByBoardBoardNoAndReplyStatus(board.getBoardNo(), "active");

        return BoardResponse.builder()
                .boardNo(board.getBoardNo())
                .memberNo(board.getMember().getMemberNo())
                .memberName(board.getMember().getMemberName())
                .memberNickname(board.getMember().getMemberNickname())
                .memberPhoto(board.getMember().getMemberPhoto())
                .boardType(board.getBoardType())
                .boardCategory(board.getBoardCategory())
                .boardTitle(board.getBoardTitle())
                .boardContent(board.getBoardContent())
                .boardLike(board.getBoardLike())
                .boardScrap(board.getBoardScrap())
                .boardReadhit(board.getBoardReadhit())
                .boardVisible(board.getBoardVisible())
                .boardInputdate(board.getBoardInputdate())
                .boardImage(imageUrl)
                .tags(tags)
                .isLiked(isLiked)
                .isScraped(isScraped)
                .replyCount(replyCount)
                .build();
    }
}