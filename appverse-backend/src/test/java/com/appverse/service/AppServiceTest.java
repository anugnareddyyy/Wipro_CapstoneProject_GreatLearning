package com.appverse.service;

import com.appverse.dto.request.AppRequest;
import com.appverse.dto.response.AppResponse;
import com.appverse.entity.*;
import com.appverse.exception.ResourceNotFoundException;
import com.appverse.exception.UnauthorizedActionException;
import com.appverse.repository.AppRepository;
import com.appverse.repository.DownloadRepository;
import com.appverse.repository.UserRepository;
import com.appverse.service.impl.AppServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AppServiceImpl}.
 *
 * Uses Mockito to mock all repository dependencies so tests run
 * without a real database. Each method group is organized into a
 * {@link Nested} class for clarity and maintainability.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AppService Unit Tests")
class AppServiceTest {

    @Mock
    private AppRepository appRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DownloadRepository downloadRepository;

    @InjectMocks
    private AppServiceImpl appService;

    // ===== TEST FIXTURES =====

    private User developerUser;
    private App sampleApp;
    private AppRequest appRequest;

    @BeforeEach
    void setUp() {
        developerUser = User.builder()
                .userId(1L)
                .username("devuser")
                .email("dev@test.com")
                .password("encoded_password")
                .role(Role.DEVELOPER)
                .isActive(true)
                .build();

        sampleApp = App.builder()
                .appId(10L)
                .name("Test App")
                .description("A comprehensive test application for unit testing")
                .tagline("Best test app")
                .category(AppCategory.PRODUCTIVITY)
                .price(BigDecimal.ZERO)
                .status(AppStatus.APPROVED)
                .averageRating(new BigDecimal("4.50"))
                .reviewCount(20)
                .downloadCount(500L)
                .isFeatured(false)
                .developer(developerUser)
                .build();

        appRequest = new AppRequest();
        appRequest.setName("New App");
        appRequest.setDescription("A brand new app for testing service layer operations");
        appRequest.setTagline("New and improved");
        appRequest.setCategory(AppCategory.EDUCATION);
        appRequest.setPrice(BigDecimal.valueOf(2.99));
        appRequest.setCurrentVersion("1.0.0");
    }

    // ===== GET APP BY ID =====

    @Nested
    @DisplayName("getAppById()")
    class GetAppById {

        @Test
        @DisplayName("should return AppResponse when app exists")
        void getAppById_WhenAppExists_ReturnsAppResponse() {
            when(appRepository.findById(10L)).thenReturn(Optional.of(sampleApp));

            AppResponse result = appService.getAppById(10L);

            assertThat(result).isNotNull();
            assertThat(result.getAppId()).isEqualTo(10L);
            assertThat(result.getName()).isEqualTo("Test App");
            assertThat(result.getCategory()).isEqualTo(AppCategory.PRODUCTIVITY);
            assertThat(result.getDeveloperName()).isEqualTo("devuser");

            verify(appRepository, times(1)).findById(10L);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when app does not exist")
        void getAppById_WhenAppNotFound_ThrowsResourceNotFoundException() {
            when(appRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> appService.getAppById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("App")
                    .hasMessageContaining("999");

            verify(appRepository, times(1)).findById(999L);
        }
    }

    // ===== CREATE APP =====

    @Nested
    @DisplayName("createApp()")
    class CreateApp {

        @Test
        @DisplayName("should create and return app with PENDING status")
        void createApp_WithValidRequest_CreatesAppWithPendingStatus() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(developerUser));

            App newApp = App.builder()
                    .appId(20L)
                    .name(appRequest.getName())
                    .description(appRequest.getDescription())
                    .category(appRequest.getCategory())
                    .price(appRequest.getPrice())
                    .status(AppStatus.PENDING)
                    .developer(developerUser)
                    .build();

            when(appRepository.save(any(App.class))).thenReturn(newApp);

            AppResponse result = appService.createApp(appRequest, 1L);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("New App");
            assertThat(result.getStatus()).isEqualTo(AppStatus.PENDING);

            verify(userRepository, times(1)).findById(1L);
            verify(appRepository, times(1)).save(any(App.class));
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when developer not found")
        void createApp_WhenDeveloperNotFound_ThrowsResourceNotFoundException() {
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> appService.createApp(appRequest, 999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User");
        }
    }

    // ===== UPDATE APP =====

    @Nested
    @DisplayName("updateApp()")
    class UpdateApp {

        @Test
        @DisplayName("should update app when developer owns it")
        void updateApp_WhenOwnerUpdates_ReturnsUpdatedResponse() {
            when(appRepository.findById(10L)).thenReturn(Optional.of(sampleApp));
            when(appRepository.save(any(App.class))).thenReturn(sampleApp);

            appRequest.setName("Updated App Name");
            AppResponse result = appService.updateApp(10L, appRequest, 1L);

            assertThat(result).isNotNull();
            verify(appRepository).save(any(App.class));
        }

        @Test
        @DisplayName("should throw UnauthorizedActionException when non-owner tries to update")
        void updateApp_WhenNotOwner_ThrowsUnauthorizedActionException() {
            when(appRepository.findById(10L)).thenReturn(Optional.of(sampleApp));

            // Developer ID 99 is not the owner (owner is ID 1)
            assertThatThrownBy(() -> appService.updateApp(10L, appRequest, 99L))
                    .isInstanceOf(UnauthorizedActionException.class)
                    .hasMessageContaining("owner");
        }
    }

    // ===== DELETE APP =====

    @Nested
    @DisplayName("deleteApp()")
    class DeleteApp {

        @Test
        @DisplayName("should delete app when developer owns it")
        void deleteApp_WhenOwner_DeletesSuccessfully() {
            when(appRepository.findById(10L)).thenReturn(Optional.of(sampleApp));

            assertThatCode(() -> appService.deleteApp(10L, 1L))
                    .doesNotThrowAnyException();

            verify(appRepository, times(1)).delete(sampleApp);
        }

        @Test
        @DisplayName("should throw UnauthorizedActionException when non-owner tries to delete")
        void deleteApp_WhenNotOwner_ThrowsUnauthorizedActionException() {
            when(appRepository.findById(10L)).thenReturn(Optional.of(sampleApp));

            assertThatThrownBy(() -> appService.deleteApp(10L, 55L))
                    .isInstanceOf(UnauthorizedActionException.class);

            verify(appRepository, never()).delete(any());
        }
    }

    // ===== MARKETPLACE LISTING =====

    @Nested
    @DisplayName("getMarketplaceApps()")
    class GetMarketplaceApps {

        @Test
        @DisplayName("should return page of approved apps when no category filter applied")
        void getMarketplaceApps_NoFilter_ReturnsApprovedApps() {
            Pageable pageable = PageRequest.of(0, 12);
            Page<App> appPage = new PageImpl<>(List.of(sampleApp));

            when(appRepository.findByStatus(AppStatus.APPROVED, pageable)).thenReturn(appPage);

            Page<AppResponse> result = appService.getMarketplaceApps(null, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getName()).isEqualTo("Test App");
        }

        @Test
        @DisplayName("should filter by category when category is provided")
        void getMarketplaceApps_WithCategoryFilter_ReturnsFilteredApps() {
            Pageable pageable = PageRequest.of(0, 12);
            Page<App> appPage = new PageImpl<>(List.of(sampleApp));

            when(appRepository.findByStatusAndCategory(
                AppStatus.APPROVED, AppCategory.PRODUCTIVITY, pageable))
                .thenReturn(appPage);

            Page<AppResponse> result = appService.getMarketplaceApps(AppCategory.PRODUCTIVITY, pageable);

            assertThat(result.getContent()).hasSize(1);
            verify(appRepository).findByStatusAndCategory(
                AppStatus.APPROVED, AppCategory.PRODUCTIVITY, pageable);
        }
    }

    // ===== DEVELOPER APPS =====

    @Nested
    @DisplayName("getDeveloperApps()")
    class GetDeveloperApps {

        @Test
        @DisplayName("should return all apps for a given developer")
        void getDeveloperApps_ReturnsAllDeveloperApps() {
            when(appRepository.findByDeveloper_UserId(1L)).thenReturn(List.of(sampleApp));

            List<AppResponse> result = appService.getDeveloperApps(1L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getDeveloperId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("should return empty list when developer has no apps")
        void getDeveloperApps_WhenNoApps_ReturnsEmptyList() {
            when(appRepository.findByDeveloper_UserId(2L)).thenReturn(List.of());

            List<AppResponse> result = appService.getDeveloperApps(2L);

            assertThat(result).isEmpty();
        }
    }

    // ===== ADMIN: UPDATE STATUS =====

    @Nested
    @DisplayName("updateAppStatus()")
    class UpdateAppStatus {

        @Test
        @DisplayName("should change app status when admin approves")
        void updateAppStatus_WhenAdminApproves_ChangesStatusToApproved() {
            App pendingApp = App.builder()
                    .appId(10L)
                    .name("Pending App")
                    .description("Waiting for admin approval with enough text")
                    .status(AppStatus.PENDING)
                    .developer(developerUser)
                    .category(AppCategory.UTILITIES)
                    .build();

            when(appRepository.findById(10L)).thenReturn(Optional.of(pendingApp));
            when(appRepository.save(any(App.class))).thenAnswer(inv -> inv.getArgument(0));

            AppResponse result = appService.updateAppStatus(10L, AppStatus.APPROVED, 99L);

            assertThat(result.getStatus()).isEqualTo(AppStatus.APPROVED);
            verify(appRepository).save(any(App.class));
        }
    }
}
