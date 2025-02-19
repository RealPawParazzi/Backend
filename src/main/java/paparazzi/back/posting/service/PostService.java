package paparazzi.back.posting.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import paparazzi.back.posting.dto.PostRequestDTO;
import paparazzi.back.posting.dto.PostResponseDTO;
//import paparazzi.back.posting.dto.UserDTO;
import paparazzi.back.posting.entity.Posting;
import paparazzi.back.posting.entity.Visibility;
import paparazzi.back.posting.repository.PostRepository;
//import paparazzi.back.user.entity.User;
//import paparazzi.back.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    //private final UserRepository userRepository;

    /**
     *게시물 등록
     */
    @Transactional
    public PostResponseDTO createPost(PostRequestDTO requestDTO) {
        /*
        //사용자 확인
        User user = userRepository.findById(requestDTO.userId())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 사용자 ID입니다."));*/

        //게시물 생성
        Posting post = new Posting();
        //post.setUser(user);
        post.setCaption(requestDTO.caption());
        post.setImageUrl(requestDTO.imageUrl());
        post.setVisibility(requestDTO.visibility());
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(null);  // 처음 등록 시 null

        //DB 저장
        Posting savedPost = postRepository.save(post);

        //DTO 변환 후 반환
        return convertToDto(savedPost);
    }

    /**
     * 전체 게시물 조회
     */
    @Transactional(readOnly = true)
    public List<PostResponseDTO> getAllPosts(String sort, String visibility) {
        Sort sortDirection = getSortDirection(sort);

        // 유효한 Visibility 값인지 확인
        Visibility visibilityEnum;
        try {
            visibilityEnum = Visibility.valueOf(visibility.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 visibility 값입니다. (PUBLIC 또는 PRIVATE)");
        }

        // 게시물 조회
        List<Posting> posts = postRepository.findByVisibility(visibilityEnum, sortDirection);

        // DTO 변환 후 반환
        return posts.stream().map(this::convertToDto).toList();
    }

    /**
     * 정렬 기준을 결정하는 메서드
     */
    private Sort getSortDirection(String sort) {
        return switch (sort.toLowerCase()) {
            case "latest" -> Sort.by(Sort.Direction.DESC, "createdAt");
            case "oldest" -> Sort.by(Sort.Direction.ASC, "createdAt");
            default -> Sort.by(Sort.Direction.DESC, "id");
        };
    }

    /**
     * 게시물 엔티티를 DTO로 변환 (조회용)
     */
    private PostResponseDTO convertToDto(Posting post) {
        return new PostResponseDTO(
                post.getId(),
                //new UserDTO(post.getUser().getId(), post.getUser().getUsername(), post.getUser().getProfileImage()),
                post.getCaption(),
                post.getImageUrl(),
                post.getVisibility(),
                post.getCreatedAt(),
                post.getUpdatedAt() != null ? post.getUpdatedAt() : post.getCreatedAt() // 업데이트 시간 없으면 생성시간 반환
                //post.getLikes().size(), 좋아요 기능 추가 후 주석 삭제
                //post.getComments().size() // 댓글 기능 출가 후 주석 삭제
        );
    }

    @Transactional
    public void deletePost(Long postId) {
        // 게시물 존재 여부 확인
        Posting post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시물을 찾을 수 없습니다."));

        /*
        // 현재 로그인한 사용자가 게시물 작성자인지 확인
        if (!post.getUser().getId().equals(userId)) {
            throw new IllegalStateException("본인의 게시물만 삭제할 수 있습니다.");
        }*/

        //게시물 삭제
        postRepository.delete(post);
    }
}