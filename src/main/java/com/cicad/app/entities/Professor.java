package com.cicad.app.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.io.Serial;
import java.util.List;

@Entity
@Table(name = "PROFESSORS")
public class Professor {

    @Id
    @Column(name = "ID", nullable = false, unique = true, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "FIRST_NAME")
    private String firstName;

    @Column(name = "LAST_NAME")
    private String lastName;

    @ManyToOne
    @JoinColumn(name = "PROGRAM_ID")
    private Program program;

    @OneToMany(mappedBy = "professor")
    @JsonIgnore
    private List<ProfessorCourse> professorCourses;



    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public List<ProfessorCourse> getProfessorCourses() {
        return professorCourses;
    }

    public void setProfessorCourses(List<ProfessorCourse> professorCourses) {
        this.professorCourses = professorCourses;
    }

    public Program getProgram() {
        return program;
    }

    public void setProgram(Program program) {
        this.program = program;
    }
}
