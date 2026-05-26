package com.cicad.app.service;

import com.cicad.app.entities.Course;
import com.cicad.app.entities.Student;
import com.cicad.app.entities.StudentCourse;
import com.cicad.app.repository.CourseRepository;
import com.cicad.app.repository.StudentCourseRepository;
import com.cicad.app.repository.StudentRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class StudentCourseService {
    @Autowired
    private StudentCourseRepository studentCourseRepository;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private StudentRepository studentRepository;

    public StudentCourse get(Integer id) {return studentCourseRepository.get(id);}

    public Map<String, Object> findById(Integer id) {
        StudentCourse enrollment = studentCourseRepository.get(id);
        Map<String, Object> result = new HashMap<>();
        if (enrollment != null) {
            result.put("enrollments", List.of(enrollment));
            result.put("total", 1L);
            result.put("page", 0);
            result.put("size", 1);
            result.put("totalPages", 1);
        } else {
            result.put("enrollments", List.of(enrollment));
            result.put("total", 0L);
            result.put("page", 0);
            result.put("size", 0);
            result.put("totalPages", 0);

        }
        return result;

    }


    public Map<String, Object> getStudentCoursePaginated(int page, int size) {
        List<StudentCourse> enrollments = studentCourseRepository.getPage(page, size);
        Long total = studentCourseRepository.countAll();

        Map<String, Object> result = new HashMap<>();
        result.put("enrollments", enrollments);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", (int) Math.ceil((double) total / size));
        return result;
    }

    public List<StudentCourse> getAll() {return studentCourseRepository.getAll();}

    public StudentCourse create(StudentCourse sourceStudentCourse) {
        StudentCourse actualStudentCourse = new StudentCourse();

        // validate and set Student
        if (sourceStudentCourse.getStudent() == null ||
                sourceStudentCourse.getStudent().getId() == null) {
            throw new RuntimeException("Student id is required");
        }
        Student student = studentRepository.get(sourceStudentCourse.getStudent().getId());
        if (student == null) {
            throw new RuntimeException("Student not found");
        }
        actualStudentCourse.setStudent(student);

        // validate and set Course
        if (sourceStudentCourse.getCourse() == null ||
                sourceStudentCourse.getCourse().getId() == null) {
            throw new RuntimeException("Course id is required");
        }
        Course course = courseRepository.get(sourceStudentCourse.getCourse().getId()); // ← fixed
        if (course == null) {
            throw new RuntimeException("Course not found");
        }
        actualStudentCourse.setCourse(course);

        // set grade only if provided and valid
        Float grade = sourceStudentCourse.getGrade();
        if (grade != null) {                          // ← check null first
            if (grade < 0 || grade > 100) {
                throw new RuntimeException("Grade must be between 0 and 100");
            }
            actualStudentCourse.setGrade(grade);
        }

        return studentCourseRepository.create(actualStudentCourse);
    }

    public StudentCourse update(StudentCourse sourceStudentCourse) {
        StudentCourse existingEnrollment = studentCourseRepository.get(sourceStudentCourse.getId());
        if (existingEnrollment == null) {
            throw new RuntimeException("Student-Course enrollment not found with id: " + sourceStudentCourse.getId());
        }
        return studentCourseRepository.update(sourceStudentCourse);
    }

    public void delete(Integer id) {
        StudentCourse existingEnrollment = studentCourseRepository.get(id);
        if (existingEnrollment != null) {
            studentCourseRepository.delete(id);
        }
    }

    public   Map<String, Object> findByCourseId(Integer courseId, int page, int size) {
        Map<String, Object> result = new HashMap<>();
        Course course = courseRepository.get(courseId);

        if (course == null) {
            result.put("enrollments", new ArrayList<>());
            result.put("total", 0L);
            result.put("page", page);
            result.put("size", size);
            result.put("totalPages", 0);
            return result;
        }
        List<StudentCourse> enrollments = studentCourseRepository.findByCourseId(courseId, page, size);
        Long total = studentCourseRepository.CountByCourseId(courseId);
        result.put("enrollments", enrollments);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", (int) Math.ceil((double) total / size));
        return result;
    }


    public   Map<String, Object> findByStudentId(Integer studentId, int page, int size) {
        Map<String, Object> result = new HashMap<>();

        Student student = studentRepository.get(studentId);
        if (student == null) {
            result.put("enrollments", new ArrayList<>());
            result.put("total", 0L);
            result.put("page", page);
            result.put("size", size);
            result.put("totalPages", 0);
            return result;
        }

        List<StudentCourse> enrollments = studentCourseRepository.findByStudentId(studentId, page, size);
        Long total = studentCourseRepository.countByStudentId(studentId);

        result.put("enrollments", enrollments);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", (int) Math.ceil((double) total / size));

        return result;
    }

    public Map<String, Object> findByStudentIdAndCourseId(Integer studentId, Integer courseId, int page, int size) {
        Student student = studentRepository.get(studentId);
        Course course = courseRepository.get(courseId);
        Map<String, Object> result = new HashMap<>();
        if (student == null || course == null) {
            result.put("enrollments", new ArrayList<>());
            result.put("total", 0L);
            result.put("page", page);
            result.put("size", size);
            result.put("totalPages", 0);
            return result;
        }
        List<StudentCourse> enrollments = studentCourseRepository.findByStudentIdAndCourseId(studentId, courseId, page, size);


        if (enrollments == null) {
            result.put("enrollments", new ArrayList<>());
            result.put("total", 0L);
            result.put("page", page);
            result.put("size", size);
            result.put("totalPages", 0);
            return result;
        }

        result.put("enrollments", enrollments);
        result.put("total", 1L);
        result.put("page", 0);
        result.put("size", 1);
        result.put("totalPages", 1);
        return result;
    }

    public Map<String, Object> findByStudentName(String studentName, int page, int size) {
        Map<String, Object> result = new HashMap<>();

        // If the search string is empty/null, return empty results or redirect to getStudentCoursePaginated
        if (studentName == null || studentName.trim().isEmpty()) {
            return getStudentCoursePaginated(page, size);
        }

        List<StudentCourse> enrollments = studentCourseRepository.findByStudentName(studentName, page, size);
        Long total = studentCourseRepository.countByStudentName(studentName);

        result.put("enrollments", enrollments);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", total == 0 ? 0 : (int) Math.ceil((double) total / size));

        return result;
    }

    public Map<String, Object> findByCourseName(String courseName, int page, int size) {
        Map<String, Object> result = new HashMap<>();

        if (courseName == null || courseName.trim().isEmpty()) {
             return getStudentCoursePaginated(page, size);
        }

        List<StudentCourse> enrollments = studentCourseRepository.findByCourseName(courseName, page, size);
        Long total = studentCourseRepository.countByCourseName(courseName);

        result.put("enrollments", enrollments);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", total == 0 ? 0 : (int) Math.ceil((double) total / size));

        return result;
    }


}
