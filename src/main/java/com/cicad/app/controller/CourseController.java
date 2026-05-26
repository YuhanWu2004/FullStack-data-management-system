package com.cicad.app.controller;

import com.cicad.app.entities.Course;
import com.cicad.app.service.CourseService;
import com.cicad.app.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/course")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Object get(@PathVariable Integer id) {
        return courseService.get(id);
    }

    @RequestMapping(method = RequestMethod.POST)
    public Object create(@RequestBody Course sourceCourse) {
        return courseService.create(sourceCourse);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public Object delete(@PathVariable Integer id) {
        courseService.delete(id);
        return "Course deleted Successfully";
    }

    @RequestMapping(method = RequestMethod.PUT)
    public Object update(@RequestBody Course sourceCourse) {
        return courseService.update(sourceCourse);
    }

    // GET api/course?page=0&size=10
    @RequestMapping(method = RequestMethod.GET)
    public Object getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return courseService.getCoursesPaginated(page, size);
    }

    // ── SEARCH by name ────────────────────────
    // GET api/course/search/name?value=math&page=0&size=10
    @RequestMapping(value = "/search/name", method = RequestMethod.GET)
    public Object searchByName(
            @RequestParam String value,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return courseService.searchByName(value, page, size);
    }

    // ── SEARCH by id ──────────────────────────
    // GET api/course/search/id?value=231
    @RequestMapping(value = "/search/id", method = RequestMethod.GET)
    public Object searchById(@RequestParam Integer value) {
        return courseService.searchById(value);
    }




}
