package com.cicad.app.controller;

import com.cicad.app.security.AppUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The frontend's single source of truth for who it is talking as. It replaces the old
 * role-selection screen: the browser no longer announces its role, it asks.
 * <p>
 * Login and logout are handled by the Spring Security filter chain at
 * {@code POST /api/auth/login} and {@code POST /api/auth/logout} — see
 * {@link com.cicad.app.security.SecurityConfig}.
 */
@RestController
@RequestMapping("api/auth")
public class AuthController {

	/**
	 * Public on purpose. The login screen has to be able to call something before a session
	 * exists — both to learn that nobody is signed in, and to be issued the CSRF cookie
	 * that the login POST will need. Taking {@link CsrfToken} as a parameter is what forces
	 * that cookie to be written.
	 */
	@GetMapping("/me")
	public Map<String, Object> me(@AuthenticationPrincipal AppUserDetails principal,
	                              CsrfToken csrfToken) {
		Map<String, Object> body = new HashMap<>();
		if (principal == null) {
			body.put("authenticated", false);
			body.put("roles", List.of());
			return body;
		}
		body.put("authenticated", true);
		body.put("username", principal.getUsername());
		body.put("displayName", principal.getDisplayName());
		body.put("roles", principal.getRoleNames());
		body.put("studentId", principal.getStudentId());
		body.put("professorId", principal.getProfessorId());
		return body;
	}
}
