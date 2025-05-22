package pawparazzi.back.backoffice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pawparazzi.back.board.repository.BoardRepository;
import pawparazzi.back.member.repository.MemberRepository;
import pawparazzi.back.pet.repository.PetRepository;
import pawparazzi.back.story.repository.StoryLogRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CountBackServiceImpl implements CountBackService {

    private final MemberRepository memberRepository;
    private final BoardRepository boardRepository;
    private final PetRepository petRepository;
    private final StoryLogRepository storyLogRepository;

    public long countUsers() {
        return memberRepository.count();
    }

    public long countBoards() {
        return boardRepository.count();
    }

    public long countPets() {
        return petRepository.count();
    }

    public long countStories() {
        return storyLogRepository.count();
    }

    public int[] getMonthlyUserCounts() {
        int[] monthlyCounts = new int[12];
        List<Object[]> results = memberRepository.countMembersGroupedByMonth();

        for (Object[] row : results) {
            Integer month = (Integer) row[0];
            Long count = (Long) row[1];
            monthlyCounts[month - 1] = count.intValue();
        }

        return monthlyCounts;
    }

    public int[] getMonthlyBoardCounts() {
        int[] monthlyCounts = new int[12];
        List<Object[]> results = boardRepository.countBoardsGroupedByMonth();

        for (Object[] row : results) {
            Integer month = (Integer) row[0];
            Long count = (Long) row[1];
            monthlyCounts[month - 1] = count.intValue();
        }

        return monthlyCounts;
    }

    public int[] getMonthlyStoryCounts() {
        int[] monthlyCounts = new int[12];
        List<Object[]> results = storyLogRepository.countStoriesGroupedByMonth();

        for (Object[] row : results) {
            Integer month = (Integer) row[0];
            Long count = (Long) row[1];
            monthlyCounts[month - 1] = count.intValue();
        }

        return monthlyCounts;
    }

    public int[] getMonthlyPetCounts() {
        int[] monthlyCounts = new int[12];
        List<Object[]> results = petRepository.countPetsGroupedByMonth();

        for (Object[] row : results) {
            Integer month = (Integer) row[0];
            Long count = (Long) row[1];
            monthlyCounts[month - 1] = count.intValue();
        }

        return monthlyCounts;
    }


}
