package pawparazzi.back.member.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pawparazzi.back.member.dto.MemberLoginDto;
import pawparazzi.back.member.dto.MemberRegisterDto;
import pawparazzi.back.member.exception.DuplicatedMemberException;
import pawparazzi.back.member.service.MemberService;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/register")
    public ResponseEntity<?> registerMember(@RequestBody @Valid MemberRegisterDto registerDto) {
        try{
            memberService.registerUser(registerDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "회원 가입 성공"));
        } catch(DuplicatedMemberException e){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
        }
    }

    //추후 메소드 수정 필요
    @PostMapping("/login")
    public ResponseEntity<?> loginMember(@RequestBody @Valid MemberLoginDto loginDto) {
        try{
            String token = memberService.login(loginDto);
            return ResponseEntity.ok(Map.of("token", token));
        } catch(IllegalArgumentException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }

    //추후 메소드 수정 필요
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String token) {
        memberService.logout(token);
        return ResponseEntity.ok(Map.of("message", "로그아웃 성공"));
    }

    @DeleteMapping("/{memberId}")
    public ResponseEntity<?> deleteMember(@PathVariable Long memberId) {
        memberService.deleteMember(memberId);
        return ResponseEntity.ok(Map.of("message", "회원이 성공적으로 삭제되었습니다."));
    }
}
