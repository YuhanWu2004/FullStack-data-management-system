package com.cicad.app.controller;

import com.cicad.app.entities.StudentCourse;
import com.cicad.app.service.StudentCourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/enrollment")
public class StudentCourseController {
    @Autowired
    private StudentCourseService studentCourseService;

    @PreAuthorize("hasRole('STAFF')")
    @RequestMapping(method = RequestMethod.GET)
    public Object getAll(@RequestParam(defaultValue = "0") int page,
                         @RequestParam(defaultValue = "10") int size,
                         @RequestParam(required = false) Integer termId) {
        return studentCourseService.getStudentCoursePaginated(page, size, termId);}

    @PreAuthorize("@access.canViewEnrollment(#id)")
    @RequestMapping(value="/{id}", method = RequestMethod.GET)
    public Object get(@PathVariable Integer id) {
        return studentCourseService.findById(id);
    }

    // Enrolling and unenrolling is a registrar action, not something a professor or a
    // student does for themselves.
    @PreAuthorize("hasRole('STAFF')")
    @RequestMapping(method = RequestMethod.POST)
    public Object create(@RequestBody StudentCourse sourceStudentCourse) {
        return studentCourseService.create(sourceStudentCourse);
    }

    // The grading path. Staff always; a professor only for an enrollment in a course on
    // their own teaching assignments; a student never, not even their own record.
    @PreAuthorize("@access.canGradeEnrollment(#sourceStudentCourse.id)")
    @RequestMapping(method = RequestMethod.PUT)
    public Object update(@RequestBody StudentCourse sourceStudentCourse) {
        return studentCourseService.update(sourceStudentCourse);
    }

    @PreAuthorize("hasRole('STAFF')")
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public Object delete(@PathVariable Integer id) {
        studentCourseService.delete(id);
        return "Enrollment deleted Successfully";
    }

    // StudentProfileView's "my courses" list. Same ownership rule as reading the student.
    @PreAuthorize("@access.canViewStudent(#value)")
    @RequestMapping(value="/search/studentId", method = RequestMethod.GET)
    public Object findByStudentId(@RequestParam Integer value,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "10") int size,
                                  @RequestParam(required = false) Integer termId) {

        return studentCourseService.findByStudentId(value, page, size, termId);
    }

    @PreAuthorize("hasRole('STAFF')")
    @RequestMapping(value="/search/studentName", method = RequestMethod.GET)
    public Object findByStudentName(@RequestParam String value,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "10") int size) {

        return studentCourseService.findByStudentName(value, page, size);
    }

    @PreAuthorize("hasRole('STAFF')")
    @RequestMapping(value="/search/courseName", method = RequestMethod.GET)
    public Object findByCourseName(@RequestParam String value,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "10") int size) {

        return studentCourseService.findByCourseName(value, page, size);
    }

    // A course roster: staff, or the professor teaching that course. This is what a
    // gradebook screen would call.
    @PreAuthorize("@access.canViewCourseRoster(#value)")
    @RequestMapping(value="/search/courseId", method = RequestMethod.GET)
    public Object findByCourseId(@RequestParam Integer value,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "10") int size) {

        return studentCourseService.findByCourseId(value, page, size);
    }

    @PreAuthorize("@access.canViewStudent(#studentId)")
    @RequestMapping(value="/search/StudentIdAndCourseId", method = RequestMethod.GET)
    public Object findByStudentIdAndCourseId(@RequestParam Integer studentId,
                                             @RequestParam Integer courseId,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "10") int size) {
        return studentCourseService.findByStudentIdAndCourseId(studentId, courseId, page, size);
    }

}
