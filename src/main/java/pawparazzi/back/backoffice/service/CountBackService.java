package pawparazzi.back.backoffice.service;

public interface CountBackService {
    long countUsers();
    long countBoards();
    long countPets();
    long countStories();
    int[] getMonthlyUserCounts();
    int[] getMonthlyBoardCounts();
}
