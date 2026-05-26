package com.cicad.app.repository;

import com.cicad.app.entities.Course;
import com.cicad.app.entities.Student;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.awt.print.Pageable;
import java.util.List;

@Repository
@Transactional
public class StudentRepository {

	@PersistenceContext
	private EntityManager entityManager;

	public Student get(Integer id) {
		return entityManager.find(Student.class, id);
	}

	public List<Student> getAll() {
		return entityManager.createQuery("SELECT s FROM Student s", Student.class).getResultList();
	}
	public List<Student> getPage(int page, int size) {
		return entityManager
				.createQuery("SELECT s FROM Student s", Student.class)
				.setFirstResult(page * size)
				.setMaxResults(size)
				.getResultList();}


	public Long countAll() {
		return entityManager
				.createQuery("SELECT COUNT(s) FROM Student s", Long.class)
				.getSingleResult();
	}

	public Student create(Student actualStudent) {
		entityManager.persist(actualStudent);
		return actualStudent;
	}

	public Student update(Student actualStudent) {
		return entityManager.merge(actualStudent);
	}

	public void delete(Integer id) {
		Student student = entityManager.find(Student.class, id);
		if (student != null) {
			entityManager.remove(student);
		}
	}


	public List<Student> findByLastName(String lastName, int page, int size) {
		return entityManager
				.createQuery
						("SELECT s FROM Student s WHERE s.lastName = :lastName",
				Student.class)
				.setParameter("lastName", lastName)
				.setFirstResult(page * size)
				.setMaxResults(size)
				.getResultList();
	}

	public Long countByFirstName(String firstName) {
		return entityManager
				.createQuery(
						"SELECT COUNT(s) FROM Student s WHERE LOWER(s.firstName) LIKE :firstName",
						Long.class)
				.setParameter("firstName", "%" + firstName.toLowerCase() + "%")
				.getSingleResult();
	}

	public List<Student> findByFirstName(String firstName, int page, int size) {
		return entityManager
				.createQuery
						("SELECT s FROM Student s WHERE s.firstName = :firstName",
								Student.class)
				.setParameter("firstName", firstName)
				.setFirstResult(page * size)
				.setMaxResults(size)
				.getResultList();

	}

	public Long countByLastName(String lastName) {
		return entityManager
				.createQuery(
						"SELECT COUNT(s) FROM Student s WHERE LOWER(s.lastName) LIKE :lastName",
						Long.class)
				.setParameter("lastName", "%" + lastName.toLowerCase() + "%")
				.getSingleResult();
	}

	public List<Student> findByProgramId(Integer programId, int page, int size) {
		return entityManager
				.createQuery
						("SELECT s FROM Student s WHERE s.program.id = :programId",
								Student.class)
				.setParameter("programId", programId)
				.setFirstResult(page * size)
				.setMaxResults(size)
				.getResultList();
	}


	public List<Student> findGpaGreaterOrEqualThan(Integer gpa, int page, int size) {
		return entityManager
				.createQuery
						("SELECT s FROM Student s WHERE s.gpa >= :gpa " +
										"ORDER BY s.gpa DESC",
								Student.class)
				.setParameter("gpa", gpa)
				.setFirstResult(page * size)
				.setMaxResults(size)
				.getResultList();
	}

	public Long countByGpaGreaterOrEqualThan(Integer gpa, int page, int size) {
		return entityManager
				.createQuery(
						"SELECT COUNT(s) FROM Student s WHERE s.gpa >= :gpa",
						Long.class)
				.setParameter("gpa", gpa)
				.getSingleResult();
	}


}
