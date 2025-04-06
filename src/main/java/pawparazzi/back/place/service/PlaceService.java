package pawparazzi.back.place.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import pawparazzi.back.member.entity.Member;
import pawparazzi.back.member.repository.MemberRepository;
import pawparazzi.back.place.dto.PlaceRequestDto;
import pawparazzi.back.place.dto.PlaceResponseDto;
import pawparazzi.back.place.entity.Place;
import pawparazzi.back.place.repository.PlaceRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlaceService {

    private final PlaceRepository placeRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public PlaceResponseDto savePlace(Long memberId, PlaceRequestDto placeRequestDto) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원을 찾을 수 없습니다. id = " + memberId));

        Place place = new Place();
        place.setName(placeRequestDto.getName());
        place.setAddress(placeRequestDto.getAddress());
        place.setLatitude(placeRequestDto.getLatitude());
        place.setLongitude(placeRequestDto.getLongitude());
        place.setMember(member);

        placeRepository.save(place);
        return new PlaceResponseDto(place);
    }

    @Transactional(readOnly = true)
    public List<PlaceResponseDto> getPlacesByMemberId(Long memberId) {
        List<Place> places = placeRepository.findByMemberId(memberId);
        return places.stream()
                .map(PlaceResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PlaceResponseDto getPlaceById(Long placeId) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 장소를 찾을 수 없습니다. id = " + placeId));
        return new PlaceResponseDto(place);
    }

    @Transactional
    public void deletePlace(Long userId, Long placeId) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 장소를 찾을 수 없습니다. id = " + placeId));

        if (!place.getMember().getId().equals(userId)) {
            throw new IllegalArgumentException("해당 장소를 삭제할 권한이 없습니다.");
        }
        placeRepository.delete(place);
    }

    @Transactional
    public PlaceResponseDto updatePlace(Long userId, Long placeId, PlaceRequestDto placeRequestDto) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 장소를 찾을 수 없습니다. id = " + placeId));

        if (!place.getMember().getId().equals(userId)) {
            throw new IllegalArgumentException("해당 장소를 찾을 수 없습니다.");
        }

        // null이 아닌 필드만 업데이트
        if (placeRequestDto.getName() != null) {
            place.setName(placeRequestDto.getName());
        }

        if (placeRequestDto.getAddress() != null) {
            place.setAddress(placeRequestDto.getAddress());
        }

        if (placeRequestDto.getLatitude() != null) {
            place.setLatitude(placeRequestDto.getLatitude());
        }

        if (placeRequestDto.getLongitude() != null) {
            place.setLongitude(placeRequestDto.getLongitude());
        }

        placeRepository.save(place);
        return new PlaceResponseDto(place);
    }

}
