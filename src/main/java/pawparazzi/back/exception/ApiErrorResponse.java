package pawparazzi.back.exception;

import lombok.Getter;

import java.util.Map;

@Getter
public class ApiErrorResponse {
    private final int status;
    private final String message;
    private final Map<String, String> errors;

    public ApiErrorResponse(int status, String message) {
        this.status = status;
        this.message = message;
        this.errors = null; // 기본적으로 오류 필드는 없는 상태
    }

    public ApiErrorResponse(int status, String message, Map<String, String> errors) {
        this.status = status;
        this.message = message;
        this.errors = errors;
    }
}