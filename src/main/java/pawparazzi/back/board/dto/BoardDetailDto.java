package pawparazzi.back.board.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class BoardDetailDto {

    private Long id;
    private String title;
    private String titleImage;
    private String titleContent;
    private LocalDateTime writeDatetime;
    private int favoriteCount;
    private int commentCount;
    private int viewCount;
    private AuthorDto author;
    private List<ContentDto> contents;

    @Getter
    @Setter
    public static class ContentDto {
        private String contentData;
        private String mediaUrl;
        private String mediaType;
        private int orderIndex;
    }

    @Getter
    @Setter
    public static class AuthorDto {
        private Long id;
        private String nickname;
        private String profileImageUrl;
    }
}