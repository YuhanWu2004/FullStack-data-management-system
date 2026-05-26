package com.cicad.app.repository;

import com.cicad.app.entities.Course;
import com.cicad.app.entities.Program;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Repository
@Transactional
public class ProgramRepository {
    @PersistenceContext
    private EntityManager entityManager;

    public Program get(Integer id) {
        return entityManager.find(Program.class, id);
    }

    public List<Program> getAll() {
        return entityManager
                .createQuery("SELECT p FROM Program p", Program.class).getResultList();
    }

    public List<Program> getPage(int page, int size) {
        return entityManager
                .createQuery("SELECT p FROM Program p", Program.class)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    public Long countAll() {
        return entityManager
                .createQuery("SELECT COUNT(p) FROM Program p", Long.class)
                .getSingleResult();
    }

    public List<Program> findByName(String name, int page, int size) {
        return entityManager
                .createQuery(
                        "SELECT p FROM Program p WHERE LOWER(p.name) LIKE :name",
                        Program.class)
                .setParameter("name", "%" + name.toLowerCase() + "%")
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    public Long countByName(String name) {
        return entityManager
                .createQuery(
                        "SELECT COUNT(p) FROM Program p WHERE LOWER(p.name) LIKE :name",
                        Long.class)
                .setParameter("name", "%" + name.toLowerCase() + "%")
                .getSingleResult();
    }

    public Program create(Program actualProgram) {
        entityManager.persist(actualProgram);
        return actualProgram;
    }

    public Program update(Program actualProgram) {
        entityManager.merge(actualProgram);
        return actualProgram;
    }

    public void delete(Integer id) {
        Program existingProgram = entityManager.find(Program.class, id);
        if (existingProgram != null) {
            entityManager.remove(existingProgram);
        }
    }
    public Program findByProgramName(String programName) {
        return entityManager
                .createQuery("SELECT p FROM Program p WHERE p.name = :programName", Program.class)
                .setParameter("programName", programName).getSingleResult();
    }



}
