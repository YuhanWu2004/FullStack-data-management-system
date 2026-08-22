package com.cicad.app.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * The relationship questions authorization needs to ask, kept apart from the CRUD
 * repositories so the rules are readable in one place.
 */
@Repository
@Transactional(readOnly = true)
public class AccessQueryRepository {

	@PersistenceContext
	private EntityManager entityManager;

	/** Is this course on the professor's teaching assignments? */
	public boolean professorTeachesCourse(Integer professorId, Integer courseId) {
		if (professorId == null || courseId == null) {
			return false;
		}
		return entityManager
				.createQuery(
						"SELECT COUNT(pc) FROM ProfessorCourse pc " +
								"WHERE pc.professor.id = :professorId AND pc.course.id = :courseId",
						Long.class)
				.setParameter("professorId", professorId)
				.setParameter("courseId", courseId)
				.getSingleResult() > 0;
	}

	/** Is the student enrolled in at least one course the professor teaches? */
	public boolean professorTeachesStudent(Integer professorId, Integer studentId) {
		if (professorId == null || studentId == null) {
			return false;
		}
		return entityManager
				.createQuery(
						"SELECT COUNT(sc) FROM StudentCourse sc, ProfessorCourse pc " +
								"WHERE sc.course.id = pc.course.id " +
								"AND sc.student.id = :studentId " +
								"AND pc.professor.id = :professorId",
						Long.class)
				.setParameter("studentId", studentId)
				.setParameter("professorId", professorId)
				.getSingleResult() > 0;
	}

	/** Which student does this enrollment belong to? Null when the enrollment is gone. */
	public Integer studentIdOfEnrollment(Integer enrollmentId) {
		if (enrollmentId == null) {
			return null;
		}
		return entityManager
				.createQuery(
						"SELECT sc.student.id FROM StudentCourse sc WHERE sc.id = :id", Integer.class)
				.setParameter("id", enrollmentId)
				.getResultStream()
				.findFirst()
				.orElse(null);
	}

	/** Which course does this enrollment belong to? Null when the enrollment is gone. */
	public Integer courseIdOfEnrollment(Integer enrollmentId) {
		if (enrollmentId == null) {
			return null;
		}
		return entityManager
				.createQuery(
						"SELECT sc.course.id FROM StudentCourse sc WHERE sc.id = :id", Integer.class)
				.setParameter("id", enrollmentId)
				.getResultStream()
				.findFirst()
				.orElse(null);
	}
}
