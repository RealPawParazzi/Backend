package pawparazzi.back.backoffice.dto.member;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pawparazzi.back.member.entity.Member;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberListWrapperResponse {

    private long totalUserCount;
    private List<MemberListItem> users;

    public static MemberListWrapperResponse from(List<Member> members) {
        List<MemberListItem> userList = members.stream()
                .map(MemberListItem::from)
                .collect(Collectors.toList());
        return new MemberListWrapperResponse(userList.size(), userList);
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberListItem {
        private Long id;
        private String email;
        private String nickName;
        private String name;
        private String createdAt;

        public static MemberListItem from(Member member) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            return new MemberListItem(
                    member.getId(),
                    member.getEmail(),
                    member.getNickName(),
                    member.getName(),
                    member.getCreatedAt().format(formatter)
            );
        }
    }
}
