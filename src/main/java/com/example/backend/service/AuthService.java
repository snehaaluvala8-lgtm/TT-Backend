package com.example.backend.service;

import com.example.backend.dto.ApiDtos.AuthResponse;
import com.example.backend.dto.ApiDtos.LoginRequest;
import com.example.backend.dto.ApiDtos.RegisterRequest;
import com.example.backend.model.AppUser;
import com.example.backend.repository.AppUserRepository;
import com.example.backend.security.JwtTokenService;
import java.math.BigDecimal;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

	private static final BigDecimal STARTING_BALANCE = new BigDecimal("50000.00");

	private final AppUserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenService tokenService;

	public AuthService(AppUserRepository userRepository, PasswordEncoder passwordEncoder,
			JwtTokenService tokenService) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.tokenService = tokenService;
	}

	@Transactional
	public AuthResponse register(RegisterRequest request) {
		String username = request.username().trim();
		String email = request.email().trim().toLowerCase(Locale.ROOT);
		if (userRepository.existsByUsernameIgnoreCase(username)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "That username is already registered.");
		}
		if (userRepository.existsByEmailIgnoreCase(email)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "That email address is already registered.");
		}

		AppUser user = new AppUser(username, email, passwordEncoder.encode(request.password()), STARTING_BALANCE);
		userRepository.save(user);
		return tokenService.issueToken(username);
	}

	@Transactional(readOnly = true)
	public AuthResponse login(LoginRequest request) {
		String identifier = request.identifier().trim();
		AppUser user = userRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase(identifier, identifier)
			.orElseThrow(this::invalidCredentials);
		if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
			throw invalidCredentials();
		}
		return tokenService.issueToken(user.getUsername());
	}

	private ResponseStatusException invalidCredentials() {
		return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username/email or password.");
	}
}
