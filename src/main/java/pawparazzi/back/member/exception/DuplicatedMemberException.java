package pawparazzi.back.member.exception;

public class DuplicatedMemberException extends RuntimeException {
    public DuplicatedMemberException(String message) {
        super(message);
    }
}
