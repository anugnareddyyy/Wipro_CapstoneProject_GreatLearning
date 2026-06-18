package com.appverse.service;
 
import com.appverse.dto.request.ChatRequest;
import com.appverse.dto.response.ChatResponse;
 
public interface ChatbotService {
    ChatResponse chat(ChatRequest request, Long userId);
}