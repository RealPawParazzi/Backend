package pawparazzi.back.board.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pawparazzi.back.board.dto.BoardListResponseDto;
import pawparazzi.back.board.dto.BoardDetailDto;
import pawparazzi.back.board.service.BoardService;
import pawparazzi.back.security.user.CustomUserDetails;

import java.util.List;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    /**
     * 게시물 등록
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BoardDetailDto> createBoard(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestPart("userData") String userDataJson,
            @RequestPart(value = "mediaFiles", required = false) List<MultipartFile> mediaFiles,
            @RequestPart(value = "titleImage", required = false) MultipartFile titleImageFile,
            @RequestPart(value = "titleContent", required = false) String titleContent) {

        Long memberId = userDetails.getId();

        BoardDetailDto response = boardService.createBoard(userDataJson, memberId, titleImageFile, mediaFiles, titleContent);
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
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestPart("userData") String userDataJson,
            @RequestPart(value = "mediaFiles", required = false) List<MultipartFile> mediaFiles,
            @RequestPart(value = "titleImage", required = false) MultipartFile titleImageFile,
            @RequestPart(value = "titleContent", required = false) String titleContent) {

        Long memberId = userDetails.getId();

        BoardDetailDto updatedBoard = boardService.updateBoard(boardId, memberId, userDataJson, mediaFiles, titleImageFile, titleContent).join();
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
    public ResponseEntity<Void> deleteBoard(@PathVariable Long boardId, @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getId();

        boardService.deleteBoard(boardId, userId).join();
        return ResponseEntity.noContent().build();
    }
}