package com.cicad.app.service;

import com.cicad.app.entities.Program;
import com.cicad.app.repository.ProgramRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class ProgramService {

    @Autowired
    private ProgramRepository programRepository;

    public Program get(Integer id) {return programRepository.get(id);}
    public List<Program> getAll() {return programRepository.getAll();}

    public Program create(Program sourceProgram) {
        Program actualProgram = new Program();
        if (sourceProgram.getName() == null ) {
            throw new RuntimeException("Program name is now provided!");
        }
        actualProgram.setName(sourceProgram.getName());

        return programRepository.create(actualProgram);
    }

    public Program update(Program sourceProgram) {
        Program existingProgram = programRepository.get(sourceProgram.getId());
        if (existingProgram == null) {
            throw new RuntimeException("Program not found with id: " + sourceProgram.getId());
        }
        return programRepository.update(sourceProgram);
    }

    public void delete(Integer id) {
        Program existingProgram = programRepository.get(id);
        if (existingProgram != null) {
            programRepository.delete(id);
        }

    }

    public Map<String, Object> getProgramsPaginated(int page, int size) {
        List<Program> programs = programRepository.getPage(page, size);
        Long total = programRepository.countAll();
        Map<String, Object> result = new HashMap<>();
        result.put("programs", programs);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", (int) Math.ceil((double) total / size));
        return result;
    }


    public Map<String, Object> searchByName(String name, int page, int size) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        List<Program> programs = programRepository.findByName(name, page, size);
        Long total = programRepository.countByName(name);

        Map<String, Object> result = new HashMap<>();
        result.put("programs", programs);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", (int) Math.ceil((double) total / size));
        return result;
    }

    public Map<String, Object> searchById(Integer id) {
        Program programs = programRepository.get(id);

        Map<String, Object> result = new HashMap<>();
        if (programs != null) {
            result.put("programs", List.of(programs));
            result.put("total", 1L);
            result.put("page", 0);
            result.put("size", 1);
            result.put("totalPages", 1);
        } else {
            result.put("programs", List.of());
            result.put("total", 0L);
            result.put("page", 0);
            result.put("size", 1);
            result.put("totalPages", 0);
        }
        return result;
    }


    public Program findByProgramName(String name) {
        if (name == null || name.isEmpty()) {
            throw new RuntimeException("Program not found with name: " + name);
        }
        return programRepository.findByProgramName(name);
    }

}
