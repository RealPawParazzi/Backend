package pawparazzi.back.pet.exception;

public class DuplicatedPetException extends RuntimeException {
    public DuplicatedPetException() {
    }

    public DuplicatedPetException(String message) {
        super(message);
    }

    public DuplicatedPetException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicatedPetException(Throwable cause) {
        super(cause);
    }

    public DuplicatedPetException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
