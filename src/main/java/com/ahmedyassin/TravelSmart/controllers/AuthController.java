package com.ahmedyassin.TravelSmart.controllers;

import com.ahmedyassin.TravelSmart.Security.JwtService;
import com.ahmedyassin.TravelSmart.dto.LoginRequest;
import com.ahmedyassin.TravelSmart.dto.LoginResponse;
import com.ahmedyassin.TravelSmart.services.AppUserDetailsService;
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
    private final AppUserDetailsService userDetailsService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            String token = jwtService.generateToken(userDetails);
            return ResponseEntity.ok(new LoginResponse(token, userDetails.getUsername()));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body(new LoginResponse(null, null, "Incorrect username or password"));
        }
    }
}