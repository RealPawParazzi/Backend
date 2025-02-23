package pawparazzi.back.common;

public interface ResponseCode {

    // HTTP 200
    String SUCCESS = "SU";
    String VALIDATION_FAILED = "VF";

    // HTTP 400
    String DUPLICATE_ID = "DI";
    String DUPLICATE_NICKNAME = "DN";
    String NOT_EXISTED_USER = "NU";
    String NOT_EXISTED_BOARD = "NB";

    // HTTP 403
    String NO_PERMISSION = "NP";

    // HTTP 500
    String DATABASE_ERROR = "DE";
}
