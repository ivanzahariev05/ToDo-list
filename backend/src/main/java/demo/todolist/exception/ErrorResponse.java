package demo.todolist.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        Map<String, String> errors
) {
    public static ErrorResponse of(HttpStatus status,
                                   String message,
                                   Map<String, String> fieldErrors) {
        return new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.name(),
                message,
                fieldErrors == null || fieldErrors.isEmpty() ? null : fieldErrors
        );
    }

    public static ErrorResponse of(HttpStatus status, String message) {
        return of(status, message, null);
    }
}
