package pawparazzi.back.comment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

@Getter
@AllArgsConstructor
public class CommentLikesResponseDto {
    private Long commentId;
    private int likeCount;
    private List<LikedMemberDto> likedMembers;

    @Getter
    @AllArgsConstructor
    public static class LikedMemberDto {
        private Long memberId;
        private String nickname;
        private String profileImageUrl;
    }
}