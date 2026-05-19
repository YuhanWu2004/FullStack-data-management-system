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

    public List<ProfessorCourse> findByProfessorId(Integer professorId) {
        return entityManager.createQuery(
                "SELECT pc FROM ProfessorCourse pc WHERE pc.professor.id = :professorId", ProfessorCourse.class
        ).setParameter("professorId", professorId).getResultList();
    }

    public List<ProfessorCourse> findByCourseId(Integer courseId) {
        return entityManager.createQuery(
                "SELECT pc FROM ProfessorCourse pc WHERE pc.course.id = :courseId", ProfessorCourse.class
        ).setParameter("courseId", courseId).getResultList();
    }

    public ProfessorCourse findByProfessorIdAndCourseId (Integer professorId, Integer courseId) {
        return entityManager.createQuery(
                "SELECT pc FROM ProfessorCourse pc WHERE pc.professor.id = :professorId AND pc.course.id = :courseId ", ProfessorCourse.class
        ).setParameter("professorId", professorId).setParameter("courseId", courseId).getSingleResult();
    }
}
