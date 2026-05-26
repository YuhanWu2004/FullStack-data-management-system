package com.cicad.app.controller;

import com.cicad.app.entities.ProfessorCourse;
import com.cicad.app.service.ProfessorCourseService;
import com.cicad.app.service.ProfessorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/assignment")
public class ProfessorCourseController {

    @Autowired
    private ProfessorCourseService professorCourseService;

    @RequestMapping(method = RequestMethod.GET)
    public Object getAll(@RequestParam(defaultValue = "0") int page,
                         @RequestParam(defaultValue = "10") int size) {
        return professorCourseService.getProfessorCoursePaginated(page, size);}

    @RequestMapping(value="/{id}", method = RequestMethod.GET)
    public Object get(@PathVariable Integer id) {return professorCourseService.findById(id);}


    @RequestMapping(value="/{id}",  method = RequestMethod.DELETE)
    public Object delete(@PathVariable Integer id) {
        professorCourseService.delete(id);
        return "Teaching assignment deleted Successfully";
    }

    @RequestMapping(method = RequestMethod.PUT)
    public Object update(@RequestBody ProfessorCourse sourceProfessorCourse) {
        return professorCourseService.update(sourceProfessorCourse);
    }

    @RequestMapping(method = RequestMethod.POST)
    public Object create(@RequestBody ProfessorCourse sourceProfessorCourse) {
        return professorCourseService.create(sourceProfessorCourse);
    }

    @RequestMapping(value="/search/professorId", method = RequestMethod.GET)
    public Object findByProfessorId(@RequestParam Integer value,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "10") int size) {
        return professorCourseService.findByProfessorId(value, page, size);
    }

    @RequestMapping(value="/search/courseId", method = RequestMethod.GET)
    public Object findByCourseId(@RequestParam Integer value,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "10") int size) {
        return professorCourseService.findByCourseId(value, page, size);
    }

    @RequestMapping(value="/search/ProfessorIdAndCourseId", method = RequestMethod.GET)
    public Object findByProfessorId(@RequestParam Integer professorId,
                                    @RequestParam Integer courseId,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "10") int size) {
        return professorCourseService.findByProfessorIdAndCourseId(professorId, courseId, page, size);
    }



}
