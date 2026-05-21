export default {
    SET_PROFESSORS(state, professors) {
        state.professors = professors
    },
    ADD_PROFESSOR(state, professor) {
        state.professors.push(professor)
    },
    DELETE_PROFESSOR(state, id) {
        state.professors = state.professors.filter(a => a.id !== id)
    },
    SET_LOADING(state, loading) {
        state.loading = loading
    },
    SET_ERROR(state, error) {
        state.error = error
    }
}