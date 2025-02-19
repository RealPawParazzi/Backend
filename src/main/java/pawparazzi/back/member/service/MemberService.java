package pawparazzi.back.member.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pawparazzi.back.member.dto.MemberLoginDto;
import pawparazzi.back.member.dto.MemberRegisterDto;
import pawparazzi.back.member.entity.Member;
import pawparazzi.back.member.exception.DuplicatedMemberException;
import pawparazzi.back.member.repository.MemberRepository;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final Set<String> tokenBlacklist = ConcurrentHashMap.newKeySet();
    private final MemberRepository memberRepository;

    public String login(MemberLoginDto loginDto){
        Optional<Member> memberOptional = memberRepository.findByEmail(loginDto.getEmail());
        if(memberOptional.isEmpty()){
            throw new IllegalArgumentException("Invalid email or password");
        }

        Member member = memberOptional.get();

        if(!member.getPassword().equals(loginDto.getPassword())){
            throw new IllegalArgumentException("Invalid password");
        }
        return generateToken(member);
    }

    public void registerUser(MemberRegisterDto registerDto){
        //동일한 이름 존재 확인
        if (memberRepository.existsByName(registerDto.getName())) {
            throw new DuplicatedMemberException("duplicated name");
        }

        // 동일한 email 존재 확인
        if (memberRepository.existsByEmail(registerDto.getEmail())) {
            throw new DuplicatedMemberException("duplicated email");
        }
    }

    public void deleteMember(Long memberId){
        memberRepository.deleteById(memberId);
    }

    //JWT로 수정 필요
    private String generateToken(Member member) {
        return "jwt-tmp-token" + member.getEmail();
    }

    public void logout(String token){
        if(token!=null&&!tokenBlacklist.contains(token)){
            tokenBlacklist.add(token);
        }
    }

}
