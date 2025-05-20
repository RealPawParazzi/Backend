package pawparazzi.back.backoffice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pawparazzi.back.backoffice.service.CountBackServiceImpl;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/count")
@RequiredArgsConstructor
public class CountBackController {

    private final CountBackServiceImpl countBackService;

    @GetMapping("/member")
    public Map<String, Long> countAllUsers() {
        long count = countBackService.countUsers();
        return Map.of("totalUserCount", count);
    }

    @GetMapping("/board")
    public  Map<String, Long> countAllBoards() {
        long count = countBackService.countBoards();
        return Map.of("totalBoardCount", count);
    }

    @GetMapping("/pet")
    public  Map<String, Long> countAllPets() {
        long count = countBackService.countPets();
        return Map.of("totalPetCount", count);
    }

    @GetMapping("/story")
    public  Map<String, Long> countAllStorys() {
        long count = countBackService.countStories();
        return Map.of("totalStoryCount", count);
    }

    @GetMapping("/member/monthly")
    public Map<String, int[]> getMonthlyUserStats() {
        int[] monthlyCounts = countBackService.getMonthlyUserCounts();
        return Map.of("data", monthlyCounts);
    }


    @GetMapping("/board/monthly")
    public Map<String, int[]> getMonthlyBoardStats() {
        int[] monthlyCounts = countBackService.getMonthlyBoardCounts();
        return Map.of("data", monthlyCounts);
    }

}
