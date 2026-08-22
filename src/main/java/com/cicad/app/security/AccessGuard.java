package com.cicad.app.security;

import com.cicad.app.repository.AccessQueryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Ownership rules, reachable from {@code @PreAuthorize} as {@code @access.…}.
 * <p>
 * Role checks answer "what kind of user is this?"; these answer "is this user allowed to
 * touch <em>that particular row</em>?" — the half that a role annotation cannot express.
 * Every method reads the caller's identity from the security context rather than from a
 * request parameter, which is the whole point: a student can change the id in the URL,
 * but not the id attached to their session.
 */
@Component("access")
public class AccessGuard {

	@Autowired
	private AccessQueryRepository accessQueries;

	// ── who is calling ────────────────────────────────────────────────

	private AppUserDetails current() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !auth.isAuthenticated()) {
			return null;
		}
		Object principal = auth.getPrincipal();
		return principal instanceof AppUserDetails details ? details : null;
	}

	private boolean hasRole(String role) {
		AppUserDetails me = current();
		return me != null && me.getRoleNames().contains(role);
	}

	public boolean isStaff() {
		return hasRole("ROLE_STAFF");
	}

	public boolean isProfessor() {
		return hasRole("ROLE_PROFESSOR");
	}

	public Integer myStudentId() {
		AppUserDetails me = current();
		return me == null ? null : me.getStudentId();
	}

	public Integer myProfessorId() {
		AppUserDetails me = current();
		return me == null ? null : me.getProfessorId();
	}

	private boolean isMe(Integer studentId) {
		Integer mine = myStudentId();
		return mine != null && Objects.equals(mine, studentId);
	}

	// ── students ──────────────────────────────────────────────────────

	/**
	 * Staff see anyone. A student sees only themselves. A professor sees students who are
	 * enrolled in a course they teach.
	 */
	public boolean canViewStudent(Integer studentId) {
		if (studentId == null) {
			return false;
		}
		if (isStaff()) {
			return true;
		}
		if (isMe(studentId)) {
			return true;
		}
		return isProfessor() && accessQueries.professorTeachesStudent(myProfessorId(), studentId);
	}

	// ── enrollments ───────────────────────────────────────────────────

	/** Reading one enrollment: staff, the student it belongs to, or the teaching professor. */
	public boolean canViewEnrollment(Integer enrollmentId) {
		if (enrollmentId == null) {
			return false;
		}
		if (isStaff()) {
			return true;
		}
		Integer studentId = accessQueries.studentIdOfEnrollment(enrollmentId);
		if (studentId == null) {
			// Enrollment does not exist. Deny rather than 404 — telling an unauthorized
			// caller which ids are real is itself a leak.
			return false;
		}
		if (isMe(studentId)) {
			return true;
		}
		return canGradeEnrollment(enrollmentId);
	}

	/**
	 * Changing an enrollment — in practice, setting a grade. Staff always; a professor only
	 * for courses on their own teaching assignments. Students never, including their own.
	 */
	public boolean canGradeEnrollment(Integer enrollmentId) {
		if (enrollmentId == null) {
			return false;
		}
		if (isStaff()) {
			return true;
		}
		if (!isProfessor()) {
			return false;
		}
		Integer courseId = accessQueries.courseIdOfEnrollment(enrollmentId);
		return accessQueries.professorTeachesCourse(myProfessorId(), courseId);
	}

	/** Listing the roster of a course: staff, or the professor who teaches it. */
	public boolean canViewCourseRoster(Integer courseId) {
		if (courseId == null) {
			return false;
		}
		return isStaff()
				|| (isProfessor() && accessQueries.professorTeachesCourse(myProfessorId(), courseId));
	}

	// ── professors ────────────────────────────────────────────────────

	/** A professor's own teaching assignments, or anything staff asks for. */
	public boolean canViewProfessorAssignments(Integer professorId) {
		if (professorId == null) {
			return false;
		}
		if (isStaff()) {
			return true;
		}
		Integer mine = myProfessorId();
		return mine != null && Objects.equals(mine, professorId);
	}
}
