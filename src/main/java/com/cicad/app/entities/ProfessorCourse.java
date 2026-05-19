package com.cicad.app.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "PROFESSOR_COURSES")
public class ProfessorCourse {

    @Id
    @Column(name = "ID", nullable = false, unique = true, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

     @ManyToOne
     @JoinColumn(name = "PROFESSOR_ID")
     private Professor professor;

     @ManyToOne
     @JoinColumn(name = "COURSE_ID")
     private Course course;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Professor getProfessor() {
        return professor;
    }

    public void setProfessor(Professor professor) {
        this.professor = professor;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }
}
