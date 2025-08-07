package demo.todolist.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.*;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(DuplicateFieldException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleDuplicate(DuplicateFieldException ex) {
        return ErrorResponse.of(
                HttpStatus.BAD_REQUEST,
                "Validation error",
                Map.of(ex.getField(), ex.getMessage())
        );
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(ResourceNotFoundException ex) {
        return ErrorResponse.of(HttpStatus.NOT_FOUND, ex.getMessage());
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleArgumentNotValid(MethodArgumentNotValidException ex) {

        Map<String, String> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        err -> err.getField(),
                        err -> err.getDefaultMessage(),
                        (first, second) -> first   // ако има дублиращи се полета
                ));

        return ErrorResponse.of(HttpStatus.BAD_REQUEST, "Validation error", fieldErrors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleConstraintViolation(ConstraintViolationException ex) {

        Map<String, String> fieldErrors = ex.getConstraintViolations()
                .stream()
                .collect(Collectors.toMap(
                        v -> v.getPropertyPath().toString(),
                        v -> v.getMessage(),
                        (f, s) -> f
                ));

        return ErrorResponse.of(HttpStatus.BAD_REQUEST, "Validation error", fieldErrors);
    }

    /* ---------- 3. Грешен тип или липсващи параметри ---------- */

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String msg = "Parameter '%s' should be of type '%s'"
                .formatted(ex.getName(), ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");
        return ErrorResponse.of(HttpStatus.BAD_REQUEST, msg);
    }

    /* ---------- 4. HTTP method not allowed ---------- */

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ErrorResponse handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        return ErrorResponse.of(HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage());
    }

    /* ---------- 5. Catch-all (последна инстанция) ---------- */

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleOther(Exception ex) {
        // Логни ex  (log.error)  ако имаш логър
        return ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
    }
}
