package com.cicad.app.repository;

import com.cicad.app.entities.Professor;
import com.cicad.app.entities.Program;
import com.cicad.app.entities.Student;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public class ProfessorRepository {
    @PersistenceContext
    private EntityManager entityManager;

    public Professor get(Integer id) {
        return entityManager.find(Professor.class, id);
    }

    public List<Professor> getAll() {
        return entityManager.createQuery("SELECT p FROM Professor p", Professor.class)
                .getResultList();
    }

    public List<Professor> getPage(int page, int size) {
        return entityManager
                .createQuery("SELECT p FROM Professor p", Professor.class)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();}


    public Long countAll() {
        return entityManager
                .createQuery("SELECT COUNT(p) FROM Professor p", Long.class)
                .getSingleResult();
    }

    public Professor create(Professor actualProProfessor) {
        entityManager.persist(actualProProfessor);
        return actualProProfessor;
    }


    public Professor update(Professor actualProfessor) {
        entityManager.merge(actualProfessor);
        return actualProfessor;
    }

    public void delete(Integer id) {
        Professor professor = entityManager.find(Professor.class, id);
        if (professor != null) {
            entityManager.remove(professor);
        }
    }

    public List<Professor> findByLastName(String lastName, int page, int size) {
        return entityManager
                .createQuery("SELECT p FROM Professor p WHERE p.lastName = :lastName",
                        Professor.class)
                .setParameter("lastName", lastName)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    public Long countByLastName(String lastName) {
        return entityManager
                .createQuery("SELECT COUNT(p) FROM Professor p WHERE LOWER(p.lastName) LIKE :lastName",
                        Long.class)
                .setParameter("lastName", "%" + lastName.toLowerCase() + "%")
                .getSingleResult();
    }

    public List<Professor> findByFirstName(String firstName, int page, int size) {
        return entityManager
                .createQuery("SELECT p FROM Professor p WHERE p.firstName = :firstName",
                        Professor.class)
                .setParameter("firstName", firstName)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();

    }

    public Long countByFirstName(String firstName) {
        return entityManager
                .createQuery("SELECT COUNT(p) FROM Professor p WHERE LOWER(p.firstName) LIKE :firstName",
                        Long.class)
                .setParameter("firstName", "%" + firstName.toLowerCase() + "%")
                .getSingleResult();
    }

    public List<Professor> findByFirstAndLastName(String firstName, String lastName, int page, int size) {
        return entityManager
                .createQuery("SELECT p FROM Professor p WHERE p.firstName = :firstName AND p.lastName = :lastName"
                        , Professor.class)
                .setParameter("firstName", firstName)
                .setParameter("lastName", lastName)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();

    }

    public Long countByFirstAndLastName(String firstName, String lastName) {
        return entityManager
                .createQuery("SELECT COUNT(p) FROM Professor p WHERE LOWER(p.firstName) LIKE :firstName AND LOWER(p.lastName) LIKE :lastName",
                        Long.class)
                .setParameter("firstName", "%" + firstName.toLowerCase() + "%")
                .setParameter("lastName", lastName)
                .getSingleResult();
    }




}



