package com.cicad.app.repository;

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

    public List<StudentCourse> findByStudentId(Integer studentId) {
        return entityManager
                .createQuery
                        ("SELECT sc FROM StudentCourse sc WHERE sc.student.id = :id", StudentCourse.class)
                .setParameter("id", studentId)
                .getResultList();
    }

    public  List<StudentCourse> findByCourseId(Integer courseId) {
        return entityManager
                .createQuery
                        ("SELECT sc FROM StudentCourse sc WHERE sc.course.id = :id", StudentCourse.class)

                .setParameter("id", courseId)
                .getResultList();
    }

    public StudentCourse findByStudentIdAndCourseId(Integer studentId, Integer courseId) {
        return entityManager
                .createQuery
                        ("SELECT sc FROM StudentCourse  sc WHERE sc.course.id = :courseId AND sc.student.id = :studentId"
                                , StudentCourse.class)
                .setParameter("courseId", courseId).setParameter("studentId", studentId)
                .getSingleResult();
    }





}
