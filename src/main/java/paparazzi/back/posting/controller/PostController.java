package paparazzi.back.posting.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import paparazzi.back.posting.dto.PostRequestDTO;
import paparazzi.back.posting.dto.PostResponseDTO;
import paparazzi.back.posting.service.PostService;
import java.util.List;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    //게시물 등록
    @PostMapping("/add")
    public ResponseEntity<PostResponseDTO> addPost(@RequestBody @Valid PostRequestDTO requestDTO) {
        PostResponseDTO response = postService.createPost(requestDTO);
        return ResponseEntity.status(201).body(response);
    }
    //전체 게시물 조회
    @GetMapping
    public ResponseEntity<List<PostResponseDTO>> getAllPosts(
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(defaultValue = "PUBLIC") String visibility) {

        List<PostResponseDTO> response = postService.getAllPosts(sort, visibility);
        return ResponseEntity.ok(response);
    }

    // 게시물 삭제
    @DeleteMapping("/{postId}")
    public void deletePost(@PathVariable Long postId) {
        postService.deletePost(postId);
    }
  }

