package pawparazzi.back.backoffice.controller;

import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import pawparazzi.back.backoffice.dto.member.MemberListWrapperResponse;
import pawparazzi.back.backoffice.dto.member.MemberResponse;
import pawparazzi.back.backoffice.service.MemberBackService;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/member")
@RequiredArgsConstructor
public class MemberBackController {

    private final MemberBackService memberbackservice;

    @GetMapping("/count-user")
    public Map<String, Long> countAllUsers() {
        long count = memberbackservice.countUsers();
        return Map.of("totalUserCount", count);
    }

    @GetMapping("/list")
    public MemberListWrapperResponse getAllUsers() {
        return memberbackservice.getAllUsers();
    }

    @GetMapping("/{memberId}")
    public MemberResponse getUserById(@PathVariable Long memberId) {
        return memberbackservice.getUserById(memberId);
    }

    @DeleteMapping("/{memberId}/delete")
    public Map<String, String> deleteUser(@PathVariable Long memberId) {
        memberbackservice.deleteUser(memberId);
        return Map.of("message", "User deleted successfully");
    }
}
