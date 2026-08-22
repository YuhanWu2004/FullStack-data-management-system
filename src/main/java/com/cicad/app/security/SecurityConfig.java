package com.cicad.app.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

/**
 * Session-based authentication. Spring already serves the SPA shell, so the browser and
 * the API share an origin — which means a session cookie needs no token storage on the
 * client and CSRF protection is available for free. A JWT would only start to earn its
 * keep if the frontend later moved to a host of its own.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

	/** Both the collection path (POST) and the item path (PUT/DELETE) of every resource. */
	private static final String[] WRITABLE_RESOURCES = {
			"/api/student", "/api/student/**",
			"/api/course", "/api/course/**",
			"/api/program", "/api/program/**",
			"/api/professor", "/api/professor/**",
			"/api/enrollment", "/api/enrollment/**",
			"/api/assignment", "/api/assignment/**",
			"/api/term", "/api/term/**"
	};

	/** Same list minus enrollment, whose PUT is the professor grading path. */
	private static final String[] STAFF_ONLY_UPDATES = {
			"/api/student", "/api/student/**",
			"/api/course", "/api/course/**",
			"/api/program", "/api/program/**",
			"/api/professor", "/api/professor/**",
			"/api/assignment", "/api/assignment/**",
			"/api/term", "/api/term/**"
	};

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

		// The SPA reads the CSRF token out of a cookie and echoes it in a header. The
		// default handler XORs the token per-request for BREACH hardening, which makes the
		// cookie value and the expected header value differ; setting the attribute name to
		// null opts back into a stable token so the round trip works. The BREACH vector it
		// gives up needs the token reflected in a compressed response body, which never
		// happens here — the token only ever travels in headers.
		CsrfTokenRequestAttributeHandler csrfRequestHandler = new CsrfTokenRequestAttributeHandler();
		csrfRequestHandler.setCsrfRequestAttributeName(null);

		http
				.csrf(csrf -> csrf
						.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
						.csrfTokenRequestHandler(csrfRequestHandler))

				.authorizeHttpRequests(auth -> auth
						// SPA shell and its assets, plus the two endpoints the login screen
						// itself has to reach before a session exists.
						.requestMatchers("/", "/error", "/favicon.ico").permitAll()
						.requestMatchers("/assets/**", "/js/**").permitAll()
						.requestMatchers("/api/auth/me", "/api/auth/login").permitAll()

						// Writes are a registrar action. Stated here as well as with
						// @PreAuthorize on the handlers, because the filter chain runs
						// before Spring MVC parses the request body: without this, a
						// caller who was never allowed to POST at all gets a 500 out of
						// the deserializer instead of a clean 403, and learns the body
						// shape was wrong rather than that they were refused.
						.requestMatchers(HttpMethod.POST, WRITABLE_RESOURCES).hasRole("STAFF")
						.requestMatchers(HttpMethod.DELETE, WRITABLE_RESOURCES).hasRole("STAFF")
						.requestMatchers(HttpMethod.PUT, STAFF_ONLY_UPDATES).hasRole("STAFF")

						// The one write that is not staff-only: setting a grade. Any signed-in
						// caller may reach it, and the handler's @PreAuthorize narrows that to
						// staff or the professor who teaches the course in question.
						.requestMatchers(HttpMethod.PUT, "/api/enrollment", "/api/enrollment/**")
						.authenticated()

						// Everything else behind the API is for signed-in callers. Which
						// signed-in callers is decided per endpoint by @PreAuthorize.
						.requestMatchers("/api/**").authenticated()
						.anyRequest().permitAll())

				.formLogin(form -> form
						.loginProcessingUrl("/api/auth/login")
						.successHandler((req, res, auth) -> res.setStatus(HttpStatus.NO_CONTENT.value()))
						.failureHandler((req, res, ex) -> {
							res.setStatus(HttpStatus.UNAUTHORIZED.value());
							res.setContentType(MediaType.APPLICATION_JSON_VALUE);
							res.getWriter().write("{\"message\":\"Incorrect username or password\"}");
						}))

				.logout(logout -> logout
						.logoutUrl("/api/auth/logout")
						.logoutSuccessHandler((req, res, auth) ->
								res.setStatus(HttpStatus.NO_CONTENT.value()))
						.deleteCookies("JSESSIONID"))

				.exceptionHandling(ex -> ex
						// An API client gets a status it can act on, never a redirect to a
						// login page it cannot render.
						.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
						.accessDeniedHandler((req, res, denied) -> {
							res.setStatus(HttpServletResponse.SC_FORBIDDEN);
							res.setContentType(MediaType.APPLICATION_JSON_VALUE);
							res.getWriter().write(
									"{\"message\":\"You do not have permission to do that\"}");
						}));

		return http.build();
	}
}
