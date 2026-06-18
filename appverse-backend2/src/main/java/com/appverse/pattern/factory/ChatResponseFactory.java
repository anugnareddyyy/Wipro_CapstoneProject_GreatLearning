package com.appverse.pattern.factory;
 
import com.appverse.dto.response.AppResponse;
import com.appverse.dto.response.ChatResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
 
import java.util.List;
 
/**
 * Factory Pattern — Creates different types of ChatResponse
 * objects based on the response scenario.
 *
 * Centralizes ChatResponse creation so changes to the
 * response structure only need to be made in one place.
 *
 * Factory methods:
 * - createSuccessResponse()  — normal AI response
 * - createErrorResponse()    — when AI fails
 * - createFallbackResponse() — rule-based fallback
 * - createGreetingResponse() — welcome message
 */
@Component
@Slf4j
public class ChatResponseFactory {
 
    /**
     * Create a successful AI-generated response.
     */
    public ChatResponse createSuccessResponse(
            String message,
            List<AppResponse> apps,
            String intent) {
 
        log.debug("Factory creating SUCCESS response, intent: {}", intent);
        return ChatResponse.builder()
                .message(message)
                .recommendedApps(apps)
                .hasRecommendations(apps != null && !apps.isEmpty())
                .intent(intent)
                .build();
    }
 
    /**
     * Create an error response when the AI service is unavailable.
     */
    public ChatResponse createErrorResponse() {
        log.debug("Factory creating ERROR response");
        return ChatResponse.builder()
                .message("I'm having trouble connecting right now. " +
                         "Please try again in a moment! 🔄")
                .hasRecommendations(false)
                .intent("ERROR")
                .build();
    }
 
    /**
     * Create a fallback response when Gemini API key is not set.
     */
    public ChatResponse createFallbackResponse(
            String message,
            List<AppResponse> apps) {
 
        log.debug("Factory creating FALLBACK response");
        return ChatResponse.builder()
                .message(message)
                .recommendedApps(apps)
                .hasRecommendations(apps != null && !apps.isEmpty())
                .intent("FALLBACK")
                .build();
    }
 
    /**
     * Create the initial greeting response.
     */
    public ChatResponse createGreetingResponse() {
        log.debug("Factory creating GREETING response");
        return ChatResponse.builder()
                .message("Hi! 👋 I'm AppBot, your AppVerse AI assistant! " +
                         "I can help you find the perfect app, answer questions " +
                         "about AppVerse, or recommend apps based on your needs. " +
                         "What are you looking for?")
                .hasRecommendations(false)
                .intent("GREETING")
                .build();
    }
}
 