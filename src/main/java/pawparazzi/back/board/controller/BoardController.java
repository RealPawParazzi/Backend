package pawparazzi.back.board.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pawparazzi.back.board.dto.BoardCreateRequestDto;
import pawparazzi.back.board.dto.BoardListResponseDto;
import pawparazzi.back.board.dto.BoardDetailDto;
import pawparazzi.back.board.dto.BoardUpdateRequestDto;
import pawparazzi.back.board.service.BoardService;
import pawparazzi.back.security.util.JwtUtil;

import java.util.List;

@RestController
@RequestMapping("/api/v1/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;
    private final JwtUtil jwtUtil;

    /**
     * 게시글 등록
     */
    @PostMapping
    public ResponseEntity<BoardDetailDto> createBoard(
            @RequestHeader("Authorization") String token,
            @RequestBody BoardCreateRequestDto requestDto) {

        Long memberId = jwtUtil.extractMemberId(token.replace("Bearer ", ""));
        BoardDetailDto response = boardService.createBoard(requestDto, memberId);
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
    @PutMapping("/{boardId}")
    public ResponseEntity<BoardDetailDto> updateBoard(
            @PathVariable Long boardId,
            @RequestHeader("Authorization") String token,
            @RequestBody BoardUpdateRequestDto requestDto) {

        Long memberId = jwtUtil.extractMemberId(token.replace("Bearer ", ""));
        BoardDetailDto updatedBoard = boardService.updateBoard(boardId, memberId, requestDto);
        return ResponseEntity.ok(updatedBoard);
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
    public ResponseEntity<String> deleteBoard(
            @PathVariable Long boardId,
            @RequestHeader("Authorization") String token) {

        Long memberId = jwtUtil.extractMemberId(token.replace("Bearer ", ""));
        boardService.deleteBoard(boardId, memberId);
        return ResponseEntity.ok("게시물이 삭제되었습니다.");
    }
}