package pawparazzi.back.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class CommentResponseDto {
    private Long commentId;
    private MemberDto commentMember;
    private String content;
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