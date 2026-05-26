export default {
    SET_PROGRAMS(state, programs) {
        state.programs = programs
    },
    ADD_PROGRAM(state, program) {
        state.programs.push(program)
    },
    UPDATE_PROGRAM(state, updatedProgram) {
        const index = state.programs.findIndex(p => p.id === updatedProgram.id)
        if (index !== -1) {
            state.programs[index] = updatedProgram
        }
    },
    DELETE_PROGRAM(state, id) {
        state.programs = state.programs.filter(p => p.id !== id)
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
    },
    SET_ALL_PROGRAM(state, allPrograms) {
        state.allPrograms = allPrograms
    }
}