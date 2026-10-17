package com.example.backend.security;

import com.example.backend.dto.ApiDtos.AuthResponse;
import java.time.Duration;
import java.time.Instant;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {

	private static final Duration TOKEN_LIFETIME = Duration.ofHours(1);

	private final JwtEncoder jwtEncoder;

	public JwtTokenService(JwtEncoder jwtEncoder) {
		this.jwtEncoder = jwtEncoder;
	}

	public AuthResponse issueToken(String username) {
		Instant issuedAt = Instant.now();
		Instant expiresAt = issuedAt.plus(TOKEN_LIFETIME);
		JwtClaimsSet claims = JwtClaimsSet.builder()
			.subject(username)
			.issuedAt(issuedAt)
			.expiresAt(expiresAt)
			.build();
		JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
		String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
		return new AuthResponse(token, "Bearer", TOKEN_LIFETIME.toSeconds(), username);
	}
}
