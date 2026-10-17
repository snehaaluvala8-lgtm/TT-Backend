package com.example.backend.controller;

import com.example.backend.dto.ApiDtos.AuthResponse;
import com.example.backend.dto.ApiDtos.LoginRequest;
import com.example.backend.dto.ApiDtos.RegisterRequest;
import com.example.backend.security.JwtTokenService.IssuedToken;
import com.example.backend.security.SessionCookie;
import com.example.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;
	private final boolean secureCookie;

	public AuthController(AuthService authService, @Value("${app.auth.cookie-secure:true}") boolean secureCookie) {
		this.authService = authService;
		this.secureCookie = secureCookie;
	}

	@PostMapping("/register")
	public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
		return authResponse(authService.register(request), HttpStatus.CREATED);
	}

	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
		return authResponse(authService.login(request), HttpStatus.OK);
	}

	@GetMapping("/me")
	public AuthResponse currentUser(@AuthenticationPrincipal Jwt jwt) {
		return new AuthResponse(jwt.getSubject());
	}

	@PostMapping("/logout")
	public ResponseEntity<Void> logout() {
		ResponseCookie cookie = ResponseCookie.from(SessionCookie.NAME, "")
			.httpOnly(true)
			.secure(secureCookie)
			.sameSite("Lax")
			.path("/api")
			.maxAge(0)
			.build();
		return ResponseEntity.noContent()
			.header(HttpHeaders.SET_COOKIE, cookie.toString())
			.build();
	}

	private ResponseEntity<AuthResponse> authResponse(IssuedToken issuedToken, HttpStatus status) {
		ResponseCookie cookie = ResponseCookie.from(SessionCookie.NAME, issuedToken.value())
			.httpOnly(true)
			.secure(secureCookie)
			.sameSite("Lax")
			.path("/api")
			.maxAge(issuedToken.expiresInSeconds())
			.build();
		return ResponseEntity.status(status)
			.header(HttpHeaders.SET_COOKIE, cookie.toString())
			.body(new AuthResponse(issuedToken.username()));
	}
}
