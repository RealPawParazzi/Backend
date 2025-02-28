package pawparazzi.back.comment.dto.response;

import lombok.Getter;


@Getter
public class CommentLikeResponseDto {
    private final boolean liked;
    private final int commentsLikeCount;

    public CommentLikeResponseDto(boolean liked, int commentsLikeCount) {
        this.liked = liked;
        this.commentsLikeCount = commentsLikeCount;
    }
}