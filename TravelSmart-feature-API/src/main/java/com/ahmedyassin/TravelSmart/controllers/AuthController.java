package com.ahmedyassin.TravelSmart.controllers;

import com.ahmedyassin.TravelSmart.Security.JwtService;
import com.ahmedyassin.TravelSmart.dto.LoginRequest;
import com.ahmedyassin.TravelSmart.dto.LoginResponse;
import com.ahmedyassin.TravelSmart.dto.RegisterRequest;
import com.ahmedyassin.TravelSmart.dto.PasswordResetRequest;
import com.ahmedyassin.TravelSmart.dto.PasswordResetConfirmRequest;
import com.ahmedyassin.TravelSmart.entities.User;
import com.ahmedyassin.TravelSmart.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            String token = jwtService.generateToken(userDetails);

            // Get user role from authorities
            String role = userDetails.getAuthorities().stream()
                    .findFirst()
                    .map(auth1 -> auth1.getAuthority())
                    .orElse("ROLE_USER");

            return ResponseEntity.ok(new LoginResponse(token, userDetails.getUsername(), role));
        } catch (BadCredentialsException e) {
            LoginResponse errorResponse = new LoginResponse();
            errorResponse.setMessage("Incorrect username or password");
            return ResponseEntity.status(401).body(errorResponse);
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<LoginResponse> signup(@RequestBody RegisterRequest request) {
        // Build User entity from register request
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());

        // Create user (will encode password)
        userService.createUser(user);

        // Authenticate newly created user and return token
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        String token = jwtService.generateToken(userDetails);

        // Get user role from authorities
        String role = userDetails.getAuthorities().stream()
                .findFirst()
                .map(auth1 -> auth1.getAuthority())
                .orElse("ROLE_USER");

        return ResponseEntity.ok(new LoginResponse(token, userDetails.getUsername(), role));
    }

    @PostMapping("/request-password-reset")
    public ResponseEntity<?> requestPasswordReset(@RequestBody PasswordResetRequest request) {
        // Generate token and send email. Do not return token in the response for
        // security.
        userService.requestPasswordReset(request.getEmail());
        return ResponseEntity.ok().body(java.util.Map.of("message",
                "If an account with that email exists, a password reset link has been sent."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody PasswordResetConfirmRequest request) {
        userService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok().body(java.util.Map.of("message", "Password has been reset successfully."));
    }
}