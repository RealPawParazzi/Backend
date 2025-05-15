package pawparazzi.back.battle.controller;

import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pawparazzi.back.battle.dto.BattleResponseDto;
import pawparazzi.back.battle.service.BattleService;
import pawparazzi.back.member.service.MemberService;
import pawparazzi.back.security.util.JwtUtil;

import java.util.List;

@RestController
@RequestMapping("/api/battle")
@RequiredArgsConstructor
public class BattleController {

    private final BattleService battleService;
    private final MemberService memberService;
    private final JwtUtil jwtUtil;



}
