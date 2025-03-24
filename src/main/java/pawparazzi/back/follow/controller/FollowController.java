package pawparazzi.back.follow.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pawparazzi.back.follow.dto.FollowResponseDto;
import pawparazzi.back.follow.dto.FollowerResponseDto;
import pawparazzi.back.follow.dto.FollowingResponseDto;
import pawparazzi.back.follow.dto.UnfollowResponseDto;
import pawparazzi.back.follow.service.FollowService;

import java.util.List;

@RestController
@RequestMapping("/api/follow")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    @PostMapping("/{targetId}")
    public ResponseEntity<FollowResponseDto> follow(
            @PathVariable Long targetId,
            @RequestHeader("Authorization") String token){
        FollowResponseDto response = followService.follow(targetId, token);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{targetId}")
    public ResponseEntity<UnfollowResponseDto> unfollow(
            @PathVariable Long targetId,
            @RequestHeader("Authorization") String token){
        UnfollowResponseDto response = followService.unfollow(targetId, token);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/followers/{targetId}")
    public ResponseEntity<List<FollowerResponseDto>> getFollowers(@PathVariable Long targetId){
        List<FollowerResponseDto> followers = followService.getFollowers(targetId);
        return ResponseEntity.ok(followers);
    }

    @GetMapping("/following/{targetId}")
    public ResponseEntity<List<FollowingResponseDto>> getFollowing(@PathVariable Long targetId){
        List<FollowingResponseDto> followings = followService.getFollowing(targetId);
        return ResponseEntity.ok(followings);
    }
}
