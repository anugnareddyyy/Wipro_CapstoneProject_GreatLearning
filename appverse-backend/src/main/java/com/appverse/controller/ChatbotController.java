package com.appverse.controller;
 
import com.appverse.dto.request.ChatRequest;
import com.appverse.dto.response.ChatResponse;
import com.appverse.repository.UserRepository;
import com.appverse.service.ChatbotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
 
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "AI Chatbot", description = "Gemini-powered AppVerse assistant")
public class ChatbotController {
 
    private final ChatbotService chatbotService;
    private final UserRepository userRepository;
 
    @PostMapping
    @Operation(summary = "Chat with the AppVerse AI assistant")
    public ResponseEntity<ChatResponse> chat(
            @Valid @RequestBody ChatRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
 
        Long userId = null;
        if (userDetails != null) {
            userId = userRepository.findByEmail(userDetails.getUsername())
                    .map(u -> u.getUserId()).orElse(null);
        }
        return ResponseEntity.ok(chatbotService.chat(request, userId));
    }
}
 