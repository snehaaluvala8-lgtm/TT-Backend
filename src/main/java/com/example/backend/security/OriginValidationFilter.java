package com.example.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpMethod;
import org.springframework.web.filter.OncePerRequestFilter;

public class OriginValidationFilter extends OncePerRequestFilter {

	private final List<String> allowedOrigins;

	public OriginValidationFilter(List<String> allowedOrigins) {
		this.allowedOrigins = List.copyOf(allowedOrigins);
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String origin = request.getHeader("Origin");
		if (isUnsafeMethod(request.getMethod())
				&& (origin == null || !allowedOrigins.contains(origin))) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			response.setContentType("application/json");
			response.getWriter().write(
				"{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Request origin is not allowed.\"}"
			);
			return;
		}
		filterChain.doFilter(request, response);
	}

	private boolean isUnsafeMethod(String method) {
		return !HttpMethod.GET.matches(method)
			&& !HttpMethod.HEAD.matches(method)
			&& !HttpMethod.OPTIONS.matches(method)
			&& !HttpMethod.TRACE.matches(method);
	}
}
