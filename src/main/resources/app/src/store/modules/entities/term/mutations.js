export default {
    SET_TERMS(state, terms) {
        state.terms = terms
    },
    SET_CURRENT(state, term) {
        state.current = term
    },
    ADD_TERM(state, term) {
        state.terms.push(term)
    },
    UPDATE_TERM(state, updatedTerm) {
        const index = state.terms.findIndex(t => t.id === updatedTerm.id)
        if (index !== -1) {
            state.terms[index] = updatedTerm
        }
    },
    DELETE_TERM(state, id) {
        state.terms = state.terms.filter(t => t.id !== id)
    },
    SET_LOADING(state, loading) {
        state.loading = loading
    },
    SET_ERROR(state, error) {
        state.error = error
    }
}
