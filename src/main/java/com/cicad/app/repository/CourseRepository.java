package com.cicad.app.repository;

import com.cicad.app.entities.Course;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.security.PublicKey;
import java.util.List;

@Repository
@Transactional
public class CourseRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public Course get(Integer id) {return entityManager.find(Course.class, id);}

    public List<Course> getALl() {
        return entityManager
                .createQuery("SELECT c FROM Course c", Course.class).getResultList();}


    public List<Course> getPage(int page, int size) {
        return entityManager
                .createQuery("SELECT c FROM Course c", Course.class)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();}


    public Long countAll() {
        return entityManager
                .createQuery("SELECT COUNT(c) FROM Course c", Long.class)
                .getSingleResult();
    }


    // search by name with pagination
    public List<Course> findByName(String name, int page, int size) {
        return entityManager
                .createQuery(
                        "SELECT c FROM Course c WHERE LOWER(c.name) LIKE :name",
                        Course.class)
                .setParameter("name", "%" + name.toLowerCase() + "%")
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    // count for search
    public Long countByName(String name) {
        return entityManager
                .createQuery(
                        "SELECT COUNT(c) FROM Course c WHERE LOWER(c.name) LIKE :name",
                        Long.class)
                .setParameter("name", "%" + name.toLowerCase() + "%")
                .getSingleResult();
    }

    public Course create(Course actualCourse) {
        entityManager.persist(actualCourse);
        return actualCourse;
    }

    public Course update(Course actualCourse) {
        return entityManager.merge(actualCourse);
    }

    public void delete(Integer id) {
        Course course = entityManager.find(Course.class, id);
        if (course != null) {
            entityManager.remove(course);
        }
    }




}
