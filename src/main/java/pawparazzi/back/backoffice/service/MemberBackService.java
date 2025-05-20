package pawparazzi.back.backoffice.service;

import pawparazzi.back.backoffice.dto.member.MemberListWrapperResponse;
import pawparazzi.back.backoffice.dto.member.MemberResponse;


public interface MemberBackService {
    MemberListWrapperResponse getAllUsers();
    MemberResponse getUserById(Long memberId);
    void deleteUser(Long memberId);
}
