package pawparazzi.back.board.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import pawparazzi.back.board.dto.PostBoardRequestDto;
import pawparazzi.back.board.dto.PostBoardResponseDto;
import pawparazzi.back.board.service.BoardService;

@RestController
@RequestMapping("/api/v1/board")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    @PostMapping("")
    public ResponseEntity<? super PostBoardResponseDto> postBoard(
            @RequestBody @Valid PostBoardRequestDto requestBody,
            @RequestParam("id") Long id) {
        return boardService.postBoard(requestBody, id);
    }
}