package com.cicad.app.controller;

import com.cicad.app.entities.Professor;
import com.cicad.app.service.ProfessorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.OptionalInt;

@RestController
@RequestMapping("api/professor")
public class ProfessorController {
    @Autowired
    private ProfessorService professorService;

    @RequestMapping(value="/{id}", method = RequestMethod.GET)
    public Object get(@PathVariable Integer id) {return professorService.findById(id);}

    @RequestMapping(method = RequestMethod.GET)
    public Object getAll(@RequestParam(defaultValue = "0") int page,
                         @RequestParam(defaultValue = "10") int size) {
        return professorService.getProfessorsPaginated(page, size);}

    @RequestMapping(method = RequestMethod.POST)
    public Object create(@RequestBody Professor sourceprofessor) {
        return professorService.create(sourceprofessor);
    }

    @RequestMapping(method = RequestMethod.PUT)
    public Object update(@RequestBody Professor sourceprofessor) {
        return professorService.update(sourceprofessor);
    }

    @RequestMapping(value="/{id}", method = RequestMethod.DELETE)
    public Object delete(@PathVariable Integer id) {
        professorService.delete(id);
        return "Professor deleted Successfully";
    }

    @RequestMapping(value="/search/firstName", method = RequestMethod.GET)
    public Object findByFirstName(@RequestParam String firstName,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "10") int size) {
        return professorService.searchByFirstName(firstName, page, size);
    }

    @RequestMapping(value="/search/lastName", method = RequestMethod.GET)
    public Object findByLastName(@RequestParam String lastName,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "10") int size){

        return professorService.searchByLastName(lastName, page, size);
    }

    @RequestMapping(value="/search/name", method = RequestMethod.GET)
    public Object searchByName(@RequestParam String value,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int size) {
        
        return professorService.searchByName(value, page, size);
    }
}
