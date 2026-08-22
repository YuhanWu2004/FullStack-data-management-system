package com.cicad.app.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Turns exceptions into statuses a client can act on. Without this every failure — a
 * refused permission, a bad GPA, a missing row — arrives as an identical 500 with the
 * message stripped, so the frontend cannot tell them apart.
 * <p>
 * The {@link AccessDeniedException} mapping is what makes the authorization rules usable:
 * a denied {@code @PreAuthorize} has to read as 403, not as a server error.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException e) {
		return body(HttpStatus.FORBIDDEN, "You do not have permission to do that");
	}

	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<Map<String, Object>> handleAuthentication(AuthenticationException e) {
		return body(HttpStatus.UNAUTHORIZED, "Please sign in");
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException e) {
		return body(HttpStatus.BAD_REQUEST, e.getMessage());
	}

	/** A body Jackson cannot read is the caller's mistake, not a server fault. */
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<Map<String, Object>> handleUnreadableBody(HttpMessageNotReadableException e) {
		return body(HttpStatus.BAD_REQUEST, "Request body is missing or malformed");
	}

	/**
	 * The services signal validation failures by throwing {@code RuntimeException} itself
	 * with a human-readable message ("Grade must be between 0 and 100"). Those become 400s
	 * so the UI can show the real reason.
	 * <p>
	 * Anything more specific — an NPE, an IllegalStateException, a JPA failure — is a bug
	 * or an outage, not user error, so it stays a 500 and gets logged. Reporting a genuine
	 * NPE as "400 Bad Request" would send you looking at the caller instead of the code.
	 * The better end state is typed exceptions or bean validation in the services; this
	 * handler makes the current style behave correctly in the meantime.
	 */
	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<Map<String, Object>> handleRuntime(RuntimeException e) {
		boolean isHandWrittenValidation = e.getClass() == RuntimeException.class;
		String message = e.getMessage();

		if (isHandWrittenValidation && message != null && !message.isBlank()) {
			return body(HttpStatus.BAD_REQUEST, message);
		}

		log.error("Unhandled exception serving request", e);
		return body(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong");
	}

	private ResponseEntity<Map<String, Object>> body(HttpStatus status, String message) {
		return ResponseEntity.status(status).body(Map.of(
				"status", status.value(),
				"message", message == null ? status.getReasonPhrase() : message));
	}
}
