package pawparazzi.back.member.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pawparazzi.back.S3.service.S3AsyncService;
import pawparazzi.back.member.dto.request.LoginRequestDto;
import pawparazzi.back.member.dto.request.SignUpRequestDto;
import pawparazzi.back.member.dto.request.UpdateMemberRequestDto;
import pawparazzi.back.member.dto.response.MemberResponseDto;
import pawparazzi.back.member.dto.response.UpdateMemberResponseDto;
import pawparazzi.back.member.entity.Member;
import pawparazzi.back.member.service.MemberService;
import pawparazzi.back.security.util.JwtUtil;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class MemberController {

    private final JwtUtil jwtUtil;
    private final MemberService memberService;
    private final S3AsyncService s3AsyncService;
    private final ObjectMapper objectMapper;


    /**
     * 회원 가입
     */
    @PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> registerUser(
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage,
            @RequestPart("userData") String userDataJson) {

        // JSON 데이터를 DTO로 변환
        ObjectMapper objectMapper = new ObjectMapper();
        SignUpRequestDto request;
        try {
            request = objectMapper.readValue(userDataJson, SignUpRequestDto.class);
        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().body("Invalid JSON format");
        }

        // 회원가입 처리
        memberService.registerUser(request, profileImage);
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
     * 사용자 정보 수정
     */
    @PatchMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UpdateMemberResponseDto> updateMember(
            @RequestHeader("Authorization") String token,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage,
            @RequestPart(value = "userData", required = false) String userDataJson) {

        token = token.replace("Bearer ", "");
        Long memberId = jwtUtil.extractMemberId(token);

        try {
            UpdateMemberRequestDto request = (userDataJson == null || userDataJson.isBlank())
                    ? new UpdateMemberRequestDto()
                    : objectMapper.readValue(userDataJson, UpdateMemberRequestDto.class);

            UpdateMemberResponseDto updatedMember = memberService.updateMember(memberId, request, profileImage);
            return ResponseEntity.ok(updatedMember);
        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().body(null);
        }
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


    /**
     * 전체 회원 목록 조회 API
     */
    @GetMapping
    public ResponseEntity<List<MemberResponseDto>> getAllMembers() {
        List<MemberResponseDto> members = memberService.getAllMembers();
        return ResponseEntity.ok(members);
    }
}