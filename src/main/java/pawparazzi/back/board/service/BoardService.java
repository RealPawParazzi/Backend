package pawparazzi.back.board.service;

import org.springframework.http.ResponseEntity;
import pawparazzi.back.board.dto.PostBoardRequestDto;
import pawparazzi.back.board.dto.PostBoardResponseDto;

public interface BoardService {
    ResponseEntity<? super PostBoardResponseDto> postBoard(PostBoardRequestDto dto, Long id);

}
