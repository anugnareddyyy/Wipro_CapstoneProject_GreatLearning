package com.appverse.dto.response;
 
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
 
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
 
    private String message;
    private List<AppResponse> recommendedApps;
    private Boolean hasRecommendations;
    private String intent;
 
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
 