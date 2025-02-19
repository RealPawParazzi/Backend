package paparazzi.back.posting.dto;

import paparazzi.back.posting.entity.Visibility;
import java.time.LocalDateTime;

public record PostResponseDTO(
        Long id,
        //UserDTO user,
        String caption,
        String imageUrl,
        Visibility visibility,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
        //Integer likesCount,
        //Integer commentsCount
) {
    //userDTO 추가시 매개변수 및 코드에 user 추가해야 함
    // 게시물 등록 시 사용할 DTO 생성자 (좋아요/댓글 개수 포함 x)
    public static PostResponseDTO fromCreate(Long id, String caption, String imageUrl, Visibility visibility, LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new PostResponseDTO(id,caption, imageUrl, visibility, createdAt, updatedAt);
    } // 좋아요 기능, 댓글 기능 추가 후 likesCount, commentsCount = null 로 반환 시키기

    // 게시물 조회 시 사용할 DTO 생성자 (좋아요/댓글 개수 포함 o)
    public static PostResponseDTO fromRead(Long id, String caption, String imageUrl, Visibility visibility, LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new PostResponseDTO(id, caption, imageUrl, visibility, createdAt, updatedAt );
    } // 좋아요 기능, 댓글 기능 추가 후 likesCount, commentsCount = 매개변수 및 코드에 추가해야 함
}