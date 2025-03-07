package pawparazzi.back.follow.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pawparazzi.back.follow.dto.FollowResponseDto;
import pawparazzi.back.follow.dto.FollowerResponseDto;
import pawparazzi.back.follow.dto.FollowingResponseDto;
import pawparazzi.back.follow.service.FollowService;

import java.util.List;

@RestController
@RequestMapping("/api/follow")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    @PostMapping("/{targetNickName}")
    public ResponseEntity<FollowResponseDto> follow(
            @PathVariable String targetNickName,
            @RequestHeader("Authorization") String token){
        FollowResponseDto response = followService.follow(targetNickName, token);
        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/{targetNickName}")
    public ResponseEntity<String> unfollow(
            @PathVariable String targetNickName,
            @RequestHeader("Authorization") String token){
        followService.unfollow(targetNickName, token);
        return ResponseEntity.ok("팔로우 취소되었습니다.");
    }

    @GetMapping("/followers/{nickName}")
    public ResponseEntity<List<FollowerResponseDto>> getFollowers(@PathVariable String nickName){
        List<FollowerResponseDto> followers = followService.getFollowers(nickName);
        return ResponseEntity.ok(followers);
    }

    @GetMapping("/following/{nickName}")
    public ResponseEntity<List<FollowingResponseDto>> getFollowing(@PathVariable String nickName){
        List<FollowingResponseDto> followings = followService.getFollowing(nickName);
        return ResponseEntity.ok(followings);
    }
}
