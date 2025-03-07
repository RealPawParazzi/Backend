package pawparazzi.back.comment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class ReplyResponseDto {
    private Long replyId;
    private MemberDto replyMember;
    private String content;
    private int likeCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Getter
    @AllArgsConstructor
    public static class MemberDto {
        private Long memberId;
        private String nickname;
        private String profileImageUrl;
    }
}