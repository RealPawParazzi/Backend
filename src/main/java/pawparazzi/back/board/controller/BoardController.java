package pawparazzi.back.board.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pawparazzi.back.board.dto.BoardCreateRequestDto;
import pawparazzi.back.board.dto.BoardListResponseDto;
import pawparazzi.back.board.dto.BoardDetailDto;
import pawparazzi.back.board.dto.BoardUpdateRequestDto;
import pawparazzi.back.board.service.BoardService;
import pawparazzi.back.security.util.JwtUtil;

import java.util.List;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    /**
     * 게시물 등록
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BoardDetailDto> createBoard(
            @RequestHeader("Authorization") String token,
            @RequestPart("userData") String userDataJson,
            @RequestPart(value = "mediaFiles", required = false) List<MultipartFile> mediaFiles,
            @RequestPart(value = "titleImage", required = false) MultipartFile titleImageFile,
            @RequestPart(value = "titleContent", required = false) String titleContent) {

        Long memberId;
        try {
            memberId = jwtUtil.extractMemberId(token.replace("Bearer ", ""));
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        BoardCreateRequestDto requestDto;
        try {
            requestDto = objectMapper.readValue(userDataJson, BoardCreateRequestDto.class);
            requestDto.setMediaFiles(mediaFiles);
            requestDto.setTitleContent(titleContent);
        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().body(null);
        }

        BoardDetailDto response = boardService.createBoard(requestDto, memberId, titleImageFile);
        return ResponseEntity.ok(response);
    }

    /**
     * 게시글 상세 조회
     */
    @GetMapping("/{boardId}")
    public ResponseEntity<BoardDetailDto> getBoardDetail(@PathVariable Long boardId) {
        BoardDetailDto response = boardService.getBoardDetail(boardId);
        return ResponseEntity.ok(response);
    }

    /**
     * 게시글 목록 조회
     */
    @GetMapping
    public ResponseEntity<List<BoardListResponseDto>> getBoardList() {
        List<BoardListResponseDto> response = boardService.getBoardList();
        return ResponseEntity.ok(response);
    }

    /**
     * 게시물 수정
     */
    @PutMapping(value = "/{boardId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BoardDetailDto> updateBoard(
            @PathVariable Long boardId,
            @RequestHeader("Authorization") String token,
            @RequestPart("userData") String userDataJson,
            @RequestPart(value = "mediaFiles", required = false) List<MultipartFile> mediaFiles,
            @RequestPart(value = "titleImage", required = false) MultipartFile titleImageFile,
            @RequestPart(value = "titleContent", required = false) String titleContent) {

        Long memberId;
        try {
            memberId = jwtUtil.extractMemberId(token.replace("Bearer ", ""));
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            BoardUpdateRequestDto requestDto = objectMapper.readValue(userDataJson, BoardUpdateRequestDto.class);

            requestDto.setTitleContent(titleContent);

            BoardDetailDto updatedBoard = boardService.updateBoard(boardId, memberId, requestDto, mediaFiles, titleImageFile).join();

            return ResponseEntity.ok(updatedBoard);
        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * 특정 회원의 게시물 조회
     */
    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<BoardListResponseDto>> getBoardsByMember(@PathVariable Long memberId) {
        List<BoardListResponseDto> response = boardService.getBoardsByMember(memberId);
        return ResponseEntity.ok(response);
    }

    /**
     * 게시물 삭제
     */
    @DeleteMapping("/{boardId}")
    public ResponseEntity<Void> deleteBoard(@PathVariable Long boardId, @RequestHeader("Authorization") String token) {
        Long userId;
        try {
            userId = jwtUtil.extractMemberId(token.replace("Bearer ", ""));
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        boardService.deleteBoard(boardId, userId).join();

        return ResponseEntity.noContent().build();
    }
}