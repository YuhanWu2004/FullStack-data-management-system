package com.cicad.app.service;

import com.cicad.app.entities.Course;
import com.cicad.app.repository.CourseRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class CourseService {

    @Autowired
    private CourseRepository courseRepository;

    public Course get(Integer id) {
        return courseRepository.get(id);
    }

    public List<Course> getAll() {
        return courseRepository.getALl();
    }


    public Map<String, Object> getCoursesPaginated(int page, int size) {
        List<Course> courses = courseRepository.getPage(page, size);
        Long total = courseRepository.countAll();

        Map<String, Object> result = new HashMap<>();
        result.put("courses", courses);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", (int) Math.ceil((double) total / size));
        return result;
    }

    public Map<String, Object> searchByName(String name, int page, int size) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        List<Course> courses = courseRepository.findByName(name, page, size);
        Long total = courseRepository.countByName(name);

        Map<String, Object> result = new HashMap<>();
        result.put("courses", courses);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", (int) Math.ceil((double) total / size));
        return result;
    }

    public Map<String, Object> searchById(Integer id) {
        Course course = courseRepository.get(id);

        Map<String, Object> result = new HashMap<>();
        if (course != null) {
            result.put("courses", List.of(course));
            result.put("total", 1L);
            result.put("page", 0);
            result.put("size", 1);
            result.put("totalPages", 1);
        } else {
            result.put("courses", List.of());
            result.put("total", 0L);
            result.put("page", 0);
            result.put("size", 1);
            result.put("totalPages", 0);
        }
        return result;
    }



    public Map<String, Object> searchCourses(String name, int page, int size) {
        List<Course> courses = courseRepository.findByName(name, page, size);
        Long total = courseRepository.countByName(name);

        Map<String, Object> result = new HashMap<>();
        result.put("courses", courses);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", (int) Math.ceil((double) total / size));
        return result;
    }


    public Course create(Course sourceCourse) {
        Course actualCourse = new Course();
        if (sourceCourse.getName() == null) {
            throw new RuntimeException("Failed to create course, course name is not provided!");
        }
        actualCourse.setName(sourceCourse.getName());

        return courseRepository.create(actualCourse);
    }

    public Course update(Course sourceCourse) {
        Course existingCourse = courseRepository.get(sourceCourse.getId());
        if (existingCourse == null) {
            throw new RuntimeException("Course not found with id: " + sourceCourse.getId());
        }
        return courseRepository.update(sourceCourse);
    }

    public void delete(Integer id) {
        Course course = courseRepository.get(id);
        if (course != null ) {
            courseRepository.delete(id);
        }
    }

//    public List<Course> findByCourseName(String courseName) {
//        if (courseName != null || courseName.isEmpty()) {
//            throw new  RuntimeException("Student not found with last name: " + courseName);
//        }
//        return courseRepository.findByCourseName(courseName);
//    }



}
