package com.cicad.app.controller;

import com.cicad.app.entities.Course;
import com.cicad.app.service.CourseService;
import com.cicad.app.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * The course catalogue is readable by anyone signed in — students and professors both need
 * to browse it — so the read handlers carry no annotation and fall through to the
 * "/api/** is authenticated" rule in SecurityConfig. Only the writes narrow to staff.
 */
@RestController
@RequestMapping("api/course")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Object get(@PathVariable Integer id) {
        return courseService.get(id);
    }

    @PreAuthorize("hasRole('STAFF')")
    @RequestMapping(method = RequestMethod.POST)
    public Object create(@RequestBody Course sourceCourse) {
        return courseService.create(sourceCourse);
    }

    @PreAuthorize("hasRole('STAFF')")
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public Object delete(@PathVariable Integer id) {
        courseService.delete(id);
        return "Course deleted Successfully";
    }

    @PreAuthorize("hasRole('STAFF')")
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
