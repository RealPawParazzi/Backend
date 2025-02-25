package pawparazzi.back.board.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pawparazzi.back.board.dto.BoardCreateRequestDto;
import pawparazzi.back.board.dto.BoardListResponseDto;
import pawparazzi.back.board.dto.BoardDetailDto;
import pawparazzi.back.board.dto.BoardUpdateRequestDto;
import pawparazzi.back.board.service.BoardService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    /**
     * 게시글 등록
     */
    @PostMapping
    public ResponseEntity<BoardDetailDto> createBoard(
            @RequestHeader("Authorization") String token,
            @RequestBody BoardCreateRequestDto requestDto) {
        BoardDetailDto response = boardService.createBoard(requestDto, token);
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

        BoardDetailDto updatedBoard = boardService.updateBoard(boardId, token, requestDto);
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
     * 게시물 삭제 API
     */
    @DeleteMapping("/{boardId}")
    public ResponseEntity<String> deleteBoard(
            @PathVariable Long boardId,
            @RequestHeader("Authorization") String token) {

        boardService.deleteBoard(boardId, token);
        return ResponseEntity.ok("게시물이 삭제되었습니다.");
    }

}