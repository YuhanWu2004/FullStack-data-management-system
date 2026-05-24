export default {
    SET_ASSIGNMENTS(state, assignments) {
        state.assignments = assignments
    },
    ADD_ASSIGNMENT(state, assignment) {
        state.assignments.push(assignment)
    },
    DELETE_ASSIGNMENT(state, id) {
        state.assignments = state.assignments.filter(a => a.id !== id)
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