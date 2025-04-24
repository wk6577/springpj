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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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
    private static final String UPLOAD_DIR = "./src/main/resources/static/uploads/";

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
     * 게시물 작성
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

        // 태그 처리
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            try {
                // JSON 문자열에서 태그 배열 추출
                List<String> tagList = tagService.parseTagsFromJson(request.getTags());
                tagService.saveBoardTags(savedBoard, tagList);
            } catch (Exception e) {
                logger.error("태그 처리 중 오류 발생: {}", e.getMessage(), e);
            }
        }

        // 이미지 처리
        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            try {
                // 파일 저장
                String fileName = UUID.randomUUID().toString() + "_" + image.getOriginalFilename();
                Path targetPath = Paths.get(UPLOAD_DIR);

                // 디렉토리가 없으면 생성
                if (!Files.exists(targetPath)) {
                    Files.createDirectories(targetPath);
                }

                Path filePath = targetPath.resolve(fileName);
                Files.copy(image.getInputStream(), filePath);

                // 게시물 이미지 엔티티 생성 및 저장
                BoardImage boardImage = BoardImage.builder()
                        .board(savedBoard)
                        .boardImageName(fileName)
                        .boardImagePath("/uploads/" + fileName)
                        .boardImageOrder(0)
                        .build();

                BoardImage savedImage = boardImageRepository.save(boardImage);
                imageUrl = savedImage.getBoardImagePath();
            } catch (IOException e) {
                logger.error("이미지 저장 중 오류 발생: {}", e.getMessage(), e);
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