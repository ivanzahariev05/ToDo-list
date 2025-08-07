package demo.todolist.exception;

import lombok.Getter;

@Getter
public class DuplicateFieldException extends RuntimeException {

    private final String field;

    public DuplicateFieldException(String field, String message) {
        super(message);
        this.field = field;
    }


}