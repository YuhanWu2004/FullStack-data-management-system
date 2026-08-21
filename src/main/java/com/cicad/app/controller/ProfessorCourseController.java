package com.cicad.app.controller;

import com.cicad.app.entities.ProfessorCourse;
import com.cicad.app.service.ProfessorCourseService;
import com.cicad.app.service.ProfessorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * Teaching assignments. Deciding who teaches what is a registrar action, so everything
 * here is staff-only except two reads: a professor may list their own assignments (that is
 * what ProfessorProfileView shows), and anyone signed in may see who teaches a course.
 */
@RestController
@RequestMapping("api/assignment")
public class ProfessorCourseController {

    @Autowired
    private ProfessorCourseService professorCourseService;

    @PreAuthorize("hasRole('STAFF')")
    @RequestMapping(method = RequestMethod.GET)
    public Object getAll(@RequestParam(defaultValue = "0") int page,
                         @RequestParam(defaultValue = "10") int size) {
        return professorCourseService.getProfessorCoursePaginated(page, size);}

    @PreAuthorize("hasRole('STAFF')")
    @RequestMapping(value="/{id}", method = RequestMethod.GET)
    public Object get(@PathVariable Integer id) {return professorCourseService.findById(id);}


    @PreAuthorize("hasRole('STAFF')")
    @RequestMapping(value="/{id}",  method = RequestMethod.DELETE)
    public Object delete(@PathVariable Integer id) {
        professorCourseService.delete(id);
        return "Teaching assignment deleted Successfully";
    }

    @PreAuthorize("hasRole('STAFF')")
    @RequestMapping(method = RequestMethod.PUT)
    public Object update(@RequestBody ProfessorCourse sourceProfessorCourse) {
        return professorCourseService.update(sourceProfessorCourse);
    }

    @PreAuthorize("hasRole('STAFF')")
    @RequestMapping(method = RequestMethod.POST)
    public Object create(@RequestBody ProfessorCourse sourceProfessorCourse) {
        return professorCourseService.create(sourceProfessorCourse);
    }

    // A professor's own teaching list, or anything staff asks for.
    @PreAuthorize("@access.canViewProfessorAssignments(#value)")
    @RequestMapping(value="/search/professorId", method = RequestMethod.GET)
    public Object findByProfessorId(@RequestParam Integer value,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "10") int size) {
        return professorCourseService.findByProfessorId(value, page, size);
    }

    // Who teaches this course — visible to any signed-in user.
    @RequestMapping(value="/search/courseId", method = RequestMethod.GET)
    public Object findByCourseId(@RequestParam Integer value,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "10") int size) {
        return professorCourseService.findByCourseId(value, page, size);
    }

    @PreAuthorize("@access.canViewProfessorAssignments(#professorId)")
    @RequestMapping(value="/search/ProfessorIdAndCourseId", method = RequestMethod.GET)
    public Object findByProfessorId(@RequestParam Integer professorId,
                                    @RequestParam Integer courseId,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "10") int size) {
        return professorCourseService.findByProfessorIdAndCourseId(professorId, courseId, page, size);
    }

    @PreAuthorize("hasRole('STAFF')")
    @RequestMapping(value = "/search/professorName", method = RequestMethod.GET)
    public Object searchByProfessor(
            @RequestParam(required = false, defaultValue = "") String professorName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return professorCourseService.searchByProfessorName(professorName, page, size);
    }

    @PreAuthorize("hasRole('STAFF')")
    @RequestMapping(value = "/search/courseName", method = RequestMethod.GET)
    public Object searchByCourse(
            @RequestParam(required = false, defaultValue = "") String courseName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return professorCourseService.searchByCourseName(courseName, page, size);
    }



}
