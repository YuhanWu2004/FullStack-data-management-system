package com.cicad.app.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The permission matrix, asserted against a running server over real HTTP.
 *
 * Signs in with the seeded accounts and checks what each of them can and cannot reach. The
 * refusals are the point: a future change that drops an annotation, widens a path pattern,
 * or reorders the filter chain shows up here as a 200 where a 403 belongs.
 * <p>
 * This drives real HTTP rather than MockMvc because Jetty 11 pins this project to the
 * Servlet 5 API, which Spring's request mocks cannot run on — see the comment on
 * {@code jakarta-servlet.version} in pom.xml.
 * <p>
 * It needs the application database, so it is skipped unless {@code db.url} is set:
 * <pre>mvn test -Ddb.url="jdbc:sqlserver://…;databaseName=APP_DB"</pre>
 * The ownership rules themselves are covered without a database by {@link AccessGuardTest}.
 * Almost nothing here writes: every request expected to succeed is a read, and every write
 * is expected to be refused — the one exception is the staff term-creation test, which
 * creates a uniquely-named row and deletes it again in a {@code finally} block.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@EnabledIfSystemProperty(named = "db.url", matches = ".+",
		disabledReason = "needs the application database; pass -Ddb.url=…")
class ApiAuthorizationIT {

	/** The password the seed changeset gives every demo account. */
	private static final String SEED_PASSWORD = "changeit";

	@Autowired
	private TestRestTemplate rest;

	private final ObjectMapper json = new ObjectMapper();

	private Session staff;
	private Session student;
	private Session professor;

	private int myStudentId;
	private int anotherStudentId;
	private int myProfessorId;
	private int anotherProfessorId;

	@BeforeAll
	void signInEveryone() throws Exception {
		// The JDK's HttpURLConnection wants to retry a POST that comes back 401, and cannot
		// when the body was streamed ("cannot retry due to server authentication, in
		// streaming mode"). Buffering the body lets the 401 surface as a normal response,
		// which the wrong-password test needs to see.
		SimpleClientHttpRequestFactory bufferedRequests = new SimpleClientHttpRequestFactory();
		bufferedRequests.setOutputStreaming(false);
		rest.getRestTemplate().setRequestFactory(bufferedRequests);

		staff = Session.login(rest, "admin", SEED_PASSWORD);
		student = Session.login(rest, "demo.stu", SEED_PASSWORD);
		professor = Session.login(rest, "demo.prof", SEED_PASSWORD);

		JsonNode me = json.readTree(student.get("/api/auth/me").getBody());
		myStudentId = me.path("studentId").asInt();

		JsonNode profMe = json.readTree(professor.get("/api/auth/me").getBody());
		myProfessorId = profMe.path("professorId").asInt();

		anotherStudentId = firstIdOtherThan(
				staff.get("/api/student?page=0&size=20").getBody(), "students", myStudentId);
		anotherProfessorId = firstIdOtherThan(
				staff.get("/api/professor?page=0&size=20").getBody(), "professors", myProfessorId);
	}

	private int firstIdOtherThan(String body, String collection, int excluded) throws Exception {
		for (JsonNode row : json.readTree(body).path(collection)) {
			int id = row.path("id").asInt();
			if (id != excluded) {
				return id;
			}
		}
		throw new IllegalStateException(
				"the database needs at least two rows in " + collection + " for this test");
	}

	// ── no session ────────────────────────────────────────────────────

	@Nested
	@DisplayName("without a session")
	class Anonymous {

		@Test
		@DisplayName("the API is closed")
		void apiIsClosed() {
			for (String path : List.of("/api/student", "/api/course", "/api/program",
					"/api/professor", "/api/enrollment", "/api/assignment")) {
				assertThat(rest.getForEntity(path, String.class).getStatusCode())
						.as("anonymous GET %s", path)
						.isEqualTo(HttpStatus.UNAUTHORIZED);
			}
		}

		@Test
		@DisplayName("but the login screen can still ask who it is talking to")
		void sessionEndpointIsPublic() {
			ResponseEntity<String> response = rest.getForEntity("/api/auth/me", String.class);
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(response.getBody()).contains("\"authenticated\":false");
		}

		@Test
		@DisplayName("a wrong password is refused")
		void wrongPasswordRefused() {
			assertThat(Session.attemptLogin(rest, "admin", "definitely-not-the-password"))
					.isEqualTo(HttpStatus.UNAUTHORIZED);
		}
	}

	// ── staff ─────────────────────────────────────────────────────────

	@Nested
	@DisplayName("as staff")
	class Staff {

		@Test
		@DisplayName("may browse and search the whole student body")
		void mayBrowseStudents() {
			assertThat(staff.get("/api/student").getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(staff.get("/api/student/search/name?value=a").getStatusCode())
					.isEqualTo(HttpStatus.OK);
		}

		@Test
		@DisplayName("may read any student's record")
		void mayReadAnyStudent() {
			assertThat(staff.get("/api/student/" + anotherStudentId).getStatusCode())
					.isEqualTo(HttpStatus.OK);
		}

		@Test
		@DisplayName("may list enrollments and teaching assignments")
		void mayListEverything() {
			assertThat(staff.get("/api/enrollment").getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(staff.get("/api/assignment").getStatusCode()).isEqualTo(HttpStatus.OK);
		}

		@Test
		@DisplayName("may read anyone's teaching assignments")
		void mayReadAnyAssignments() {
			assertThat(staff.get("/api/assignment/search/professorId?value=" + anotherProfessorId)
					.getStatusCode()).isEqualTo(HttpStatus.OK);
		}
	}

	// ── student ───────────────────────────────────────────────────────

	@Nested
	@DisplayName("as a student")
	class Student {

		@Test
		@DisplayName("may read the course and program catalogue")
		void mayReadCatalogue() {
			assertThat(student.get("/api/course").getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(student.get("/api/program").getStatusCode()).isEqualTo(HttpStatus.OK);
		}

		@Test
		@DisplayName("may read their own record")
		void mayReadOwnRecord() {
			assertThat(student.get("/api/student/" + myStudentId).getStatusCode())
					.isEqualTo(HttpStatus.OK);
		}

		@Test
		@DisplayName("may NOT read another student's record — changing the id in the URL fails")
		void mayNotReadAnotherRecord() {
			assertThat(student.get("/api/student/" + anotherStudentId).getStatusCode())
					.isEqualTo(HttpStatus.FORBIDDEN);
		}

		@Test
		@DisplayName("may read their own enrollments")
		void mayReadOwnEnrollments() {
			assertThat(student.get("/api/enrollment/search/studentId?value=" + myStudentId)
					.getStatusCode()).isEqualTo(HttpStatus.OK);
		}

		@Test
		@DisplayName("may NOT read another student's enrollments")
		void mayNotReadAnotherEnrollments() {
			assertThat(student.get("/api/enrollment/search/studentId?value=" + anotherStudentId)
					.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
		}

		@Test
		@DisplayName("may NOT list every student")
		void mayNotListStudents() {
			assertThat(student.get("/api/student").getStatusCode())
					.isEqualTo(HttpStatus.FORBIDDEN);
		}

		@Test
		@DisplayName("may NOT search students — that would enumerate the directory")
		void mayNotSearchStudents() {
			assertThat(student.get("/api/student/search/name?value=a").getStatusCode())
					.isEqualTo(HttpStatus.FORBIDDEN);
			assertThat(student.get("/api/student/search/gpaGreater?value=3").getStatusCode())
					.isEqualTo(HttpStatus.FORBIDDEN);
		}

		@Test
		@DisplayName("may NOT list every enrollment or assignment")
		void mayNotListAdminTables() {
			assertThat(student.get("/api/enrollment").getStatusCode())
					.isEqualTo(HttpStatus.FORBIDDEN);
			assertThat(student.get("/api/assignment").getStatusCode())
					.isEqualTo(HttpStatus.FORBIDDEN);
		}

		@Test
		@DisplayName("may NOT create, update or delete anything")
		void mayNotWrite() {
			assertThat(student.post("/api/course", "{\"name\":\"ZZ-should-never-exist\"}")
					.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
			assertThat(student.put("/api/course", "{\"id\":1,\"name\":\"ZZ\"}")
					.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
			assertThat(student.delete("/api/student/" + anotherStudentId)
					.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
			assertThat(student.post("/api/enrollment", "{}")
					.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
		}

		@Test
		@DisplayName("may NOT set a grade, not even on an enrollment of their own")
		void mayNotGrade() {
			assertThat(student.put("/api/enrollment", "{\"id\":1,\"grade\":100}")
					.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
		}
	}

	// ── professor ─────────────────────────────────────────────────────

	@Nested
	@DisplayName("as a professor")
	class Professor {

		@Test
		@DisplayName("may list their own teaching assignments")
		void mayListOwnAssignments() {
			assertThat(professor.get("/api/assignment/search/professorId?value=" + myProfessorId)
					.getStatusCode()).isEqualTo(HttpStatus.OK);
		}

		@Test
		@DisplayName("may NOT list another professor's teaching assignments")
		void mayNotListOthersAssignments() {
			assertThat(professor.get("/api/assignment/search/professorId?value=" + anotherProfessorId)
					.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
		}

		@Test
		@DisplayName("may read the catalogue")
		void mayReadCatalogue() {
			assertThat(professor.get("/api/course").getStatusCode()).isEqualTo(HttpStatus.OK);
		}

		@Test
		@DisplayName("may NOT list every student")
		void mayNotListStudents() {
			assertThat(professor.get("/api/student").getStatusCode())
					.isEqualTo(HttpStatus.FORBIDDEN);
		}

		@Test
		@DisplayName("may NOT decide who teaches what")
		void mayNotAssignCourses() {
			assertThat(professor.post("/api/assignment",
					"{\"professor\":{\"id\":1},\"course\":{\"id\":1}}")
					.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
			assertThat(professor.delete("/api/assignment/1")
					.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
		}

		// Whether a professor may read a given student depends on whether that student is
		// enrolled in one of their courses, which is a property of the data rather than of
		// the rules — so asserting it here would make the test pass or fail on whatever
		// happens to be in the database. That rule is covered exhaustively, both ways, by
		// AccessGuardTest.CanViewStudent.
	}

	// ── terms ─────────────────────────────────────────────────────────

	@Nested
	@DisplayName("terms")
	class Terms {

		@Test
		@DisplayName("every signed-in role may read the term list and the current term")
		void readableByEveryone() {
			for (Session session : List.of(staff, student, professor)) {
				assertThat(session.get("/api/term").getStatusCode()).isEqualTo(HttpStatus.OK);
				assertThat(session.get("/api/term/current").getStatusCode()).isEqualTo(HttpStatus.OK);
			}
		}

		@Test
		@DisplayName("but not anonymously")
		void notReadableAnonymously() {
			assertThat(rest.getForEntity("/api/term", String.class).getStatusCode())
					.isEqualTo(HttpStatus.UNAUTHORIZED);
		}

		@Test
		@DisplayName("a student may NOT create, update, delete, or reassign the current term")
		void studentMayNotWrite() {
			assertThat(student.post("/api/term", "{\"name\":\"ZZ-student\"}")
					.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
			assertThat(student.put("/api/term", "{\"id\":1,\"name\":\"ZZ-student\"}")
					.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
			assertThat(student.delete("/api/term/1")
					.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
			assertThat(student.put("/api/term/1/current", null)
					.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
		}

		@Test
		@DisplayName("nor may a professor")
		void professorMayNotWrite() {
			assertThat(professor.post("/api/term", "{\"name\":\"ZZ-prof\"}")
					.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
			assertThat(professor.put("/api/term", "{\"id\":1,\"name\":\"ZZ-prof\"}")
					.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
			assertThat(professor.delete("/api/term/1")
					.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
			assertThat(professor.put("/api/term/1/current", null)
					.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
		}

		@Test
		@DisplayName("an empty body from a non-staff caller is still a 403, not a 500")
		void malformedBodyStillRefused() {
			assertThat(student.post("/api/term", "").getStatusCode())
					.isEqualTo(HttpStatus.FORBIDDEN);
		}

		@Test
		@DisplayName("staff may create a term, and it comes back in the list")
		void staffMayCreateAndDelete() throws Exception {
			// Uniquely named so a leftover row from an interrupted run can never collide
			// with UQ_TERMS_NAME and fail the next one.
			String name = "ZZ-IT-" + System.nanoTime();
			ResponseEntity<String> created =
					staff.post("/api/term", "{\"name\":\"" + name + "\"}");
			assertThat(created.getStatusCode()).isEqualTo(HttpStatus.OK);

			int newId = json.readTree(created.getBody()).path("id").asInt();
			try {
				assertThat(staff.get("/api/term").getBody()).contains(name);

				// A freshly created term is never the current one — only setCurrent makes
				// it so, and this test deliberately never calls that.
				assertThat(json.readTree(created.getBody()).path("current").asBoolean())
						.isFalse();
			} finally {
				assertThat(staff.delete("/api/term/" + newId).getStatusCode())
						.isEqualTo(HttpStatus.OK);
			}
		}

		// The staff happy path for PUT /api/term/{id}/current is deliberately not
		// exercised. Setting a term current clears IS_CURRENT from whichever term held it,
		// and this suite runs against the shared application database — a failure between
		// the clear and the set would leave the real registrar with no current term, which
		// TermService treats as "no enrollments may be created". The refusal cases above
		// are what protect the endpoint; its success path is cheap to check by hand and
		// not worth that blast radius here.
	}

	// ── CSRF ──────────────────────────────────────────────────────────

	@Nested
	@DisplayName("CSRF")
	class Csrf {

		@Test
		@DisplayName("a write without a token is refused even for staff")
		void writeWithoutTokenRefused() {
			assertThat(staff.postWithoutCsrf("/api/course", "{\"name\":\"ZZ-no-token\"}")
					.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
		}

		@Test
		@DisplayName("a read needs no token")
		void readNeedsNoToken() {
			assertThat(staff.get("/api/course").getStatusCode()).isEqualTo(HttpStatus.OK);
		}
	}

	// ── signing out ───────────────────────────────────────────────────

	@Nested
	@DisplayName("after signing out")
	class SignOut {

		@Test
		@DisplayName("the session no longer opens anything")
		void sessionIsGone() {
			Session temporary = Session.login(rest, "admin", SEED_PASSWORD);
			assertThat(temporary.get("/api/student").getStatusCode()).isEqualTo(HttpStatus.OK);

			assertThat(temporary.post("/api/auth/logout", null).getStatusCode())
					.isEqualTo(HttpStatus.NO_CONTENT);

			assertThat(temporary.get("/api/student").getStatusCode())
					.isEqualTo(HttpStatus.UNAUTHORIZED);
		}
	}

	// ── a browser's worth of cookie handling ──────────────────────────

	/**
	 * Holds the session cookie and CSRF token across requests, the way a browser would.
	 * TestRestTemplate is stateless, so without this every request would arrive
	 * unauthenticated.
	 */
	private static final class Session {

		private final TestRestTemplate rest;
		private final List<String> cookies = new ArrayList<>();
		private String csrfToken;

		private Session(TestRestTemplate rest) {
			this.rest = rest;
		}

		static Session login(TestRestTemplate rest, String username, String password) {
			Session session = new Session(rest);
			HttpStatusCode status = session.doLogin(username, password);
			if (status != HttpStatus.NO_CONTENT) {
				throw new IllegalStateException(
						"could not sign in as " + username + " (got " + status + "). "
								+ "The seed accounts come from changeset seed-users-1.");
			}
			return session;
		}

		static HttpStatusCode attemptLogin(TestRestTemplate rest, String username, String password) {
			return new Session(rest).doLogin(username, password);
		}

		private HttpStatusCode doLogin(String username, String password) {
			// The public session endpoint is what issues the CSRF cookie, exactly as the
			// login screen does before it can POST.
			get("/api/auth/me");

			MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
			form.add("username", username);
			form.add("password", password);

			HttpHeaders headers = headers();
			headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

			ResponseEntity<String> response = rest.exchange(
					"/api/auth/login", HttpMethod.POST,
					new HttpEntity<>(form, headers), String.class);
			remember(response);
			return response.getStatusCode();
		}

		ResponseEntity<String> get(String path) {
			return send(path, HttpMethod.GET, null, true);
		}

		ResponseEntity<String> post(String path, String body) {
			return send(path, HttpMethod.POST, body, true);
		}

		ResponseEntity<String> postWithoutCsrf(String path, String body) {
			return send(path, HttpMethod.POST, body, false);
		}

		ResponseEntity<String> put(String path, String body) {
			return send(path, HttpMethod.PUT, body, true);
		}

		ResponseEntity<String> delete(String path) {
			return send(path, HttpMethod.DELETE, null, true);
		}

		private ResponseEntity<String> send(String path, HttpMethod method,
		                                    String body, boolean withCsrf) {
			HttpHeaders headers = withCsrf ? headers() : cookieHeadersOnly();
			if (body != null) {
				headers.setContentType(MediaType.APPLICATION_JSON);
			}
			ResponseEntity<String> response = rest.exchange(
					path, method, new HttpEntity<>(body, headers), String.class);
			remember(response);
			return response;
		}

		private HttpHeaders cookieHeadersOnly() {
			HttpHeaders headers = new HttpHeaders();
			cookies.forEach(cookie -> headers.add(HttpHeaders.COOKIE, cookie));
			return headers;
		}

		private HttpHeaders headers() {
			HttpHeaders headers = cookieHeadersOnly();
			if (csrfToken != null) {
				headers.add("X-XSRF-TOKEN", csrfToken);
			}
			return headers;
		}

		/** Keep whatever cookies the server just set, and pick the CSRF token out of them. */
		private void remember(ResponseEntity<String> response) {
			List<String> setCookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
			if (setCookies == null) {
				return;
			}
			for (String setCookie : setCookies) {
				String pair = setCookie.split(";", 2)[0];
				String name = pair.split("=", 2)[0];
				if ("XSRF-TOKEN".equals(name)) {
					csrfToken = pair.substring(pair.indexOf('=') + 1);
				}
				cookies.removeIf(existing -> existing.startsWith(name + "="));
				cookies.add(pair);
			}
		}
	}
}
