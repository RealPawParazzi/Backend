package pawparazzi.back.member.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pawparazzi.back.member.dto.request.LoginRequestDto;
import pawparazzi.back.member.dto.request.SignUpRequestDto;
import pawparazzi.back.member.dto.request.UpdateMemberRequestDto;
import pawparazzi.back.member.dto.response.UpdateMemberResponseDto;
import pawparazzi.back.member.entity.Member;
import pawparazzi.back.member.service.MemberService;
import pawparazzi.back.security.util.JwtUtil;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class MemberController {

    private final JwtUtil jwtUtil;
    private final MemberService memberService;

    /**
     * 회원 가입
     */
    @PostMapping("/signup")
    public ResponseEntity<String> registerUser(@Valid @RequestBody SignUpRequestDto request) {
        memberService.registerUser(request);
        return ResponseEntity.ok("회원가입 성공");
    }

    /**
     * 로그인
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginRequestDto request) {
        String token = memberService.login(request);
        return ResponseEntity.ok(Map.of("token", token));
    }

    /**
     * 사용자 정보 조회
     */
    @GetMapping("/me")
    public ResponseEntity<Member> getCurrentUser(@RequestHeader("Authorization") String token) {
        token = token.replace("Bearer ", "");
        Long memberId = jwtUtil.extractMemberId(token);
        Member member = memberService.findById(memberId);
        return ResponseEntity.ok(member);
    }

    /**
     * 사용자 정보 수정 (게시물 정보 제외)
     */
    @PatchMapping("/me")
    public ResponseEntity<UpdateMemberResponseDto> updateMember(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody UpdateMemberRequestDto request) {

        token = token.replace("Bearer ", "");
        Long memberId = jwtUtil.extractMemberId(token);

        UpdateMemberResponseDto updatedMember = memberService.updateMember(memberId, request);
        return ResponseEntity.ok(updatedMember);
    }
    /**
     * 회원 탙퇴
     */
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteMember(@RequestHeader("Authorization") String token) {
        token = token.replace("Bearer ", "");
        Long memberId = jwtUtil.extractMemberId(token);
        memberService.deleteMember(memberId);
        return ResponseEntity.ok("회원 탈퇴 완료");
    }
}