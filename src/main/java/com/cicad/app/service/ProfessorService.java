package com.cicad.app.service;

import com.cicad.app.entities.Professor;
import com.cicad.app.entities.Program;
import com.cicad.app.repository.ProfessorRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class ProfessorService {

    @Autowired
    private ProfessorRepository professorRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public Professor get(Integer id) {return professorRepository.get(id);}

    public Map<String, Object> findById(Integer id) {
        Professor professor =  professorRepository.get(id);
        Map<String, Object> result = new HashMap<>();
        if (professor != null) {
            result.put("professors", List.of(professor));
            result.put("total", 1L);
            result.put("page", 0);
            result.put("size", 1);
            result.put("totalPages", 1);
        } else {
            result.put("professors", new ArrayList<>());
            result.put("total", 0L);
            result.put("page", 0);
            result.put("size", 0);
            result.put("totalPages", 0);
        }

        return result;
    }

    public Map<String, Object> getProfessorsPaginated(int page, int size) {
        List<Professor> professors = professorRepository.getPage(page, size);
        Long total = professorRepository.countAll();

        Map<String, Object> result = new HashMap<>();
        result.put("professors", professors);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", (int) Math.ceil((double) total / size));
        return result;
    }

    public List<Professor> getAll() {return professorRepository.getAll();}

    public Professor create(Professor sourceProfessor) {
        Professor actualProfessor = new Professor();
        if (sourceProfessor.getFirstName() == null || sourceProfessor.getLastName() == null) {
            throw new RuntimeException("Failed to create professor, first name and last name must be provided!");
        }
        actualProfessor.setFirstName(sourceProfessor.getFirstName());
        actualProfessor.setLastName(sourceProfessor.getLastName());
        if (sourceProfessor.getProgram() != null && sourceProfessor.getProgram().getId() != null) {
            // Program actualProgram = programRepository.find(sourceProfessor.getProgram().getId());
            // actualProfessor.setProgram(actualProgram);
            actualProfessor.setProgram(entityManager.find(Program.class, sourceProfessor.getProgram().getId()));
        }
        return professorRepository.create(actualProfessor);
    }

    public Professor update(Professor sourceProfessor) {
        Professor professor = professorRepository.get(sourceProfessor.getId());
        if (professor == null) {
            throw new RuntimeException("Professor not found with id: " + sourceProfessor.getId());
        }
        if (sourceProfessor.getProgram() != null && sourceProfessor.getProgram().getId() != null) {
            if (sourceProfessor.getProgram() != professor.getProgram()) {

                professor.setProgram(entityManager.find(Program.class, sourceProfessor.getProgram().getId()));
            }
        }
        if (sourceProfessor.getFirstName() != null) {
            professor.setFirstName(sourceProfessor.getFirstName());
        }
        if (sourceProfessor.getLastName() != null) {
            professor.setLastName(sourceProfessor.getLastName());
        }

        return professorRepository.update(professor);
    }
    public void delete(Integer id) {
        Professor existingProfessor = professorRepository.get(id);
        if (existingProfessor != null) {
            professorRepository.delete(id);
        }
    }

    public Map<String, Object> searchByLastName(String name, int page, int size) {
        if (name == null || name.isEmpty()) {
            throw new RuntimeException("Professor not found with name: " + name);
        }
        List<Professor> professors = professorRepository.findByLastName(name, page, size);
        Long total = professorRepository.countByLastName(name);
        Map<String, Object> result = new HashMap<>();
        result.put("professors", professors);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", (int) Math.ceil((double) total / size));
        return result;
    }

    public  Map<String, Object> searchByFirstName(String name, int page, int size) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        List<Professor> professors = professorRepository.findByFirstName(name, page, size);
        Long total = professorRepository.countByFirstName(name);
        Map<String, Object> result = new HashMap<>();
        result.put("professors", professors);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", (int) Math.ceil((double) total / size));
        return result;
    }



}
