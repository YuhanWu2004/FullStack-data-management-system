package com.cicad.app.repository;

import com.cicad.app.entities.ProfessorCourse;
import com.cicad.app.entities.Program;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public class ProfessorCourseRepository {

    @Autowired
    private EntityManager entityManager;

    public ProfessorCourse get(Integer id) {return entityManager.find(ProfessorCourse.class, id);}

    public List<ProfessorCourse> getAll() {
        return  entityManager
                .createQuery("SELECT pc FROM ProfessorCourse pc", ProfessorCourse.class)
                .getResultList();
    }

    public List<ProfessorCourse> getPage(int page, int size) {
        return entityManager
                .createQuery("SELECT pc FROM ProfessorCourse  pc", ProfessorCourse.class)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    public Long countAll() {
        return entityManager
                .createQuery("SELECT COUNT(pc) FROM ProfessorCourse pc", Long.class)
                .getSingleResult();
    }

    public ProfessorCourse create(ProfessorCourse actualProfessorCourse) {
        entityManager.persist(actualProfessorCourse);
        return actualProfessorCourse;
    }

    public ProfessorCourse update(ProfessorCourse actualProfessorCourse) {
        entityManager.merge(actualProfessorCourse);
        return actualProfessorCourse;
    }

    public void delete(Integer id) {
        ProfessorCourse existingProfessorCourse = entityManager.find(ProfessorCourse.class, id);
        if (existingProfessorCourse != null) {
            entityManager.remove(existingProfessorCourse);
        }
    }

    public List<ProfessorCourse> findByProfessorId(Integer professorId, int page, int size) {
        return entityManager.createQuery(
                "SELECT pc FROM ProfessorCourse pc WHERE pc.professor.id = :professorId", ProfessorCourse.class)
                .setParameter("professorId", professorId)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    public Long countByProfessorId(Integer ProfessorId) {
        return entityManager
                .createQuery
                        ("SELECT COUNT(pc) FROM ProfessorCourse pc WHERE pc.professor.id = :id", Long.class)

                .setParameter("id", ProfessorId)
                .getSingleResult();
    }

    public List<ProfessorCourse> findByCourseId(Integer courseId, int page, int size) {
        return entityManager.createQuery(
                "SELECT pc FROM ProfessorCourse pc WHERE pc.course.id = :courseId", ProfessorCourse.class)
                .setParameter("courseId", courseId)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    public Long countByCourseId(Integer CourseId) {
        return entityManager
                .createQuery
                        ("SELECT COUNT(pc) FROM ProfessorCourse  pc WHERE pc.course.id = :id", Long.class)
                .setParameter("id", CourseId)
                .getSingleResult();
    }

    public List<ProfessorCourse> findByProfessorIdAndCourseId (Integer professorId, Integer courseId, int page, int size) {
        return entityManager
                .createQuery("SELECT pc FROM ProfessorCourse pc WHERE pc.professor.id = :professorId AND pc.course.id = :courseId ", ProfessorCourse.class)
                .setParameter("professorId", professorId)
                .setParameter("courseId", courseId)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    // --- Professor Name Search ---
    public List<ProfessorCourse> findByProfessorName(String professorName, int page, int size) {
        String searchPattern = "%" + professorName.toLowerCase() + "%";
        return entityManager
                .createQuery(
                        "SELECT pc FROM ProfessorCourse pc WHERE " +
                        "LOWER(pc.professor.firstName) LIKE :name OR " +
                        "LOWER(pc.professor.lastName) LIKE :name OR " +
                        "LOWER(CONCAT(pc.professor.firstName, ' ', pc.professor.lastName)) LIKE :name", 
                        ProfessorCourse.class)
                .setParameter("name", searchPattern)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    public Long countByProfessorName(String professorName) {
        String searchPattern = "%" + professorName.toLowerCase() + "%";
        return entityManager
                .createQuery(
                        "SELECT COUNT(pc) FROM ProfessorCourse pc WHERE " +
                        "LOWER(pc.professor.firstName) LIKE :name OR " +
                        "LOWER(pc.professor.lastName) LIKE :name OR " +
                        "LOWER(CONCAT(pc.professor.firstName, ' ', pc.professor.lastName)) LIKE :name", 
                        Long.class)
                .setParameter("name", searchPattern)
                .getSingleResult();
    }

    // --- Course Name Search ---
    public List<ProfessorCourse> findByCourseName(String courseName, int page, int size) {
        String searchPattern = "%" + courseName.toLowerCase() + "%";
        return entityManager
                .createQuery(
                        "SELECT pc FROM ProfessorCourse pc WHERE LOWER(pc.course.name) LIKE :name", 
                        ProfessorCourse.class)
                .setParameter("name", searchPattern)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    public Long countByCourseName(String courseName) {
        String searchPattern = "%" + courseName.toLowerCase() + "%";
        return entityManager
                .createQuery(
                        "SELECT COUNT(pc) FROM ProfessorCourse pc WHERE LOWER(pc.course.name) LIKE :name", 
                        Long.class)
                .setParameter("name", searchPattern)
                .getSingleResult();
    }
}
