package pawparazzi.back.member.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pawparazzi.back.S3.S3UploadUtil;
import pawparazzi.back.S3.service.S3AsyncService;
import pawparazzi.back.board.entity.Board;
import pawparazzi.back.board.repository.BoardMongoRepository;
import pawparazzi.back.board.repository.BoardRepository;
import pawparazzi.back.member.dto.KakaoUserDto;
import pawparazzi.back.member.dto.request.LoginRequestDto;
import pawparazzi.back.member.dto.request.SignUpRequestDto;
import pawparazzi.back.member.dto.request.UpdateMemberRequestDto;
import pawparazzi.back.member.dto.response.MemberResponseDto;
import pawparazzi.back.member.dto.response.UpdateMemberResponseDto;
import pawparazzi.back.member.entity.Member;
import pawparazzi.back.member.repository.MemberRepository;
import pawparazzi.back.security.util.JwtUtil;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final BoardRepository boardRepository;
    private final BoardMongoRepository boardMongoRepository;
    private final S3AsyncService s3AsyncService;
    private final S3UploadUtil s3UploadUtil;

    /**
     * 회원가입
     */
    @Transactional
    public CompletableFuture<Void> registerUser(SignUpRequestDto request, MultipartFile profileImage) {
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        if (memberRepository.existsByNickName(request.getNickName())) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 프로필 이미지 업로드 (비동기 처리)
        String pathPrefix = "profile_images/" + request.getNickName();
        String defaultImageUrl = "https://default-image-url.com/default-profile.png";
        CompletableFuture<String> profileImageUrlFuture = s3UploadUtil.uploadImageAsync(profileImage, pathPrefix, defaultImageUrl);

        // 업로드 완료 후 Member 저장
        return profileImageUrlFuture.thenAccept(profileImageUrl -> {
            Member member = new Member(request.getEmail(), encodedPassword, request.getNickName(), profileImageUrl, request.getName());
            memberRepository.save(member);
        });
    }

    /**
     * 로그인
     */
    public String login(LoginRequestDto request) {
        Optional<Member> memberOptional = memberRepository.findByEmail(request.getEmail());
        if (memberOptional.isEmpty()) {
            throw new BadCredentialsException("이메일 또는 비밀번호가 잘못되었습니다.");
        }

        Member member = memberOptional.get();

        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new BadCredentialsException("이메일 또는 비밀번호가 잘못되었습니다.");
        }

        return jwtUtil.generateIdToken(member.getId());
    }

    /**
     * ID로 사용자 조회
     */
    public Member findById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("회원 정보 없음"));
    }

    /**
     * 회원 정보 수정
     */
    public CompletableFuture<UpdateMemberResponseDto> updateMember(Long memberId, UpdateMemberRequestDto request, MultipartFile newProfileImage) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("회원 정보를 찾을 수 없습니다."));

        // 닉네임 변경
        if (request.getNickName() != null && !request.getNickName().isBlank() &&
                !request.getNickName().equals(member.getNickName())) {
            if (memberRepository.existsByNickName(request.getNickName())) {
                throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
            }
            member.setNickName(request.getNickName());
        }

        // 이름 변경
        if (request.getName() != null && !request.getName().isBlank()) {
            member.setName(request.getName());
        }

        String pathPrefix = "profile_images/" + member.getNickName();
        String defaultImageUrl = "https://default-image-url.com/default-profile.png";
        String oldProfileImageUrl = member.getProfileImageUrl();

        // 새로운 프로필 이미지 업로드
        CompletableFuture<String> profileImageUrlFuture = s3UploadUtil.uploadImageAsync(newProfileImage, pathPrefix, defaultImageUrl);

        return profileImageUrlFuture.thenCompose(newProfileImageUrl -> {
            member.setProfileImageUrl(newProfileImageUrl);
            memberRepository.save(member);

            // 기존 프로필 이미지 삭제
            if (oldProfileImageUrl != null && !oldProfileImageUrl.equals(defaultImageUrl)) {
                String oldFileName = extractFileName(oldProfileImageUrl);
                return s3AsyncService.deleteFile("profile_images/" + oldFileName)
                        .exceptionally(ex -> {
                            System.err.println("S3 파일 삭제 실패: " + ex.getMessage());
                            return null;
                        })
                        .thenApply(ignored -> new UpdateMemberResponseDto(
                                member.getId(),
                                member.getEmail(),
                                member.getNickName(),
                                member.getName(),
                                member.getProfileImageUrl()
                        ));
            }

            return CompletableFuture.completedFuture(new UpdateMemberResponseDto(
                    member.getId(),
                    member.getEmail(),
                    member.getNickName(),
                    member.getName(),
                    member.getProfileImageUrl()
            ));
        });
    }

    public String extractFileName(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return null;
        }

        if (imageUrl.contains("/profile_images/")) {
            return imageUrl.substring(imageUrl.lastIndexOf("/profile_images/") + "/profile_images/".length());
        }

        return imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
    }

    /**
     * 회원 탈퇴
     */
    @Transactional
    public void deleteMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("회원 정보를 찾을 수 없습니다."));

        // 해당 사용자의 게시물 삭제
        List<Board> boards = boardRepository.findByAuthor(member);
        for (Board board : boards) {
            boardMongoRepository.deleteByMysqlId(board.getId());
            boardRepository.delete(board);
        }

        memberRepository.delete(member);
    }

    /**
     * 전체 회원 목록 조회
     */
    @Transactional
    public List<MemberResponseDto> getAllMembers() {
        return memberRepository.findAll().stream()
                .map(member -> new MemberResponseDto(
                        member.getId(),
                        member.getName(),
                        member.getNickName(),
                        member.getProfileImageUrl()
                ))
                .collect(Collectors.toList());
    }

    /**
     * 카카오 로그인 회원 처리
     */
    @Transactional
    public Long handleKakaoLogin(KakaoUserDto kakaoUser) {
        Optional<Member> existingMember = memberRepository.findByEmail(kakaoUser.getEmail());

        if (existingMember.isPresent()) {
            return existingMember.get().getId();
        } else {
            String randomPassword = passwordEncoder.encode(UUID.randomUUID().toString());

            Member newMember = new Member(
                    kakaoUser.getEmail(),
                    randomPassword,
                    kakaoUser.getNickname(),
                    kakaoUser.getProfileImageUrl(),
                    kakaoUser.getNickname()
            );
            memberRepository.save(newMember);
            return newMember.getId();
        }
    }
}