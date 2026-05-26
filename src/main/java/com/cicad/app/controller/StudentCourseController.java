package com.cicad.app.controller;

import com.cicad.app.entities.StudentCourse;
import com.cicad.app.service.StudentCourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/enrollment")
public class StudentCourseController {
    @Autowired
    private StudentCourseService studentCourseService;

    @RequestMapping(method = RequestMethod.GET)
    public Object getAll(@RequestParam(defaultValue = "0") int page,
                         @RequestParam(defaultValue = "10") int size) {
        return studentCourseService.getStudentCoursePaginated(page, size);}

    @RequestMapping(value="/{id}", method = RequestMethod.GET)
    public Object get(@PathVariable Integer id) {
        return studentCourseService.findById(id);
    }

    @RequestMapping(method = RequestMethod.POST)
    public Object create(@RequestBody StudentCourse sourceStudentCourse) {
        return studentCourseService.create(sourceStudentCourse);
    }

    @RequestMapping(method = RequestMethod.PUT)
    public Object update(@RequestBody StudentCourse sourceStudentCourse) {
        return studentCourseService.update(sourceStudentCourse);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public Object delete(@PathVariable Integer id) {
        studentCourseService.delete(id);
        return "Enrollment deleted Successfully";
    }

    @RequestMapping(value="/search/studentId", method = RequestMethod.GET)
    public Object findByStudentId(@RequestParam Integer value,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "10") int size) {

        return studentCourseService.findByStudentId(value, page, size);
    }

    @RequestMapping(value="/search/studentName", method = RequestMethod.GET)
    public Object findByStudentName(@RequestParam String value,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "10") int size) {

        return studentCourseService.findByStudentName(value, page, size);
    }

    @RequestMapping(value="/search/courseName", method = RequestMethod.GET)
    public Object findByCourseName(@RequestParam String value,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "10") int size) {

        return studentCourseService.findByCourseName(value, page, size);
    }

    @RequestMapping(value="/search/courseId", method = RequestMethod.GET)
    public Object findByCourseId(@RequestParam Integer value,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "10") int size) {

        return studentCourseService.findByCourseId(value, page, size);
    }

    @RequestMapping(value="/search/StudentIdAndCourseId", method = RequestMethod.GET)
    public Object findByStudentIdAndCourseId(@RequestParam Integer studentId,
                                             @RequestParam Integer courseId,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "10") int size) {
        return studentCourseService.findByStudentIdAndCourseId(studentId, courseId, page, size);
    }

}
