package pawparazzi.back.backoffice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pawparazzi.back.backoffice.dto.member.MemberListWrapperResponse;
import pawparazzi.back.backoffice.dto.member.MemberResponse;
import pawparazzi.back.member.entity.Member;
import pawparazzi.back.member.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberBackServiceImpl implements MemberBackService {

    private final MemberRepository memberRepository;

    public MemberListWrapperResponse getAllUsers() {
        List<Member> members = memberRepository.findAll();
        return MemberListWrapperResponse.from(members);
    }

    public MemberResponse getUserById(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("찾을 수 없는 유저: " + memberId));
        return MemberResponse.from(member);
    }

    public void deleteUser(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("찾을 수 없는 유저: " + memberId));
        memberRepository.delete(member);
    }
}

