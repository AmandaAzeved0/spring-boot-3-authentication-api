package com.authapi.security.auth;

import com.authapi.security.auth.AuthenticationRequest;
import com.authapi.security.auth.AuthenticationResponse;
import com.authapi.security.auth.AuthenticationService;
import com.authapi.security.auth.RegisterRequest;
import com.authapi.security.config.JwtService;
import com.authapi.security.user.User;
import com.authapi.security.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class AuthenticationServiceTest {

    @InjectMocks
    private AuthenticationService authenticationService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testRegister() {
        RegisterRequest request = new RegisterRequest();
        request.setName("test");
        request.setEmail("test@test.com");
        request.setPassword("password");

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());

        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtService.generateToken(any(User.class))).thenReturn("token");

        AuthenticationResponse response = authenticationService.register(request);

        assertEquals("token", response.getToken());
    }

    @Test
    public void testLogin() {
        AuthenticationRequest request = new AuthenticationRequest();
        request.setEmail("test@test.com");
        request.setPassword("password");

        User user = new User();
        user.setName("test");
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());

        when(userRepository.findByEmail(any(String.class))).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any(User.class))).thenReturn("token");

        AuthenticationResponse response = authenticationService.login(request);

        assertEquals("token", response.getToken());
    }

    @Test
    public void testLoginWithNonExistentEmailOrPassword() {
        AuthenticationRequest request = new AuthenticationRequest();
        request.setEmail("nonexistent@test.com");
        request.setPassword("wrongpassword");

        when(userRepository.findByEmail(any(String.class))).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> {
            authenticationService.login(request);
        });
    }
}