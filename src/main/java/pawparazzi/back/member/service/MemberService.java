package pawparazzi.back.member.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final BoardRepository boardRepository;
    private final BoardMongoRepository boardMongoRepository;

    /**
     * 회원가입
     */
    public void registerUser(SignUpRequestDto request) {
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        if (memberRepository.existsByNickName(request.getNickName())) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        Member member = new Member(request.getEmail(), encodedPassword, request.getNickName(), request.getProfileImageUrl(), request.getName());
        memberRepository.save(member);
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

        // JWT를 memberId 기반으로 생성
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
    @Transactional
    public UpdateMemberResponseDto updateMember(Long memberId, UpdateMemberRequestDto request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("회원 정보를 찾을 수 없습니다."));

        if (request.getNickName() != null && !request.getNickName().isBlank()) {
            if (memberRepository.existsByNickName(request.getNickName())) {
                throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
            }
            member.setNickName(request.getNickName());
        }

        if (request.getName() != null && !request.getName().isBlank()) {
            member.setName(request.getName());
        }

        if (request.getProfileImageUrl() != null && !request.getProfileImageUrl().isBlank()) {
            member.setProfileImageUrl(request.getProfileImageUrl());
        }

        return new UpdateMemberResponseDto(
                member.getId(),
                member.getEmail(),
                member.getNickName(),
                member.getName(),
                member.getProfileImageUrl()
        );
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