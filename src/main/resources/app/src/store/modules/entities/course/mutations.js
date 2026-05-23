export default {
    SET_COURSES(state, courses) {
        state.courses = courses
    },

    ADD_COURSE(state, course) {
        state.courses.push(course)
    },

    UPDATE_COURSE(state, updatedCourse) {
        const index = state.courses.findIndex(c => c.id === updatedCourse.id)
        if (index >= 0) {
            state.courses[index] = updatedCourse
        }
    },

    DELETE_COURSE(state, courseId) {
        state.courses = state.courses.filter(c => c.id !== courseId)
    },

    SET_PAGINATION(state, { total, totalPages, page, size }) {
        state.totalItems = total
        state.totalPages = totalPages
        state.currentPage = page
        state.pageSize = size
    },
    SET_PAGE(state, page) {
        state.currentPage = page
    },

    SET_SELECTED_COURSES(state, courses) {
        state.selectedCourses = courses
    },

    SET_LOADING(state, loading) {
        state.loading = loading
    },

    SET_ERROR(state, error) {
        state.error = error
    }
}