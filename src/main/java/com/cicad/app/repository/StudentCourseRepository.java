package com.cicad.app.repository;

import com.cicad.app.entities.Student;
import com.cicad.app.entities.StudentCourse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public class StudentCourseRepository {
    @PersistenceContext
    private EntityManager entityManager;

    public StudentCourse get(Integer id) {
        return entityManager.find(StudentCourse.class, id);
    }

    public List<StudentCourse>  getAll() {
        return entityManager.createQuery
                ("SELECT sc FROM StudentCourse sc", StudentCourse.class).getResultList();
    }

    public List<StudentCourse> getPage(int page, int size) {
        return entityManager
                .createQuery("SELECT sc FROM StudentCourse sc", StudentCourse.class)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();}


    public Long countAll() {
        return entityManager
                .createQuery("SELECT COUNT(sc) FROM StudentCourse sc", Long.class)
                .getSingleResult();
    }

    public StudentCourse update(StudentCourse actualStudentCourse) {
        entityManager.merge(actualStudentCourse);
        return actualStudentCourse;
    }

    public StudentCourse create(StudentCourse actualStudentCourse) {
        entityManager.persist(actualStudentCourse);
        return actualStudentCourse;
    }


    public void delete(Integer id) {
        StudentCourse existingStudentCourse = entityManager.find(StudentCourse.class, id);
        if (existingStudentCourse != null ) {
            entityManager.remove(existingStudentCourse);
        }
    }

    public List<StudentCourse> findByStudentId(Integer studentId, int page, int size) {
        return entityManager
                .createQuery
                        ("SELECT sc FROM StudentCourse sc WHERE sc.student.id = :id", StudentCourse.class)
                .setParameter("id", studentId)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }
    public Long countByStudentId(Integer studentId) {
        return entityManager
                .createQuery
                        ("SELECT COUNT(sc) FROM StudentCourse sc WHERE sc.student.id = :id", Long.class)

                .setParameter("id", studentId)
                .getSingleResult();
    }

    public  List<StudentCourse> findByCourseId(Integer courseId, int page, int size) {
        return entityManager
                .createQuery
                        ("SELECT sc FROM StudentCourse sc WHERE sc.course.id = :id", StudentCourse.class)

                .setParameter("id", courseId)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }


    public  Long CountByCourseId(Integer courseId) {
        return entityManager
                .createQuery
                        ("SELECT COUNT(sc) FROM StudentCourse sc WHERE sc.course.id = :id", Long.class)

                .setParameter("id", courseId)
                .getSingleResult();
    }

    public List<StudentCourse> findByStudentIdAndCourseId(Integer studentId, Integer courseId, int page, int size) {
        return entityManager
                .createQuery
                        ("SELECT sc FROM StudentCourse  sc WHERE sc.student.id = :studentId AND sc.course.id = :courseId "
                                , StudentCourse.class)
                .setParameter("studentId", studentId)
                .setParameter("courseId", courseId)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

// --- Student Name Search ---

    // --- Student Name Search (Updated for "First Last" full name typing) ---
    public List<StudentCourse> findByStudentName(String studentName, int page, int size) {
        String searchPattern = "%" + studentName.toLowerCase() + "%";
        return entityManager
                .createQuery(
                        "SELECT sc FROM StudentCourse sc WHERE " +
                        "LOWER(sc.student.firstName) LIKE :name OR " +
                        "LOWER(sc.student.lastName) LIKE :name OR " +
                        "LOWER(CONCAT(sc.student.firstName, ' ', sc.student.lastName)) LIKE :name", 
                        StudentCourse.class)
                .setParameter("name", searchPattern)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    public Long countByStudentName(String studentName) {
        String searchPattern = "%" + studentName.toLowerCase() + "%";
        return entityManager
                .createQuery(
                        "SELECT COUNT(sc) FROM StudentCourse sc WHERE " +
                        "LOWER(sc.student.firstName) LIKE :name OR " +
                        "LOWER(sc.student.lastName) LIKE :name OR " +
                        "LOWER(CONCAT(sc.student.firstName, ' ', sc.student.lastName)) LIKE :name", 
                        Long.class)
                .setParameter("name", searchPattern)
                .getSingleResult();
    }

    // --- Course Name Search ---

    public List<StudentCourse> findByCourseName(String courseName, int page, int size) {
        String searchPattern = "%" + courseName.toLowerCase() + "%";
        return entityManager
                .createQuery(
                        "SELECT sc FROM StudentCourse sc WHERE LOWER(sc.course.name) LIKE :name",
                        StudentCourse.class)
                .setParameter("name", searchPattern)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    public Long countByCourseName(String courseName) {
        String searchPattern = "%" + courseName.toLowerCase() + "%";
        return entityManager
                .createQuery(
                        "SELECT COUNT(sc) FROM StudentCourse sc WHERE LOWER(sc.course.name) LIKE :name",
                        Long.class)
                .setParameter("name", searchPattern)
                .getSingleResult();
    }
    





}
