package pawparazzi.back.backoffice.controller;

import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import pawparazzi.back.backoffice.dto.member.MemberListWrapperResponse;
import pawparazzi.back.backoffice.dto.member.MemberResponse;
import pawparazzi.back.backoffice.service.MemberBackServiceImpl;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/member")
@RequiredArgsConstructor
public class MemberBackController {

    private final MemberBackServiceImpl memberBackService;

    @GetMapping("/list")
    public MemberListWrapperResponse getAllUsers() {
        return memberBackService.getAllUsers();
    }

    @GetMapping("/{memberId}")
    public MemberResponse getUserById(@PathVariable Long memberId) {
        return memberBackService.getUserById(memberId);
    }

    @DeleteMapping("/{memberId}/delete")
    public Map<String, String> deleteUser(@PathVariable Long memberId) {
        memberBackService.deleteUser(memberId);
        return Map.of("message", "유저 탈퇴처리가 완료 되었습니다.");
    }
}
