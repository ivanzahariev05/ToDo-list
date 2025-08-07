package demo.todolist.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler handler;

    /* --------------- DuplicateFieldException --------------- */
    @Test
    void handleDuplicate_ReturnsBadRequest() {
        DuplicateFieldException ex = new DuplicateFieldException("email", "Email already taken");

        ErrorResponse resp = handler.handleDuplicate(ex);

        assertEquals(HttpStatus.BAD_REQUEST.value(), resp.status());
        assertEquals("Validation error", resp.message());
        assertEquals("Email already taken", resp.errors().get("email"));
    }

    /* --------------- ResourceNotFoundException --------------- */
    @Test
    void handleNotFound_ReturnsNotFound() {
        ResourceNotFoundException ex = new ResourceNotFoundException("User not found");

        ErrorResponse resp = handler.handleNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND.value(), resp.status());
        assertEquals("User not found", resp.message());
    }

    /* --------------- MethodArgumentNotValidException --------------- */
    @Mock
    BindingResult bindingResult;

    @Test
    void handleArgumentNotValid_ReturnsFieldErrors() {
        FieldError fieldError = new FieldError("user", "email", "must be valid");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);
        ErrorResponse resp = handler.handleArgumentNotValid(ex);

        assertEquals(HttpStatus.BAD_REQUEST.value(), resp.status());
        assertEquals("Validation error", resp.message());
        assertEquals("must be valid", resp.errors().get("email"));
    }

    /* --------------- ConstraintViolationException --------------- */
    @Test
    void handleConstraintViolation_ReturnsFieldErrors() {
        @SuppressWarnings("unchecked")
        ConstraintViolation<?> cv = mock(ConstraintViolation.class);

        Path mockPath = mock(Path.class);
        when(mockPath.toString()).thenReturn("username");
        when(cv.getPropertyPath()).thenReturn(mockPath);

        when(cv.getMessage()).thenReturn("must not be blank");

        ConstraintViolationException ex = new ConstraintViolationException(Set.of(cv));
        ErrorResponse resp = handler.handleConstraintViolation(ex);

        assertEquals(HttpStatus.BAD_REQUEST.value(), resp.status());
        assertEquals("Validation error", resp.message());
        assertEquals("must not be blank", resp.errors().get("username"));
    }

}
