package pawparazzi.back.place.controller;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pawparazzi.back.place.dto.PlaceRequestDto;
import pawparazzi.back.place.dto.PlaceResponseDto;
import pawparazzi.back.place.service.PlaceService;
import pawparazzi.back.security.util.JwtUtil;

import java.util.List;

@RestController
@RequestMapping("/api/place")
@RequiredArgsConstructor
public class PlaceController {

    private final PlaceService placeService;
    private final JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<PlaceResponseDto> savePlace(
            @RequestHeader("Authorization") String token, @RequestBody PlaceRequestDto placeRequestDto) {
        Long userId;
        try{
            userId = jwtUtil.extractMemberId(token.replace("Bearer ", ""));
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
        PlaceResponseDto savedPlace = placeService.savePlace(userId, placeRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedPlace);
    }

    @GetMapping
    public ResponseEntity<List<PlaceResponseDto>> getPlaces(
            @RequestHeader("Authorization") String token){
        Long userId;
        try{
            userId = jwtUtil.extractMemberId(token.replace("Bearer ", ""));
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<PlaceResponseDto> places = placeService.getPlacesByMemberId(userId);
        return ResponseEntity.ok(places);
    }

    @GetMapping("/{placeId}")
    public ResponseEntity<PlaceResponseDto> getPlace(
            @RequestHeader("Authorization") String token, @PathVariable Long placeId) {
        Long userId;
        try {
            userId = jwtUtil.extractMemberId(token.replace("Bearer ", ""));
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        PlaceResponseDto place = placeService.getPlaceById(placeId);
        return ResponseEntity.ok(place);
    }

    @DeleteMapping("/{placeId}")
    public ResponseEntity<Void> deletePlace(
            @RequestHeader("Authorization") String token, @PathVariable Long placeId){
        Long userId;
        try {
            userId = jwtUtil.extractMemberId(token.replace("Bearer ", ""));
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        placeService.deletePlace(userId, placeId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{placeId}")
    public ResponseEntity<PlaceResponseDto> updatePlace(
            @RequestHeader("Authorization") String token,
            @PathVariable Long placeId,
            @RequestBody PlaceRequestDto placeRequestDto){
        Long userId;
        try {
            userId = jwtUtil.extractMemberId(token.replace("Bearer ", ""));
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        PlaceResponseDto updatedPlace = placeService.updatePlace(userId, placeId, placeRequestDto);
        return ResponseEntity.ok(updatedPlace);
    }
}
