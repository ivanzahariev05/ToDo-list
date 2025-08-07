package demo.todolist.web.dto;

import lombok.Data;

@Data
public class RefreshRequest {
    private String refreshToken;
}