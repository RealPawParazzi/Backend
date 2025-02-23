package pawparazzi.back.board.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PostBoardRequestDto {

    @NotBlank
    private String title;

    private String content;
    @NotNull
    private List<String> boardImageList = List.of(); // 기본값을 빈 리스트로 설정
    //private List<String> boardVideoList = List.of(); // 기본값을 빈 리스트로 설정
}
