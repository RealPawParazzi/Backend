package pawparazzi.back.common;

public interface ResponseMessage {
    // HTTP 200
    String SUCCESS = "Success";
    String VALIDATION_FAILED = "Validation failed";

    // HTTP 400
    String DUPLICATE_ID = "Duplicate ID";
    String DUPLICATE_NICKNAME = "Duplicate Nickname";
    String NOT_EXISTED_USER = "Not Existed User";
    String NOT_EXISTED_BOARD = "Not Existed Board";

    // HTTP 403
    String NO_PERMISSION = "No Permission";

    // HTTP 500
    String DATABASE_ERROR = "Database Error";
}
