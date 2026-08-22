package com.cicad.app.service;

import com.cicad.app.entities.*;
import com.cicad.app.repository.CourseRepository;
import com.cicad.app.repository.ProfessorCourseRepository;
import com.cicad.app.repository.ProfessorRepository;
import com.cicad.app.repository.TermRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class ProfessorCourseService {
    @Autowired
    private ProfessorCourseRepository professorCourseRepository;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private ProfessorRepository professorRepository;
    @Autowired
    private TermRepository termRepository;

    public ProfessorCourse get(Integer id) {
        return professorCourseRepository.get(id);
    }

    public List<ProfessorCourse> getAll() {
        return professorCourseRepository.getAll();
    }

    public Map<String, Object> findById(Integer id) {
        ProfessorCourse assignments = professorCourseRepository.get(id);
        Map<String, Object> result = new HashMap<>();
        if (assignments != null) {
            result.put("assignments", List.of(assignments));
            result.put("total", 1L);
            result.put("page", 0);
            result.put("size", 1);
            result.put("totalPages", 1);
        } else {
            result.put("assignments", List.of(assignments));
            result.put("total", 0L);
            result.put("page", 0);
            result.put("size", 0);
            result.put("totalPages", 0);
        }
        return result;

    }

    public Map<String, Object> getProfessorCoursePaginated(int page, int size, Integer termId) {
        List<ProfessorCourse> assignments = professorCourseRepository.getPage(page, size, termId);
        Long total = professorCourseRepository.countAll(termId);

        Map<String, Object> result = new HashMap<>();
        result.put("assignments", assignments);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", (int) Math.ceil((double) total / size));
        return result;
    }

    public ProfessorCourse create(ProfessorCourse sourceProfessorCourse) {
        ProfessorCourse actualProfessorCourse = new ProfessorCourse();
        if (sourceProfessorCourse.getCourse() == null || sourceProfessorCourse.getCourse().getId() == null) {
            throw new RuntimeException("Failed to create professor - course assignment, course id is required.");
        }
        Course course = courseRepository.get(sourceProfessorCourse.getCourse().getId());
        actualProfessorCourse.setCourse(course);

        if (sourceProfessorCourse.getProfessor() == null || sourceProfessorCourse.getProfessor().getId() == null) {
            throw new RuntimeException("Failed to create professor - course assignment, professor id is required.");
        }
        Professor professor = professorRepository.get(sourceProfessorCourse.getProfessor().getId());
        actualProfessorCourse.setProfessor(professor);

        // term: an explicit id lets staff assign into a specific (e.g. past) term;
        // otherwise default to whichever term is current.
        Term term;
        if (sourceProfessorCourse.getTerm() != null && sourceProfessorCourse.getTerm().getId() != null) {
            term = termRepository.get(sourceProfessorCourse.getTerm().getId());
            if (term == null) {
                throw new RuntimeException("Term not found");
            }
        } else {
            term = termRepository.findCurrent();
            if (term == null) {
                throw new RuntimeException("No current term is set — ask staff to set one before assigning professors");
            }
        }
        actualProfessorCourse.setTerm(term);

        return professorCourseRepository.create(actualProfessorCourse);
    }

    public ProfessorCourse update(ProfessorCourse sourceProfessorCourse) {
        ProfessorCourse professorCourse = professorCourseRepository.get(sourceProfessorCourse.getId());
        if (professorCourse == null){
            throw new RuntimeException("Professor-Course assignment is not found with id: " + sourceProfessorCourse.getId());
        }
        return professorCourseRepository.update(sourceProfessorCourse);
    }

    public void delete(Integer id) {
        ProfessorCourse professorCourse = professorCourseRepository.get(id);
        if (professorCourse != null){
            professorCourseRepository.delete(id);
        }
    }

    public Map<String, Object> findByProfessorId(Integer id, int page, int size, Integer termId) {
        Map<String, Object> result = new HashMap<>();
        Professor professor = professorRepository.get(id);
        if (professor == null) {
            result.put("assignments", new ArrayList<>());
            result.put("total", 0L);
            result.put("page", page);
            result.put("size", size);
            result.put("totalPages", 0);
            return result;
        }
        List<ProfessorCourse> assignments = professorCourseRepository.findByProfessorId(id, page, size, termId);
        Long total = professorCourseRepository.countByProfessorId(id, termId);
        result.put("assignments", assignments);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", (int) Math.ceil((double) total / size));
        return result;
    }

    public Map<String, Object> findByCourseId(Integer id, int page, int size) {
        Map<String, Object> result = new HashMap<>();
        Course course = courseRepository.get(id);
        if (course == null) {
            result.put("assignments", new ArrayList<>());
            result.put("total", 0L);
            result.put("page", page);
            result.put("size", size);
            result.put("totalPages", 0);
            return result;
        }
        List<ProfessorCourse> assignments = professorCourseRepository.findByCourseId(id, page, size);
        Long total = professorCourseRepository.countByCourseId(id);
        result.put("assignments", assignments);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", (int) Math.ceil((double) total / size));
        return result;
    }

    public Map<String, Object> findByProfessorIdAndCourseId (Integer professorId, Integer courseId, int page, int size) {
        Professor professor = professorRepository.get(professorId);
        Course course = courseRepository.get(courseId);
        Map<String, Object> result = new HashMap<>();
        if (professor == null || course == null) {
            result.put("assignments", new ArrayList<>());
            result.put("total", 0L);
            result.put("page", page);
            result.put("size", size);
            result.put("totalPages", 0);
            return result;
        }
        List<ProfessorCourse> assignments = professorCourseRepository.findByProfessorIdAndCourseId(professorId, courseId, page, size);
        if (assignments == null) {
            result.put("assignments", new ArrayList<>());
            result.put("total", 0L);
            result.put("page", page);
            result.put("size", size);
            result.put("totalPages", 0);
            return result;
        }

        result.put("assignments", assignments);
        result.put("total", 1L);
        result.put("page", 0);
        result.put("size", 1);
        result.put("totalPages", 1);
        return result;
    }

    public Map<String, Object> searchByProfessorName(String professorName, int page, int size) {
        if (professorName == null || professorName.trim().isEmpty()) {
            return getProfessorCoursePaginated(page, size, null); // Assuming this is your "getAll" paginated method
        }

        List<ProfessorCourse> assignments = professorCourseRepository.findByProfessorName(professorName, page, size);
        Long total = professorCourseRepository.countByProfessorName(professorName);

        Map<String, Object> result = new HashMap<>();
        result.put("assignments", assignments); // Adjust key name to match your Vuex expectations
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", total == 0 ? 0 : (int) Math.ceil((double) total / size));

        return result;
    }

    public Map<String, Object> searchByCourseName(String courseName, int page, int size) {
        if (courseName == null || courseName.trim().isEmpty()) {
             return getProfessorCoursePaginated(page, size, null);
        }

        List<ProfessorCourse> assignments = professorCourseRepository.findByCourseName(courseName, page, size);
        Long total = professorCourseRepository.countByCourseName(courseName);

        Map<String, Object> result = new HashMap<>();
        result.put("assignments", assignments);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", total == 0 ? 0 : (int) Math.ceil((double) total / size));

        return result;
    }
}
