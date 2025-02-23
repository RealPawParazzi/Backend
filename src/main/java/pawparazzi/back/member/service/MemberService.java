package pawparazzi.back.member.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import pawparazzi.back.member.dto.request.LoginRequestDto;
import pawparazzi.back.member.dto.request.SignUpRequestDto;
import pawparazzi.back.member.entity.Member;
import pawparazzi.back.member.repository.MemberRepository;
import pawparazzi.back.security.util.JwtUtil;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * 회원가입
     */
    public void registerUser(SignUpRequestDto request) {
        // 1. 이메일 중복 확인
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        // 2. 닉네임 중복 확인
        if (memberRepository.existsByNickName(request.getNickName())) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        // 3. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 4. 회원 데이터 저장
        Member member = new Member(request.getEmail(), encodedPassword, request.getNickName(), request.getProfileImageUrl());
        memberRepository.save(member);
    }

    /**
     * 로그인 (JWT 발급)
     */
    public String login(LoginRequestDto request) {
        // 1. 이메일로 사용자 조회
        Optional<Member> memberOptional = memberRepository.findByEmail(request.getEmail());
        if (memberOptional.isEmpty()) {
            throw new BadCredentialsException("이메일 또는 비밀번호가 잘못되었습니다.");
        }

        Member member = memberOptional.get();

        // 2. 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new BadCredentialsException("이메일 또는 비밀번호가 잘못되었습니다.");
        }

        // 3. JWT 토큰 생성 및 반환
        return jwtUtil.generateToken(member.getEmail());
    }

    public Member findById(Long id) {
        return memberRepository.findById(String.valueOf(id))
                .orElseThrow(()-> new EntityNotFoundException("회원 정보 없음"));
    }
}