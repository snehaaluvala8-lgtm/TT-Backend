package com.example.backend.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
			.csrf(csrf -> csrf.disable())
			.cors(Customizer.withDefaults())
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(authorize -> authorize
				.requestMatchers("/api/auth/**", "/api/stocks", "/api/stocks/**", "/api/health").permitAll()
				.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
				.anyRequest().authenticated())
			.oauth2ResourceServer(resourceServer -> resourceServer
				.jwt(Customizer.withDefaults())
				.authenticationEntryPoint((request, response, exception) -> {
					response.setStatus(401);
					response.setContentType(MediaType.APPLICATION_JSON_VALUE);
					response.getWriter().write(
						"{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"A valid bearer token is required.\"}"
					);
				}))
			.exceptionHandling(errors -> errors.accessDeniedHandler((request, response, exception) -> {
				response.setStatus(403);
				response.setContentType(MediaType.APPLICATION_JSON_VALUE);
				response.getWriter().write(
					"{\"status\":403,\"error\":\"Forbidden\",\"message\":\"You are not allowed to perform this request.\"}"
				);
			}))
			.build();
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource(
			@Value("${app.cors.allowed-origins}") String allowedOrigins) {
		List<String> origins = Arrays.stream(allowedOrigins.split(","))
			.map(String::trim)
			.filter(origin -> !origin.isEmpty())
			.toList();
		if (origins.isEmpty() || origins.contains("*")) {
			throw new IllegalStateException("CORS_ALLOWED_ORIGINS must list exact browser origins.");
		}

		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(origins);
		configuration.setAllowedMethods(List.of("GET", "POST", "OPTIONS"));
		configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
		configuration.setAllowCredentials(false);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	JwtEncoder jwtEncoder(@Value("${app.jwt.secret}") String encodedSecret) {
		return new NimbusJwtEncoder(new ImmutableSecret<>(jwtSigningKey(encodedSecret)));
	}

	@Bean
	JwtDecoder jwtDecoder(@Value("${app.jwt.secret}") String encodedSecret) {
		return NimbusJwtDecoder.withSecretKey(jwtSigningKey(encodedSecret))
			.macAlgorithm(MacAlgorithm.HS256)
			.build();
	}

	private SecretKey jwtSigningKey(String encodedSecret) {
		byte[] secretBytes;
		try {
			secretBytes = Base64.getDecoder().decode(encodedSecret);
		} catch (IllegalArgumentException exception) {
			throw new IllegalStateException("JWT_SECRET must be Base64-encoded.", exception);
		}
		if (secretBytes.length < 32) {
			throw new IllegalStateException("JWT_SECRET must decode to at least 32 bytes.");
		}
		return new SecretKeySpec(secretBytes, "HmacSHA256");
	}
}
