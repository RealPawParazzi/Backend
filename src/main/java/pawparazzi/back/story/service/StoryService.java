package pawparazzi.back.story.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pawparazzi.back.S3.service.S3AsyncService;
import pawparazzi.back.member.entity.Member;
import pawparazzi.back.member.repository.MemberRepository;
import pawparazzi.back.story.dto.response.StoryResponseDto;
import pawparazzi.back.story.dto.response.StoryViewListResponseDto;
import pawparazzi.back.story.dto.response.StoryViewResponseDto;
import pawparazzi.back.story.dto.response.UserStoryGroupDto;
import pawparazzi.back.story.entity.Story;
import pawparazzi.back.story.entity.StoryView;
import pawparazzi.back.story.entity.StoryLog;
import pawparazzi.back.story.repository.StoryRepository;
import pawparazzi.back.story.repository.StoryViewRepository;
import pawparazzi.back.story.repository.StoryLogRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoryService {

    private final StoryRepository storyRepository;
    private final StoryViewRepository storyViewRepository;
    private final MemberRepository memberRepository;
    private final S3AsyncService s3AsyncService;
    private final StoryLogRepository storyLogRepository;

    /**
     * 스토리 생성
     */
    @Transactional
    public Long createStory(Long memberId, String caption, MultipartFile mediaFile) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        String mediaUrl;
        try {
            String originalFilename = mediaFile.getOriginalFilename();
            String extension = ".mp4";
            String fileName = "story/" + System.currentTimeMillis() + extension;
            mediaUrl = s3AsyncService.uploadFile(fileName, mediaFile.getBytes(), "video/mp4").join();
        } catch (IOException e) {
            throw new RuntimeException("스토리 이미지 업로드 중 오류 발생", e);
        }

        Story story = Story.builder()
                .member(member)
                .caption(caption != null ? caption : "")
                .mediaUrl(mediaUrl)
                .createdAt(LocalDateTime.now())
                .expired(false)
                .build();
        Story savedStory = storyRepository.save(story);

        StoryLog storyLog = new StoryLog(member.getId(), LocalDateTime.now());
        storyLogRepository.save(storyLog);

        return savedStory.getId();
    }


    /**
     * 특정 스토리를 본 사용자 목록 조회
     */
    @Transactional(readOnly = true)
    public StoryViewListResponseDto getViewersOfStory(Long storyId) {
        List<StoryView> views = storyViewRepository.findAllByStoryId(storyId);
        List<StoryViewResponseDto> viewers = views.stream()
                .map(view -> StoryViewResponseDto.builder()
                        .viewerId(view.getViewer().getId())
                        .viewerNickname(view.getViewer().getNickName())
                        .viewerProfileImageUrl(view.getViewer().getProfileImageUrl())
                        .viewedAt(view.getViewedAt())
                        .build())
                .collect(Collectors.toList());

        return StoryViewListResponseDto.builder()
                .viewCount((long) viewers.size())
                .viewers(viewers)
                .build();
    }

    /**
     * 특정 스토리 조회 및 조회 기록 저장
     */
    @Transactional
    public StoryResponseDto viewStory(Long storyId, Long viewerId) {
        Story story = storyRepository.findByIdAndExpiredFalse(storyId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않거나 만료된 스토리입니다."));

        // 스토리를 작성한 사용자는 기록에 저장되지 않음
        if (story.getMember().getId().equals(viewerId)) {
            return StoryResponseDto.of(story);
        }

        Member viewer = memberRepository.findById(viewerId)
                .orElseThrow(() -> new IllegalArgumentException("조회 사용자를 찾을 수 없습니다."));

        boolean alreadyViewed = storyViewRepository.existsByStoryIdAndViewerId(storyId, viewerId);
        if (!alreadyViewed) {
            StoryView view = StoryView.builder()
                    .story(story)
                    .viewer(viewer)
                    .viewedAt(LocalDateTime.now())
                    .build();
            storyViewRepository.save(view);
        }

        return StoryResponseDto.of(story);
    }

    /**
     * 사용자 본인의 모든 스토리 조회
     */
    @Transactional(readOnly = true)
    public List<StoryResponseDto> getMyStories(Long memberId) {
        List<Story> stories = storyRepository.findByMemberIdAndExpiredFalse(memberId);
        return stories.stream()
                .map(StoryResponseDto::of)
                .collect(Collectors.toList());
    }

    /**
     * 만료 처리 (ex: 24시간이 지난 경우 외부 스케줄러에서 호출)
     * 만료된 스토리를 S3와 DB에서 삭제
     */
    @Transactional
    public void expireOldStories() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        List<Story> toExpire = storyRepository.findByExpiredFalseAndCreatedAtBefore(cutoff);

        toExpire.forEach(story -> {
            String fileName = story.getMediaUrl().substring(story.getMediaUrl().lastIndexOf("/") + 1);
            s3AsyncService.deleteFile("story/" + fileName).join();
        });

        for (Story story : toExpire) {
            storyViewRepository.deleteAllByStoryId(story.getId());
            story.setExpired(true);
        }

        storyRepository.saveAll(toExpire);
        storyRepository.deleteAll(toExpire);
    }

    /**
     * 스토리 삭제
     */
    @Transactional
    public void deleteStory(Long storyId, Long memberId) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new IllegalArgumentException("스토리를 찾을 수 없습니다."));

        if (!story.getMember().getId().equals(memberId)) {
            throw new IllegalArgumentException("스토리를 삭제할 권한이 없습니다.");
        }

        // s3 삭제
        String fileName = story.getMediaUrl().substring(story.getMediaUrl().lastIndexOf("/") + 1);
        s3AsyncService.deleteFile("story/" + fileName).join();

        // 조회기록 삭제
        storyViewRepository.deleteAllByStoryId(storyId);

        // 삭제
        storyRepository.delete(story);
    }

    /**
     * 스토리 작성자인지 확인
     */
    @Transactional(readOnly = true)
    public boolean isStoryOwner(Long storyId, Long memberId) {
        return storyRepository.findById(storyId)
                .map(story -> story.getMember().getId().equals(memberId))
                .orElse(false);
    }

    /**
     * 스토리가 있는 사용자 목록과 각 사용자의 스토리 리스트 반환
     */
    @Transactional(readOnly = true)
    public List<UserStoryGroupDto> getGroupedStories(Long viewerId) {
        List<Story> stories = storyRepository.findByExpiredFalseOrderByCreatedAtDesc();

        // 사용자별로 그룹핑
        java.util.Map<Member, List<Story>> groupedByUser = stories.stream()
                .collect(java.util.stream.Collectors.groupingBy(Story::getMember));

        return groupedByUser.entrySet().stream()
                .map(entry -> UserStoryGroupDto.builder()
                        .memberId(entry.getKey().getId())
                        .nickname(entry.getKey().getNickName())
                        .profileImageUrl(entry.getKey().getProfileImageUrl())
                        .stories(entry.getValue().stream()
                                .map(story -> new UserStoryGroupDto.StoryDto(
                                        story.getId(),
                                        story.getMediaUrl(),
                                        story.getCaption(),
                                        story.getCreatedAt(),
                                        story.isExpired(),
                                        storyViewRepository.existsByStoryIdAndViewerId(story.getId(), viewerId)
                                ))
                                .collect(java.util.stream.Collectors.toList()))
                        .build())
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 스토리 수정
     */
    @Transactional
    public StoryResponseDto updateStory(Long storyId, Long memberId, String caption, MultipartFile mediaFile) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new IllegalArgumentException("스토리를 찾을 수 없습니다."));

        if (!story.getMember().getId().equals(memberId)) {
            throw new IllegalArgumentException("스토리를 수정할 권한이 없습니다.");
        }

        if (caption != null) {
            story.setCaption(caption.trim());
        } else {
            story.setCaption("");
        }

        if (mediaFile != null && !mediaFile.isEmpty()) {
            // 기존 이미지 삭제
            String oldFileName = story.getMediaUrl().substring(story.getMediaUrl().lastIndexOf("/") + 1);
            s3AsyncService.deleteFile("story/" + oldFileName).join();

            // 새 이미지 업로드
            try {
                String newFileName = "story/" + System.currentTimeMillis() + "_" + mediaFile.getOriginalFilename();
                String newMediaUrl = s3AsyncService.uploadFile(newFileName, mediaFile.getBytes(), mediaFile.getContentType()).join();
                story.setMediaUrl(newMediaUrl);
            } catch (IOException e) {
                throw new RuntimeException("스토리 이미지 수정 중 오류 발생", e);
            }
        }

        return StoryResponseDto.of(story);
    }
}
