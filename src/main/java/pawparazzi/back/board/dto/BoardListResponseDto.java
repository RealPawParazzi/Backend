package pawparazzi.back.board.dto;

import lombok.*;
import pawparazzi.back.board.entity.BoardVisibility;

import java.time.LocalDateTime;

@Getter
@Setter
// 게시물 전체 목록 조회
public class BoardListResponseDto {

    private Long id;
    private String title;
    private BoardVisibility visibility;
    private String titleImage;
    private String titleContent;
    private String tag;
    private LocalDateTime writeDatetime;
    private int favoriteCount;
    private int commentCount;
    private int viewCount;

    private AuthorDto author;

    @Getter
    @Setter
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthorDto {
        private Long id;
        private String nickname;
        private String profileImageUrl;
    }
}