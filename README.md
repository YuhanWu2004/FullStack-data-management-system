# FullStack-data-management-system

A student-course-teacher management full-stack application designed using Spring boot and Vue.js.

The system provides Role-based access, CRUD operations, relational data handling, and a responsive UI for managing academic data.

Role - Staff
Access to all general information, can complete enrollments and assignments.

- StudentView Page:
  - Present all existing students in table form
  - CRUD operations for the student entity
 
- CourseView Page:
  - Present all existing courses in table form
  - CRUD operations for the course entity
 
- Enrollment Page
  - Present all existing enrollments in table form
  - Enroll and unenroll a student to a course
 
- Assignment Page:
  - Present all existing teacher-course assignments in table form
  - Assign and unassign a teacher to a course

Role - Student

- Student Profile Page:
  - Information related to the signed in Student: Program, GPA, Course Enrollments
 
- CourseView Page: Partial feature
  - Able to see all the available course offerings
  - Unable to make enrollments

Role - Student

- Professor Profile Page:
  - Information related to the signed in Professor: Basic Information, Course Assignments

- CourseView Page: Partial feature
  - Able to see all the available course offerings
  - Unable to create assignments

Features under implmentation:
- Role-Based Access, User-Sign-In Feature
- Students submit course selection, staff/admin review and approve to carry out the enrollments
- Professors submit course selection, staff/admin review and approve to carry out the assignment
