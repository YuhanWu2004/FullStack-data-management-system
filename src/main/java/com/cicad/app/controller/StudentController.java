package com.cicad.app.controller;

import com.cicad.app.entities.Student;
import com.cicad.app.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/student")
public class StudentController {

	@Autowired
	private StudentService studentService;

	@RequestMapping(method = RequestMethod.GET)
	public Object getAll(@RequestParam(defaultValue = "0") int page,
	                     @RequestParam(defaultValue = "10") int size) {
		return studentService.getStudentsPaginated(page, size);}

	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public Object get(@PathVariable Integer id) {
		return studentService.findById(id);
	}

	@RequestMapping(method = RequestMethod.POST)
	public Object create(@RequestBody Student sourceStudent) {
		return studentService.create(sourceStudent);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
	public Object delete(@PathVariable Integer id) {
		studentService.delete(id);
		return "Student deleted Successfully";
	}

	@RequestMapping(method = RequestMethod.PUT)
	public Object update(@RequestBody Student sourceStudent) {
		return studentService.update(sourceStudent);
	}

	@RequestMapping(value = "/search/lastName", method= RequestMethod.GET)
	public Object findByLastName(@RequestParam String value,
	                             @RequestParam(defaultValue = "0") int page,
	                             @RequestParam(defaultValue = "10") int size) {
		return studentService.searchByLastName(value, page, size);
	}

	@RequestMapping(value = "/search/firstName", method= RequestMethod.GET)
	public Object findByFirstName(@RequestParam String value,
	                              @RequestParam(defaultValue = "0") int page,
	                              @RequestParam(defaultValue = "10") int size) {
		return studentService.searchByFirstName(value, page, size);
	}

	@RequestMapping(value = "/search/prorgamId", method= RequestMethod.GET)
	public Object findByProgram(@RequestParam Integer value,
	                            @RequestParam(defaultValue = "0") int page,
	                            @RequestParam(defaultValue = "10") int size) {
		return studentService.findByProgramId(value, page, size);
	}

	@RequestMapping(value = "/search/gpaGreater", method= RequestMethod.GET)
	public Object findGpaGreaterOrEqualThan(@RequestParam Integer value,
	                                        @RequestParam(defaultValue = "0") int page,
	                                        @RequestParam(defaultValue = "10") int size) {
		return studentService.findGpaGreaterOrEqualThan(value, page, size);
	}


}
