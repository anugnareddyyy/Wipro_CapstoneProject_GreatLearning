package com.appverse.service;
 
import com.appverse.dto.request.ChatRequest;
import com.appverse.dto.response.ChatResponse;
import com.appverse.entity.App;
import com.appverse.entity.AppCategory;
import com.appverse.entity.AppStatus;
import com.appverse.entity.Role;
import com.appverse.entity.User;
import com.appverse.repository.AppRepository;
import com.appverse.service.impl.ChatbotServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
 
import java.util.List;
import java.util.Map;
 
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
 
@ExtendWith(MockitoExtension.class)
@DisplayName("ChatbotService Unit Tests")
class ChatbotServiceTest {
 
    @Mock private AppRepository appRepository;
    @Mock private RestTemplate restTemplate;
 
    @InjectMocks
    private ChatbotServiceImpl chatbotService;
 
    private User developer;
    private App productivityApp;
 
    @BeforeEach
    void setUp() {
        developer = User.builder().userId(1L).username("dev1").role(Role.DEVELOPER).isActive(true).build();
 
        productivityApp = App.builder()
                .appId(1L)
                .name("TaskMaster")
                .description("A productivity app to manage your daily tasks efficiently")
                .tagline("Get things done")
                .category(AppCategory.PRODUCTIVITY)
                .status(AppStatus.APPROVED)
                .developer(developer)
                .build();
    }
 
    @Nested
    @DisplayName("chat() — no API key configured")
    class NoApiKeyConfigured {
 
        @Test
        @DisplayName("should use fallback response when Gemini API key is blank")
        void chat_NoApiKey_UsesFallbackResponse() {
            ReflectionTestUtils.setField(chatbotService, "geminiApiKey", "");
 
            ChatRequest request = new ChatRequest();
            request.setMessage("Hello there!");
 
            ChatResponse response = chatbotService.chat(request, 1L);
 
            assertThat(response.getMessage()).contains("AppBot");
            assertThat(response.getIntent()).isEqualTo("GREETING");
        }
 
        @Test
        @DisplayName("should recommend apps in fallback when message matches a known category keyword")
        void chat_NoApiKey_RecommendMessage_ReturnsCategoryApps() {
            ReflectionTestUtils.setField(chatbotService, "geminiApiKey", null);
 
            when(appRepository.findSimilarApps(eq(AppCategory.PRODUCTIVITY), eq(-1L), any()))
                    .thenReturn(List.of(productivityApp));
 
            ChatRequest request = new ChatRequest();
            request.setMessage("Can you recommend a productivity app?");
 
            ChatResponse response = chatbotService.chat(request, 1L);
 
            assertThat(response.getHasRecommendations()).isTrue();
            assertThat(response.getRecommendedApps()).hasSize(1);
            assertThat(response.getIntent()).isEqualTo("FIND_APP");
        }
 
        @Test
        @DisplayName("should return no recommendations when no keyword category matches")
        void chat_NoApiKey_NoKeywordMatch_NoRecommendations() {
            ReflectionTestUtils.setField(chatbotService, "geminiApiKey", "");
 
            ChatRequest request = new ChatRequest();
            request.setMessage("xyz random gibberish");
 
            ChatResponse response = chatbotService.chat(request, 1L);
 
            assertThat(response.getHasRecommendations()).isFalse();
            assertThat(response.getRecommendedApps()).isEmpty();
        }
    }
 
    @Nested
    @DisplayName("chat() — Gemini API configured")
    class GeminiApiConfigured {
 
        @Test
        @DisplayName("should return Gemini-generated text when the API call succeeds")
        @SuppressWarnings("unchecked")
        void chat_WithApiKey_ReturnsGeminiResponse() {
            ReflectionTestUtils.setField(chatbotService, "geminiApiKey", "fake-api-key");
 
            Page<App> appsPage = new PageImpl<>(List.of(productivityApp));
            when(appRepository.findByStatus(eq(AppStatus.APPROVED), any(PageRequest.class)))
                    .thenReturn(appsPage);
 
            Map<String, Object> part = Map.of("text", "Try TaskMaster for productivity!");
            Map<String, Object> content = Map.of("parts", List.of(part));
            Map<String, Object> candidate = Map.of("content", content);
            Map<String, Object> geminiBody = Map.of("candidates", List.of(candidate));
 
            when(restTemplate.exchange(
                    anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                    .thenReturn(new ResponseEntity<>(geminiBody, org.springframework.http.HttpStatus.OK));
 
            when(appRepository.findSimilarApps(any(), any(), any())).thenReturn(List.of());
 
            ChatRequest request = new ChatRequest();
            request.setMessage("What's a good productivity app?");
 
            ChatResponse response = chatbotService.chat(request, 1L);
 
            assertThat(response.getMessage()).isEqualTo("Try TaskMaster for productivity!");
        }
 
        @Test
        @DisplayName("should fall back gracefully when the Gemini API call throws an exception")
        void chat_GeminiApiThrows_FallsBackGracefully() {
            ReflectionTestUtils.setField(chatbotService, "geminiApiKey", "fake-api-key");
 
            when(appRepository.findByStatus(eq(AppStatus.APPROVED), any(PageRequest.class)))
                    .thenReturn(new PageImpl<>(List.of()));
 
            when(restTemplate.exchange(
                    anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                    .thenThrow(new RuntimeException("Network error"));
 
            ChatRequest request = new ChatRequest();
            request.setMessage("hello");
 
            ChatResponse response = chatbotService.chat(request, 1L);
 
            assertThat(response).isNotNull();
            assertThat(response.getMessage()).contains("AppBot");
        }
 
        @Test
        @DisplayName("should fall back when Gemini response has no candidates")
        void chat_GeminiEmptyCandidates_FallsBack() {
            ReflectionTestUtils.setField(chatbotService, "geminiApiKey", "fake-api-key");
 
            when(appRepository.findByStatus(eq(AppStatus.APPROVED), any(PageRequest.class)))
                    .thenReturn(new PageImpl<>(List.of()));
 
            Map<String, Object> emptyBody = Map.of("candidates", List.of());
            when(restTemplate.exchange(
                    anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                    .thenReturn(new ResponseEntity<>(emptyBody, org.springframework.http.HttpStatus.OK));
 
            ChatRequest request = new ChatRequest();
            request.setMessage("hi");
 
            ChatResponse response = chatbotService.chat(request, 1L);
 
            assertThat(response.getMessage()).contains("AppBot");
        }
    }
 
    @Nested
    @DisplayName("intent detection")
    class IntentDetection {
 
        @Test
        @DisplayName("should detect TRENDING intent for 'best'/'top' keywords")
        void chat_BestKeyword_DetectsTrendingIntent() {
            ReflectionTestUtils.setField(chatbotService, "geminiApiKey", "");
 
            ChatRequest request = new ChatRequest();
            request.setMessage("Top apps trending right now please");
 
            ChatResponse response = chatbotService.chat(request, 1L);
 
            assertThat(response.getIntent()).isEqualTo("TRENDING");
        }
 
        @Test
        @DisplayName("should detect QUESTION intent for 'how'/'what' keywords without find/recommend")
        void chat_HowToDownload_DetectsQuestionIntent() {
            ReflectionTestUtils.setField(chatbotService, "geminiApiKey", "");
 
            ChatRequest request = new ChatRequest();
            request.setMessage("How do I download an app?");
 
            ChatResponse response = chatbotService.chat(request, 1L);
 
            assertThat(response.getIntent()).isEqualTo("QUESTION");
            assertThat(response.getMessage()).contains("Marketplace");
        }
 
        @Test
        @DisplayName("should fall back to GENERAL intent for unrecognized phrasing")
        void chat_UnrecognizedPhrasing_DetectsGeneralIntent() {
            ReflectionTestUtils.setField(chatbotService, "geminiApiKey", "");
 
            ChatRequest request = new ChatRequest();
            request.setMessage("zzz qwerty random unrelated text");
 
            ChatResponse response = chatbotService.chat(request, 1L);
 
            assertThat(response.getIntent()).isEqualTo("GENERAL");
        }
    }
}
 