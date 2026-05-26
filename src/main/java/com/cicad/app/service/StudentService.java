package com.cicad.app.service;

import com.cicad.app.entities.Course;
import com.cicad.app.entities.Student;
import com.cicad.app.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@Transactional
public class StudentService {

	@Autowired
	private StudentRepository studentRepository;

	public Student get(Integer id) {
		return studentRepository.get(id);
	}

	public Map<String, Object> findById(Integer id) {
		Student student =  studentRepository.get(id);
		Map<String, Object> result = new HashMap<>();
		if (student != null) {
			result.put("students", List.of(student));
			result.put("total", 1L);
			result.put("page", 0);
			result.put("size", 1);
			result.put("totalPages", 1);
		} else {
			result.put("students", new ArrayList<>());
			result.put("total", 0L);
			result.put("page", 0);
			result.put("size", 0);
			result.put("totalPages", 0);
		}

		return result;
	}

	public Map<String, Object> getStudentsPaginated(int page, int size) {
		List<Student> students = studentRepository.getPage(page, size);
		Long total = studentRepository.countAll();

		Map<String, Object> result = new HashMap<>();
		result.put("students", students);
		result.put("total", total);
		result.put("page", page);
		result.put("size", size);
		result.put("totalPages", (int) Math.ceil((double) total / size));
		return result;
	}

	public Map<String, Object> searchByFirstName(String name, int page, int size) {
		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("Name cannot be empty");
		}
		List<Student> students = studentRepository.findByFirstName(name, page, size);
		Long total = studentRepository.countByFirstName(name);

		Map<String, Object> result = new HashMap<>();
		result.put("students", students);
		result.put("total", total);
		result.put("page", page);
		result.put("size", size);
		result.put("totalPages", (int) Math.ceil((double) total / size));
		return result;
	}

	public Map<String, Object> searchByLastName(String name, int page, int size) {
		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("Name cannot be empty");
		}
		List<Student> students = studentRepository.findByLastName(name, page, size);
		Long total = studentRepository.countByLastName(name);

		Map<String, Object> result = new HashMap<>();
		result.put("students", students);
		result.put("total", total);
		result.put("page", page);
		result.put("size", size);
		result.put("totalPages", (int) Math.ceil((double) total / size));
		return result;
	}
	public Map<String, Object> findByProgramId(Integer programId, int page, int size) {
		List<Student> students =  studentRepository.findByProgramId(programId, page, size);
		Long total = 1L;
		Map<String, Object> result = new HashMap<>();
		result.put("students", students);
		result.put("total", total);
		result.put("page", page);
		result.put("size", size);
		result.put("totalPages", (int) Math.ceil((double) total / size));
		return result;
	}

	public Map<String, Object> findGpaGreaterOrEqualThan(Integer gpa,  int page, int size) {
		List<Student> students = studentRepository.findGpaGreaterOrEqualThan(gpa, page, size);
		Long total = studentRepository.countByGpaGreaterOrEqualThan(gpa, page, size);
		Map<String, Object> result = new HashMap<>();
		result.put("students", students);
		result.put("total", total);
		result.put("page", page);
		result.put("size", size);
		result.put("totalPages", (int) Math.ceil((double) total / size));
		return result;
	}

	public Student create(Student sourceStudent) {
		Student actualStudent = new Student();
		if (sourceStudent.getFirstName() == null || sourceStudent.getLastName() == null) {
			throw new RuntimeException("Failed to create a student, first Name or Last Name is not provided!");
		}
		actualStudent.setFirstName(sourceStudent.getFirstName());
		actualStudent.setLastName(sourceStudent.getLastName());

		if (sourceStudent.getDateOfBirth() == null
				|| sourceStudent.getDateOfBirth().isAfter(LocalDate.now())) {
			throw new RuntimeException("Failed to create a student, either the date of birth is not provided or the date of birth is not valid!");

		}
		actualStudent.setDateOfBirth(sourceStudent.getDateOfBirth());

		if (sourceStudent.getGpa() >= 0 && sourceStudent.getGpa() <= 4.0) {
			actualStudent.setGpa(sourceStudent.getGpa());
		}
		return studentRepository.create(actualStudent);
	}

	public Student update(Student sourceStudent) {
		Student existingStudent = studentRepository.get(sourceStudent.getId());
		if (existingStudent == null) {
			throw new RuntimeException("Student not found with id: " + sourceStudent.getId());
		}
		if (sourceStudent.getProgram() != null && sourceStudent.getProgram() != existingStudent.getProgram()) {
			existingStudent.setProgram(sourceStudent.getProgram());
		}
		if (sourceStudent.getFirstName() != null) {
			existingStudent.setFirstName(sourceStudent.getFirstName());
		}
		if (sourceStudent.getLastName() != null) {
			existingStudent.setLastName(sourceStudent.getLastName());
		}
		if (sourceStudent.getGpa() != null) {
			existingStudent.setGpa(sourceStudent.getGpa());
		}
		if (sourceStudent.getDateOfBirth() != null) {
			existingStudent.setDateOfBirth(sourceStudent.getDateOfBirth());
		}
		return studentRepository.update(existingStudent);
	}

	public void delete(Integer id ) {
		Student existingStudent = studentRepository.get(id);
		if (existingStudent != null) {
			studentRepository.delete(id);
		}
	}

}
