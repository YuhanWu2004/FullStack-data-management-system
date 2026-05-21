export default {
    SET_ENROLLMENTS(state, enrollments) {
        state.enrollments = enrollments
    },
    ADD_ENROLLMENT(state, enrollment) {
        state.enrollments.push(enrollment)
    },
    DELETE_ENROLLMENT(state, id) {
        state.enrollments = state.enrollments.filter(e => e.id !== id)
    },
    SET_LOADING(state, loading) {
        state.loading = loading
    },
    SET_ERROR(state, error) {
        state.error = error
    }
}