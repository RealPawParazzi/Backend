package pawparazzi.back.walk.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pawparazzi.back.security.util.JwtUtil;
import pawparazzi.back.walk.dto.WalkRequestDto;
import pawparazzi.back.walk.dto.WalkResponseDto;
import pawparazzi.back.walk.entity.Walk;
import pawparazzi.back.walk.repository.WalkRepository;
import pawparazzi.back.walk.service.WalkService;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/walk")
@RequiredArgsConstructor
public class WalkController {

    private final JwtUtil jwtUtil;
    private final WalkService walkService;

    //산책 기록 생성
    @PostMapping
    public ResponseEntity<WalkResponseDto> createWalk(
            @RequestBody WalkRequestDto requestDto,
            @RequestHeader("Authorization") String token
    ) {
        Long userId = jwtUtil.extractMemberId(token.replace("Bearer ", ""));
        WalkResponseDto responseDto = walkService.createWalk(requestDto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    //산책 기록 조회 (산책 기록 아이디로)
    @GetMapping("/{walkId}")
    public ResponseEntity<WalkResponseDto> getWalk(
            @PathVariable Long walkId,
            @RequestHeader("Authorization") String token) {
        try{
            Long userId = jwtUtil.extractMemberId(token.replace("Bearer ", ""));
            WalkResponseDto responseDto = walkService.getWalkById(walkId, userId);
            return ResponseEntity.ok(responseDto);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    //산책 기록 삭제 (산책 기록 아이디로)
    @DeleteMapping("/{walkId}")
    public ResponseEntity<Void> deleteWalk(
            @PathVariable Long walkId,
            @RequestHeader("Authorization") String token) {
        try{
            Long userId = jwtUtil.extractMemberId(token.replace("Bearer ", ""));
            walkService.deleteWalk(walkId, userId);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    //펫별 산책 기록 조회
    @GetMapping("/pet/{petId}")
    public ResponseEntity<List<WalkResponseDto>> getWalkByPet(
            @PathVariable Long petId,
            @RequestHeader("Authorization") String token) {
        try {
            Long userId = jwtUtil.extractMemberId(token.replace("Bearer ", ""));
            List<WalkResponseDto> walks = walkService.getWalksByPetId(petId, userId);
            return ResponseEntity.ok(walks);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    //날짜별 산책 기록 조회
    @GetMapping("/date")
    public ResponseEntity<List<WalkResponseDto>> getWalkByPetDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime date,
            @RequestHeader("Authorization") String token){
        try{
            Long userId = jwtUtil.extractMemberId(token.replace("Bearer ", ""));
            List<WalkResponseDto> walks = walkService.getWalksByDate(date, userId);
            return ResponseEntity.ok(walks);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }
}