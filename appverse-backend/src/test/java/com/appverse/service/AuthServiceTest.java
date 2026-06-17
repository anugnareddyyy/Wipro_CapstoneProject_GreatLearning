package com.appverse.service;

import com.appverse.dto.request.LoginRequest;
import com.appverse.dto.request.RegisterRequest;
import com.appverse.dto.response.AuthResponse;
import com.appverse.entity.Role;
import com.appverse.entity.User;
import com.appverse.exception.DuplicateResourceException;
import com.appverse.repository.UserRepository;
import com.appverse.security.JwtUtil;
import com.appverse.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AuthServiceImpl}.
 *
 * Covers:
 * - Successful registration with JWT response
 * - Duplicate email/username detection
 * - Role assignment (USER vs DEVELOPER)
 * - Successful login
 * - Failed login with bad credentials
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest registerRequest;
    private User savedUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("testdev");
        registerRequest.setEmail("testdev@example.com");
        registerRequest.setPassword("SecurePass123");
        registerRequest.setFullName("Test Developer");
        registerRequest.setRole("USER");

        savedUser = User.builder()
                .userId(1L)
                .username("testdev")
                .email("testdev@example.com")
                .password("$2a$10$hashedpassword")
                .fullName("Test Developer")
                .role(Role.USER)
                .isActive(true)
                .build();
    }

    @Nested
    @DisplayName("register()")
    class Register {

        @Test
        @DisplayName("should register user and return JWT when credentials are unique")
        void register_WithUniqueCredentials_ReturnsAuthResponse() {
            when(userRepository.existsByEmail("testdev@example.com")).thenReturn(false);
            when(userRepository.existsByUsername("testdev")).thenReturn(false);
            when(passwordEncoder.encode("SecurePass123")).thenReturn("$2a$10$hashed");
            when(userRepository.save(any(User.class))).thenReturn(savedUser);
            when(jwtUtil.generateToken(any(UserDetails.class))).thenReturn("mock.jwt.token");
            when(jwtUtil.getExpirationTime()).thenReturn(86400000L);

            AuthResponse result = authService.register(registerRequest);

            assertThat(result).isNotNull();
            assertThat(result.getToken()).isEqualTo("mock.jwt.token");
            assertThat(result.getEmail()).isEqualTo("testdev@example.com");
            assertThat(result.getRole()).isEqualTo("USER");
            assertThat(result.getUserId()).isEqualTo(1L);

            verify(userRepository).save(any(User.class));
            verify(passwordEncoder).encode("SecurePass123");
        }

        @Test
        @DisplayName("should throw DuplicateResourceException when email already exists")
        void register_WhenEmailExists_ThrowsDuplicateResourceException() {
            when(userRepository.existsByEmail("testdev@example.com")).thenReturn(true);

            assertThatThrownBy(() -> authService.register(registerRequest))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("testdev@example.com");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw DuplicateResourceException when username already taken")
        void register_WhenUsernameExists_ThrowsDuplicateResourceException() {
            when(userRepository.existsByEmail(any())).thenReturn(false);
            when(userRepository.existsByUsername("testdev")).thenReturn(true);

            assertThatThrownBy(() -> authService.register(registerRequest))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("testdev");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("should assign DEVELOPER role when requested")
        void register_WithDeveloperRole_AssignesDeveloperRole() {
            registerRequest.setRole("DEVELOPER");

            when(userRepository.existsByEmail(any())).thenReturn(false);
            when(userRepository.existsByUsername(any())).thenReturn(false);
            when(passwordEncoder.encode(any())).thenReturn("hashed");

            User devUser = savedUser.toBuilder().role(Role.DEVELOPER).build();
            when(userRepository.save(any(User.class))).thenReturn(devUser);
            when(jwtUtil.generateToken(any(UserDetails.class))).thenReturn("token");
            when(jwtUtil.getExpirationTime()).thenReturn(86400000L);

            AuthResponse result = authService.register(registerRequest);

            assertThat(result.getRole()).isEqualTo("DEVELOPER");
        }

        @Test
        @DisplayName("should default to USER role when invalid role is provided")
        void register_WithInvalidRole_DefaultsToUserRole() {
            registerRequest.setRole("SUPERADMIN"); // invalid role

            when(userRepository.existsByEmail(any())).thenReturn(false);
            when(userRepository.existsByUsername(any())).thenReturn(false);
            when(passwordEncoder.encode(any())).thenReturn("hashed");
            when(userRepository.save(any(User.class))).thenReturn(savedUser); // USER role
            when(jwtUtil.generateToken(any(UserDetails.class))).thenReturn("token");
            when(jwtUtil.getExpirationTime()).thenReturn(86400000L);

            AuthResponse result = authService.register(registerRequest);

            assertThat(result.getRole()).isEqualTo("USER");
        }
    }

    @Nested
    @DisplayName("login()")
    class Login {

        @Test
        @DisplayName("should return JWT when credentials are valid")
        void login_WithValidCredentials_ReturnsAuthResponse() {
            LoginRequest loginReq = new LoginRequest();
            loginReq.setEmail("testdev@example.com");
            loginReq.setPassword("SecurePass123");

            Authentication mockAuth = mock(Authentication.class);
            UserDetails mockDetails = org.springframework.security.core.userdetails.User
                    .withUsername("testdev@example.com")
                    .password("hashed")
                    .roles("USER")
                    .build();

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(mockAuth);
            when(mockAuth.getPrincipal()).thenReturn(mockDetails);
            when(jwtUtil.generateToken(mockDetails)).thenReturn("login.jwt.token");
            when(jwtUtil.getExpirationTime()).thenReturn(86400000L);
            when(userRepository.findByEmail("testdev@example.com")).thenReturn(Optional.of(savedUser));

            AuthResponse result = authService.login(loginReq);

            assertThat(result.getToken()).isEqualTo("login.jwt.token");
            assertThat(result.getEmail()).isEqualTo("testdev@example.com");
            verify(authenticationManager).authenticate(any());
        }

        @Test
        @DisplayName("should throw BadCredentialsException when password is wrong")
        void login_WithWrongPassword_ThrowsBadCredentialsException() {
            LoginRequest loginReq = new LoginRequest();
            loginReq.setEmail("testdev@example.com");
            loginReq.setPassword("WrongPassword");

            when(authenticationManager.authenticate(any()))
                    .thenThrow(new BadCredentialsException("Invalid credentials"));

            assertThatThrownBy(() -> authService.login(loginReq))
                    .isInstanceOf(BadCredentialsException.class);
        }
    }
}
