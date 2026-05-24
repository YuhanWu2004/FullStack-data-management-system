export default {
    SET_PROFESSORS(state, professors) {
        state.professors = professors
    },
    ADD_PROFESSOR(state, professor) {
        state.professors.push(professor)
    },
    UPDATE_PROFESSOR(state, updatedProfessor) {
        const index = state.professors.findIndex(p => p.id === updatedProfessor.id)
        if (index !== -1) {
            state.professors[index] = updatedProfessor
        }
    },
    DELETE_PROFESSOR(state, id) {
        state.professors = state.professors.filter(a => a.id !== id)
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