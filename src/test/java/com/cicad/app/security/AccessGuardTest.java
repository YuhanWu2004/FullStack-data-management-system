package com.cicad.app.security;

import com.cicad.app.entities.AppUser;
import com.cicad.app.entities.Professor;
import com.cicad.app.entities.Role;
import com.cicad.app.entities.Student;
import com.cicad.app.repository.AccessQueryRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The ownership half of authorization, tested in isolation.
 *
 * Role checks are easy to eyeball; "can this particular user touch this particular row" is
 * not, and it is the half that silently breaks. Every case below where the answer is
 * <em>no</em> matters more than the ones where it is yes — those are the leaks.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AccessGuardTest {

	private static final int ME = 2087;
	private static final int SOMEONE_ELSE = 3001;
	private static final int MY_PROF_ID = 86;
	private static final int OTHER_PROF_ID = 160;

	@Mock
	private AccessQueryRepository accessQueries;

	@InjectMocks
	private AccessGuard access;

	@AfterEach
	void clearContext() {
		SecurityContextHolder.clearContext();
	}

	// ── helpers ───────────────────────────────────────────────────────

	private void signedInAs(Integer studentId, Integer professorId, String... roles) {
		AppUser user = new AppUser();
		user.setId(1);
		user.setUsername("test.user");
		user.setPasswordHash("irrelevant");
		user.setEnabled(true);

		if (studentId != null) {
			Student student = new Student();
			student.setId(studentId);
			user.setStudent(student);
		}
		if (professorId != null) {
			Professor professor = new Professor();
			professor.setId(professorId);
			user.setProfessor(professor);
		}

		Set<Role> roleSet = new java.util.HashSet<>();
		for (String name : roles) {
			Role role = new Role();
			role.setName(name);
			roleSet.add(role);
		}
		user.setRoles(roleSet);

		AppUserDetails principal = new AppUserDetails(user);
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(
						principal, "n/a", principal.getAuthorities()));
	}

	private void signedInAsStaff() {
		signedInAs(null, null, "ROLE_STAFF");
	}

	private void signedInAsStudent() {
		signedInAs(ME, null, "ROLE_STUDENT");
	}

	private void signedInAsProfessor() {
		signedInAs(null, MY_PROF_ID, "ROLE_PROFESSOR");
	}

	// ── reading a student ─────────────────────────────────────────────

	@Nested
	@DisplayName("canViewStudent")
	class CanViewStudent {

		@Test
		@DisplayName("staff may read any student")
		void staffReadsAnyone() {
			signedInAsStaff();
			assertThat(access.canViewStudent(SOMEONE_ELSE)).isTrue();
		}

		@Test
		@DisplayName("staff is answered without touching the database")
		void staffShortCircuits() {
			signedInAsStaff();
			access.canViewStudent(SOMEONE_ELSE);
			verify(accessQueries, never()).professorTeachesStudent(any(), any());
		}

		@Test
		@DisplayName("a student may read their own record")
		void studentReadsSelf() {
			signedInAsStudent();
			assertThat(access.canViewStudent(ME)).isTrue();
		}

		@Test
		@DisplayName("a student may NOT read another student — the id in the URL is not the id in the session")
		void studentCannotReadAnother() {
			signedInAsStudent();
			assertThat(access.canViewStudent(SOMEONE_ELSE)).isFalse();
		}

		@Test
		@DisplayName("a professor may read a student they teach")
		void professorReadsOwnStudent() {
			signedInAsProfessor();
			when(accessQueries.professorTeachesStudent(MY_PROF_ID, SOMEONE_ELSE)).thenReturn(true);
			assertThat(access.canViewStudent(SOMEONE_ELSE)).isTrue();
		}

		@Test
		@DisplayName("a professor may NOT read a student they do not teach")
		void professorCannotReadStranger() {
			signedInAsProfessor();
			when(accessQueries.professorTeachesStudent(MY_PROF_ID, SOMEONE_ELSE)).thenReturn(false);
			assertThat(access.canViewStudent(SOMEONE_ELSE)).isFalse();
		}

		@Test
		@DisplayName("nobody signed in may read anything")
		void anonymousReadsNothing() {
			assertThat(access.canViewStudent(ME)).isFalse();
		}

		@Test
		@DisplayName("a null id is refused rather than treated as a wildcard")
		void nullIdRefused() {
			signedInAsStaff();
			assertThat(access.canViewStudent(null)).isFalse();
		}
	}

	// ── grading ───────────────────────────────────────────────────────

	@Nested
	@DisplayName("canGradeEnrollment")
	class CanGradeEnrollment {

		private static final int ENROLLMENT = 55;
		private static final int COURSE = 231;

		@Test
		@DisplayName("staff may grade anything")
		void staffGrades() {
			signedInAsStaff();
			assertThat(access.canGradeEnrollment(ENROLLMENT)).isTrue();
		}

		@Test
		@DisplayName("a professor may grade an enrollment in a course they teach")
		void professorGradesOwnCourse() {
			signedInAsProfessor();
			when(accessQueries.courseIdOfEnrollment(ENROLLMENT)).thenReturn(COURSE);
			when(accessQueries.professorTeachesCourse(MY_PROF_ID, COURSE)).thenReturn(true);
			assertThat(access.canGradeEnrollment(ENROLLMENT)).isTrue();
		}

		@Test
		@DisplayName("a professor may NOT grade a course they do not teach")
		void professorCannotGradeOtherCourse() {
			signedInAsProfessor();
			when(accessQueries.courseIdOfEnrollment(ENROLLMENT)).thenReturn(COURSE);
			when(accessQueries.professorTeachesCourse(MY_PROF_ID, COURSE)).thenReturn(false);
			assertThat(access.canGradeEnrollment(ENROLLMENT)).isFalse();
		}

		@Test
		@DisplayName("a student may NOT grade — not even their own enrollment")
		void studentNeverGrades() {
			signedInAsStudent();
			when(accessQueries.studentIdOfEnrollment(ENROLLMENT)).thenReturn(ME);
			assertThat(access.canGradeEnrollment(ENROLLMENT)).isFalse();
		}

		@Test
		@DisplayName("nobody signed in may grade")
		void anonymousNeverGrades() {
			assertThat(access.canGradeEnrollment(ENROLLMENT)).isFalse();
		}
	}

	// ── reading an enrollment ─────────────────────────────────────────

	@Nested
	@DisplayName("canViewEnrollment")
	class CanViewEnrollment {

		private static final int ENROLLMENT = 55;
		private static final int COURSE = 231;

		@Test
		@DisplayName("a student may read their own enrollment")
		void studentReadsOwn() {
			signedInAsStudent();
			when(accessQueries.studentIdOfEnrollment(ENROLLMENT)).thenReturn(ME);
			assertThat(access.canViewEnrollment(ENROLLMENT)).isTrue();
		}

		@Test
		@DisplayName("a student may NOT read someone else's enrollment")
		void studentCannotReadAnother() {
			signedInAsStudent();
			when(accessQueries.studentIdOfEnrollment(ENROLLMENT)).thenReturn(SOMEONE_ELSE);
			assertThat(access.canViewEnrollment(ENROLLMENT)).isFalse();
		}

		@Test
		@DisplayName("a professor may read an enrollment in a course they teach")
		void professorReadsOwnCourseEnrollment() {
			signedInAsProfessor();
			when(accessQueries.studentIdOfEnrollment(ENROLLMENT)).thenReturn(SOMEONE_ELSE);
			when(accessQueries.courseIdOfEnrollment(ENROLLMENT)).thenReturn(COURSE);
			when(accessQueries.professorTeachesCourse(MY_PROF_ID, COURSE)).thenReturn(true);
			assertThat(access.canViewEnrollment(ENROLLMENT)).isTrue();
		}

		@Test
		@DisplayName("an enrollment that does not exist is refused, not reported as missing")
		void unknownEnrollmentRefused() {
			signedInAsStudent();
			when(accessQueries.studentIdOfEnrollment(ENROLLMENT)).thenReturn(null);
			assertThat(access.canViewEnrollment(ENROLLMENT)).isFalse();
		}
	}

	// ── a professor's own assignments ─────────────────────────────────

	@Nested
	@DisplayName("canViewProfessorAssignments")
	class CanViewProfessorAssignments {

		@Test
		@DisplayName("a professor may list their own assignments")
		void ownAssignments() {
			signedInAsProfessor();
			assertThat(access.canViewProfessorAssignments(MY_PROF_ID)).isTrue();
		}

		@Test
		@DisplayName("a professor may NOT list another professor's assignments")
		void otherAssignments() {
			signedInAsProfessor();
			assertThat(access.canViewProfessorAssignments(OTHER_PROF_ID)).isFalse();
		}

		@Test
		@DisplayName("staff may list anyone's assignments")
		void staffListsAnyone() {
			signedInAsStaff();
			assertThat(access.canViewProfessorAssignments(OTHER_PROF_ID)).isTrue();
		}

		@Test
		@DisplayName("a student has no professor id, so cannot list assignments as one")
		void studentIsNotAProfessor() {
			signedInAsStudent();
			assertThat(access.canViewProfessorAssignments(MY_PROF_ID)).isFalse();
		}
	}

	// ── course roster ─────────────────────────────────────────────────

	@Nested
	@DisplayName("canViewCourseRoster")
	class CanViewCourseRoster {

		private static final int COURSE = 231;

		@Test
		@DisplayName("the teaching professor may see the roster")
		void teachingProfessor() {
			signedInAsProfessor();
			when(accessQueries.professorTeachesCourse(MY_PROF_ID, COURSE)).thenReturn(true);
			assertThat(access.canViewCourseRoster(COURSE)).isTrue();
		}

		@Test
		@DisplayName("a professor who does not teach it may not")
		void otherProfessor() {
			signedInAsProfessor();
			when(accessQueries.professorTeachesCourse(MY_PROF_ID, COURSE)).thenReturn(false);
			assertThat(access.canViewCourseRoster(COURSE)).isFalse();
		}

		@Test
		@DisplayName("a student may not see a course roster, even for a course they are in")
		void studentCannotSeeRoster() {
			signedInAsStudent();
			assertThat(access.canViewCourseRoster(COURSE)).isFalse();
		}
	}

	// ── someone holding two roles ─────────────────────────────────────

	@Test
	@DisplayName("holding both professor and staff grants the staff view, not the narrower one")
	void multipleRolesTakeTheWiderPath() {
		signedInAs(null, MY_PROF_ID, "ROLE_PROFESSOR", "ROLE_STAFF");
		assertThat(access.canViewStudent(SOMEONE_ELSE)).isTrue();
		assertThat(access.canViewProfessorAssignments(OTHER_PROF_ID)).isTrue();
		verify(accessQueries, never()).professorTeachesStudent(any(), any());
	}
}
