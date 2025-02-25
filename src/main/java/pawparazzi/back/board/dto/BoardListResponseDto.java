package pawparazzi.back.board.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class BoardListResponseDto {

    private Long id;
    private String title;
    private String titleImage;
    private String titleContent;
    private LocalDateTime writeDatetime;
    private int favoriteCount;
    private int commentCount;
    private int viewCount;

    private AuthorDto author;

    @Getter
    @Setter
    public static class AuthorDto {
        private Long id;
        private String nickname;
        private String profileImageUrl;
    }
}