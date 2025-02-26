package pawparazzi.back.likes.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class LikeResponseDto {
    private Long boardId;
    private int likesCount;
    private List<LikeMemberDto> likedMember;
}