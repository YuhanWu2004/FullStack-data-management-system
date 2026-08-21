package com.cicad.app.controller;

import com.cicad.app.entities.Student;
import com.cicad.app.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Authorization for this resource lives here rather than on the service, because the rules
 * that matter are about the id in the request — {@code @access.canViewStudent(#id)} — and
 * the controller is where that id is named. The service methods are reached only through
 * these handlers.
 */
@RestController
@RequestMapping("api/student")
public class StudentController {

	@Autowired
	private StudentService studentService;

	// Browsing the whole student body is a registrar task.
	@PreAuthorize("hasRole('STAFF')")
	@RequestMapping(method = RequestMethod.GET)
	public Object getAll(@RequestParam(defaultValue = "0") int page,
	                     @RequestParam(defaultValue = "10") int size) {
		return studentService.getStudentsPaginated(page, size);}

	// Staff, the student themselves, or a professor who teaches them. This is the endpoint
	// StudentProfileView calls, and the reason the id can no longer simply be swapped.
	@PreAuthorize("@access.canViewStudent(#id)")
	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public Object get(@PathVariable Integer id) {
		return studentService.findById(id);
	}

	@PreAuthorize("hasRole('STAFF')")
	@RequestMapping(method = RequestMethod.POST)
	public Object create(@RequestBody Student sourceStudent) {
		return studentService.create(sourceStudent);
	}

	@PreAuthorize("hasRole('STAFF')")
	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
	public Object delete(@PathVariable Integer id) {
		studentService.delete(id);
		return "Student deleted Successfully";
	}

	@PreAuthorize("hasRole('STAFF')")
	@RequestMapping(method = RequestMethod.PUT)
	public Object update(@RequestBody Student sourceStudent) {
		return studentService.update(sourceStudent);
	}

	// Every search below enumerates students, so all of them are staff-only. A student
	// looking themselves up goes through GET /{id} with their own id.
	@PreAuthorize("hasRole('STAFF')")
	@RequestMapping(value = "/search/name", method= RequestMethod.GET)
	public Object findByName(@RequestParam String value,
	                             @RequestParam(defaultValue = "0") int page,
	                             @RequestParam(defaultValue = "10") int size) {
		return studentService.searchByName(value, page, size);
	}

	@PreAuthorize("hasRole('STAFF')")
	@RequestMapping(value = "/search/lastName", method= RequestMethod.GET)
	public Object findByLastName(@RequestParam String value,
	                             @RequestParam(defaultValue = "0") int page,
	                             @RequestParam(defaultValue = "10") int size) {
		return studentService.searchByLastName(value, page, size);
	}

	@PreAuthorize("hasRole('STAFF')")
	@RequestMapping(value = "/search/firstName", method= RequestMethod.GET)
	public Object findByFirstName(@RequestParam String value,
	                              @RequestParam(defaultValue = "0") int page,
	                              @RequestParam(defaultValue = "10") int size) {
		return studentService.searchByFirstName(value, page, size);
	}

	@PreAuthorize("hasRole('STAFF')")
	@RequestMapping(value = "/search/prorgamId", method= RequestMethod.GET)
	public Object findByProgram(@RequestParam Integer value,
	                            @RequestParam(defaultValue = "0") int page,
	                            @RequestParam(defaultValue = "10") int size) {
		return studentService.findByProgramId(value, page, size);
	}

	@PreAuthorize("hasRole('STAFF')")
	@RequestMapping(value = "/search/gpaGreater", method= RequestMethod.GET)
	public Object findGpaGreaterOrEqualThan(@RequestParam Integer value,
	                                        @RequestParam(defaultValue = "0") int page,
	                                        @RequestParam(defaultValue = "10") int size) {
		return studentService.findGpaGreaterOrEqualThan(value, page, size);
	}


}
