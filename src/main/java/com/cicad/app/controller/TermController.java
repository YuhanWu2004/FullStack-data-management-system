package com.cicad.app.controller;

import com.cicad.app.entities.Term;
import com.cicad.app.service.TermService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Terms are catalogue data, same shape as Course/Program: any signed-in user needs to read
 * the list for a term selector, only staff may create, edit, delete, or change which term
 * is current.
 */
@RestController
@RequestMapping("api/term")
public class TermController {

	@Autowired
	private TermService termService;

	@RequestMapping(method = RequestMethod.GET)
	public Object getAll() {
		return termService.getAll();
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public Object get(@PathVariable Integer id) {
		return termService.get(id);
	}

	@RequestMapping(value = "/current", method = RequestMethod.GET)
	public Object getCurrent() {
		return termService.getCurrent();
	}

	@PreAuthorize("hasRole('STAFF')")
	@RequestMapping(method = RequestMethod.POST)
	public Object create(@RequestBody Term sourceTerm) {
		return termService.create(sourceTerm);
	}

	@PreAuthorize("hasRole('STAFF')")
	@RequestMapping(method = RequestMethod.PUT)
	public Object update(@RequestBody Term sourceTerm) {
		return termService.update(sourceTerm);
	}

	@PreAuthorize("hasRole('STAFF')")
	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
	public Object delete(@PathVariable Integer id) {
		termService.delete(id);
		return "Term deleted successfully";
	}

	@PreAuthorize("hasRole('STAFF')")
	@RequestMapping(value = "/{id}/current", method = RequestMethod.PUT)
	public Object setCurrent(@PathVariable Integer id) {
		return termService.setCurrent(id);
	}
}
