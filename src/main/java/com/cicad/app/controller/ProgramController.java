package com.cicad.app.controller;

import com.cicad.app.entities.Program;
import com.cicad.app.service.ProgramService;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Same shape as CourseController: programs are catalogue data any signed-in user may read,
 * and only staff may change.
 */
@RestController
@RequestMapping("api/program")
public class ProgramController {


    @Autowired
    private ProgramService programService;

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Object get(@PathVariable Integer id) {
        return programService.get(id);
    }

    // GET api/course?page=0&size=10
    @RequestMapping(method = RequestMethod.GET)
    public Object getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return programService.getProgramsPaginated(page, size);
    }

    @PreAuthorize("hasRole('STAFF')")
    @RequestMapping(method = RequestMethod.POST)
    public Object create(@RequestBody Program sourceProgram) {
        return programService.create(sourceProgram);
    }

    @PreAuthorize("hasRole('STAFF')")
    @RequestMapping(method = RequestMethod.PUT)
    public Object update(@RequestBody Program sourceProgram) {
        return programService.update(sourceProgram);
    }

    @PreAuthorize("hasRole('STAFF')")
    @RequestMapping(value = "/{id}",method = RequestMethod.DELETE)
    public Object delete(@PathVariable Integer id) {
        programService.delete(id);
        return "Program deleted Successfully";
    }

    @RequestMapping(value ="search/name", method = RequestMethod.GET)
    public Object searchByName(
            @RequestParam String value,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return programService.searchByName(value, page, size);
    }

    @RequestMapping(value ="search/id", method = RequestMethod.GET)
    public Object searchById(
            @RequestParam Integer value) {
        return programService.searchById(value);
    }

}
