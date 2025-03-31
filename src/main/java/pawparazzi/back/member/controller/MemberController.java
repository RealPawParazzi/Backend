package pawparazzi.back.member.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pawparazzi.back.member.dto.request.LoginRequestDto;
import pawparazzi.back.member.dto.response.MemberResponseDto;
import pawparazzi.back.member.dto.response.UpdateMemberResponseDto;
import pawparazzi.back.member.entity.Member;
import pawparazzi.back.member.service.MemberService;
import pawparazzi.back.security.user.CustomUserDetails;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    /**
     * 회원 가입
     */
    @PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CompletableFuture<ResponseEntity<String>> registerUser(
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage,
            @RequestPart("userData") String userDataJson) {

        // 비동기 회원가입 처리 후 응답 반환
        return memberService.registerUser(userDataJson, profileImage)
                .thenApply(unused -> ResponseEntity.ok("회원가입 성공"))
                .exceptionally(ex -> ResponseEntity.badRequest().body("Invalid JSON format"));
    }

    /**
     * 로그인
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginRequestDto request) {
        Map<String, String> tokenMap = memberService.login(request);
        return ResponseEntity.ok(tokenMap);
    }

    /**
     * 사용자 정보 조회
     */
    @GetMapping("/me")
    public ResponseEntity<Member> getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long memberId = userDetails.getId();
        Member member = memberService.findById(memberId);
        return ResponseEntity.ok(member);
    }

    /**
     * 사용자 정보 수정
     */
    @PatchMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CompletableFuture<ResponseEntity<UpdateMemberResponseDto>> updateMember(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage,
            @RequestPart(value = "userData", required = false) String userDataJson) {

        Long memberId = userDetails.getId();

        return memberService.updateMember(memberId, userDataJson, profileImage)
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> ResponseEntity.badRequest().body(null));
    }

    /**
     * 회원 탈퇴
     */
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteMember(@AuthenticationPrincipal CustomUserDetails userDetails) {
        memberService.deleteMember(userDetails.getId());
        return ResponseEntity.ok("회원 탈퇴 완료");
    }

    /**
     * 전체 회원 목록 조회 API
     */
    @GetMapping
    public ResponseEntity<List<MemberResponseDto>> getAllMembers() {
        List<MemberResponseDto> members = memberService.getAllMembers();
        return ResponseEntity.ok(members);
    }

    /**
     * 로그아웃
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@AuthenticationPrincipal CustomUserDetails userDetails,
                                         @RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        memberService.logout(userDetails.getId(), refreshToken);
        return ResponseEntity.ok("로그아웃 완료");
    }

    @PostMapping("/reissue")
    public ResponseEntity<Map<String, String>> reissue(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        Map<String, String> tokenMap = memberService.reissueAccessToken(refreshToken);
        return ResponseEntity.ok(tokenMap);
    }
}