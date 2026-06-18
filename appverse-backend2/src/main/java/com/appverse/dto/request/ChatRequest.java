package com.appverse.dto.request;
 
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;
 
@Data
public class ChatRequest {
 
    @NotBlank(message = "Message cannot be empty")
    @Size(max = 1000, message = "Message must not exceed 1000 characters")
    private String message;
 
    private List<ChatMessage> history;
 
    @Data
    public static class ChatMessage {
        private String role;
        private String content;
    }
}