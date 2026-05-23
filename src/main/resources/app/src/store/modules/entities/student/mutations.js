export default {
    SET_STUDENTS(state, students) {
        state.students = students
    },

    ADD_STUDENT(state, student) {
        state.students.push(student)
    },

    UPDATE_STUDENT(state, updatedStudent) {
        const index = state.students.findIndex(s => s.id === updatedStudent.id)
        if (index !== -1) {
            state.students[index] = updatedStudent
        }
    },

    DELETE_STUDENT(state, id) {
        state.students = state.students.filter(s => s.id !== id)
    },

    SET_SELECTED_STUDENT(state, student) {
        state.selectedStudent = student
    },

    SET_LOADING(state, loading) {
        state.loading = loading
    },

    SET_ERROR(state, error) {
        state.error = error
    },
    SET_PAGINATION(state, { total, totalPages, page, size }) {
        state.totalItems = total
        state.totalPages = totalPages
        state.currentPage = page
        state.pageSize = size
    },
    SET_PAGE(state, page) {
        state.currentPage = page
    }
}