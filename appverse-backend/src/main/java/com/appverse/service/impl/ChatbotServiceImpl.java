package com.appverse.service.impl;
 
import com.appverse.dto.request.ChatRequest;
import com.appverse.dto.response.AppResponse;
import com.appverse.dto.response.ChatResponse;
import com.appverse.entity.AppCategory;
import com.appverse.entity.AppStatus;
import com.appverse.repository.AppRepository;
import com.appverse.service.ChatbotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
 
import java.util.*;
import java.util.stream.Collectors;
 
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatbotServiceImpl implements ChatbotService {
 
    private final AppRepository appRepository;
    private final RestTemplate restTemplate;
 
    @Value("${app.gemini.api-key:}")
    private String geminiApiKey;
 
    private static final String GEMINI_URL =
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=";
 
    private static final Map<String, AppCategory> KEYWORD_CATEGORY_MAP = new HashMap<>();
    static {
        KEYWORD_CATEGORY_MAP.put("productivity", AppCategory.PRODUCTIVITY);
        KEYWORD_CATEGORY_MAP.put("work", AppCategory.PRODUCTIVITY);
        KEYWORD_CATEGORY_MAP.put("entertainment", AppCategory.ENTERTAINMENT);
        KEYWORD_CATEGORY_MAP.put("movie", AppCategory.ENTERTAINMENT);
        KEYWORD_CATEGORY_MAP.put("education", AppCategory.EDUCATION);
        KEYWORD_CATEGORY_MAP.put("learn", AppCategory.EDUCATION);
        KEYWORD_CATEGORY_MAP.put("study", AppCategory.EDUCATION);
        KEYWORD_CATEGORY_MAP.put("game", AppCategory.GAMING);
        KEYWORD_CATEGORY_MAP.put("gaming", AppCategory.GAMING);
        KEYWORD_CATEGORY_MAP.put("finance", AppCategory.FINANCE);
        KEYWORD_CATEGORY_MAP.put("money", AppCategory.FINANCE);
        KEYWORD_CATEGORY_MAP.put("health", AppCategory.HEALTH_FITNESS);
        KEYWORD_CATEGORY_MAP.put("fitness", AppCategory.HEALTH_FITNESS);
        KEYWORD_CATEGORY_MAP.put("social", AppCategory.SOCIAL);
        KEYWORD_CATEGORY_MAP.put("music", AppCategory.MUSIC);
        KEYWORD_CATEGORY_MAP.put("travel", AppCategory.TRAVEL);
        KEYWORD_CATEGORY_MAP.put("utility", AppCategory.UTILITIES);
        KEYWORD_CATEGORY_MAP.put("tool", AppCategory.UTILITIES);
        KEYWORD_CATEGORY_MAP.put("news", AppCategory.NEWS);
        KEYWORD_CATEGORY_MAP.put("shopping", AppCategory.SHOPPING);
    }
 
    @Override
    public ChatResponse chat(ChatRequest request, Long userId) {
        log.info("Chatbot request from user {}: {}", userId, request.getMessage());
        try {
            if (geminiApiKey == null || geminiApiKey.isBlank()) {
                log.warn("Gemini API key not configured - using fallback");
                return smartFallbackResponse(request.getMessage());
            }
            String systemContext = buildSystemPrompt();
            String fullPrompt = buildFullPrompt(systemContext, request);
            String aiText = callGeminiAPI(fullPrompt);
            List<AppResponse> apps = extractRecommendedApps(request.getMessage(), aiText);
            return ChatResponse.builder()
                    .message(aiText)
                    .recommendedApps(apps)
                    .hasRecommendations(!apps.isEmpty())
                    .intent(detectIntent(request.getMessage()))
                    .build();
        } catch (Exception e) {
            log.error("Gemini API error: {}", e.getMessage());
            return smartFallbackResponse(request.getMessage());
        }
    }
 
    private String buildSystemPrompt() {
        List<com.appverse.entity.App> apps = appRepository
                .findByStatus(AppStatus.APPROVED, PageRequest.of(0, 30))
                .getContent();
 
        StringBuilder appList = new StringBuilder();
        for (com.appverse.entity.App app : apps) {
            appList.append(String.format("- %s (%s): %s%n",
                    app.getName(), app.getCategory(),
                    app.getTagline() != null ? app.getTagline() :
                    app.getDescription().substring(0, Math.min(60, app.getDescription().length()))));
        }
 
        return "You are AppBot, the friendly AI assistant for AppVerse AI marketplace.\n"
            + "Help users find the right apps, answer questions about categories, "
            + "and provide personalized recommendations.\n"
            + "Current apps available:\n"
            + (appList.length() > 0 ? appList.toString() : "No apps yet.")
            + "\nKeep responses under 150 words. Be friendly and helpful.";
    }
 
    private String buildFullPrompt(String systemPrompt, ChatRequest request) {
        StringBuilder prompt = new StringBuilder(systemPrompt);
        prompt.append("\n\nConversation:\n");
        if (request.getHistory() != null) {
            for (ChatRequest.ChatMessage msg : request.getHistory()) {
                prompt.append(msg.getRole().equals("user") ? "User: " : "AppBot: ");
                prompt.append(msg.getContent()).append("\n");
            }
        }
        prompt.append("User: ").append(request.getMessage()).append("\nAppBot:");
        return prompt.toString();
    }
 
    @SuppressWarnings("unchecked")
    private String callGeminiAPI(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
 
        Map<String, Object> body = new HashMap<>();
        Map<String, Object> content = new HashMap<>();
        Map<String, Object> part = new HashMap<>();
        part.put("text", prompt);
        content.put("parts", List.of(part));
        body.put("contents", List.of(content));
 
        Map<String, Object> genConfig = new HashMap<>();
        genConfig.put("temperature", 0.7);
        genConfig.put("maxOutputTokens", 300);
        body.put("generationConfig", genConfig);
 
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.exchange(
                GEMINI_URL + geminiApiKey, HttpMethod.POST, entity, Map.class);
 
        Map<String, Object> responseBody = response.getBody();
        if (responseBody != null && responseBody.containsKey("candidates")) {
            List<Map<String, Object>> candidates =
                    (List<Map<String, Object>>) responseBody.get("candidates");
            if (!candidates.isEmpty()) {
                Map<String, Object> candidate = candidates.get(0);
                Map<String, Object> contentMap = (Map<String, Object>) candidate.get("content");
                List<Map<String, Object>> parts = (List<Map<String, Object>>) contentMap.get("parts");
                if (!parts.isEmpty()) {
                    return parts.get(0).get("text").toString().trim();
                }
            }
        }
        throw new RuntimeException("Empty Gemini response");
    }
 
    private List<AppResponse> extractRecommendedApps(String userMessage, String aiResponse) {
        String combined = (userMessage + " " + aiResponse).toLowerCase();
        AppCategory matchedCategory = null;
        for (Map.Entry<String, AppCategory> entry : KEYWORD_CATEGORY_MAP.entrySet()) {
            if (combined.contains(entry.getKey())) {
                matchedCategory = entry.getValue();
                break;
            }
        }
        if (matchedCategory == null) return Collections.emptyList();
        return appRepository
                .findSimilarApps(matchedCategory, -1L, PageRequest.of(0, 3))
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
 
    private ChatResponse smartFallbackResponse(String message) {
        String lower = message.toLowerCase();
        String responseText;
        List<AppResponse> apps = new ArrayList<>();
        AppCategory category = null;
 
        for (Map.Entry<String, AppCategory> entry : KEYWORD_CATEGORY_MAP.entrySet()) {
            if (lower.contains(entry.getKey())) {
                category = entry.getValue();
                break;
            }
        }
 
        if (lower.contains("hello") || lower.contains("hi")) {
            responseText = "Hi! I'm AppBot, your AppVerse AI assistant! What app are you looking for today?";
        } else if (lower.contains("recommend") || lower.contains("suggest") || lower.contains("find")) {
            responseText = category != null
                ? "Great choice! Here are some top " + category.name().replace("_", " ").toLowerCase() + " apps for you!"
                : "I'd love to help! Tell me what you need — productivity, gaming, education, health, finance, or music?";
        } else if (lower.contains("free")) {
            responseText = "We have many free apps! Browse the Marketplace and check the price filter.";
        } else if (lower.contains("how") && lower.contains("download")) {
            responseText = "To download: go to Marketplace, click any app, then hit the Download button. Sign in first!";
        } else if (lower.contains("publish") || lower.contains("developer")) {
            responseText = "Register as a Developer, go to Console, click New App, fill the form and submit for review!";
        } else {
            responseText = "I can help you find apps, answer questions about AppVerse, or recommend something new. What do you need?";
        }
 
        if (category != null) {
            final AppCategory finalCat = category;
            apps = appRepository
                    .findSimilarApps(finalCat, -1L, PageRequest.of(0, 3))
                    .stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        }
 
        return ChatResponse.builder()
                .message(responseText)
                .recommendedApps(apps)
                .hasRecommendations(!apps.isEmpty())
                .intent(detectIntent(message))
                .build();
    }
 
    private String detectIntent(String message) {
        String lower = message.toLowerCase();
        if (lower.contains("find") || lower.contains("recommend")) return "FIND_APP";
        if (lower.contains("how") || lower.contains("what")) return "QUESTION";
        if (lower.contains("best") || lower.contains("top")) return "TRENDING";
        if (lower.contains("hello") || lower.contains("hi")) return "GREETING";
        return "GENERAL";
    }
 
    private AppResponse mapToResponse(com.appverse.entity.App app) {
        return AppResponse.builder()
                .appId(app.getAppId())
                .name(app.getName())
                .tagline(app.getTagline())
                .category(app.getCategory())
                .price(app.getPrice())
                .averageRating(app.getAverageRating())
                .downloadCount(app.getDownloadCount())
                .status(app.getStatus())
                .developerId(app.getDeveloper().getUserId())
                .developerName(app.getDeveloper().getUsername())
                .build();
    }
}
 