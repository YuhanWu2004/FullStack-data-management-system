export default {
    SET_SESSION(state, session) {
        state.authenticated = true
        state.username = session.username
        state.displayName = session.displayName || session.username
        state.roles = session.roles || []
        state.studentId = session.studentId ?? null
        state.professorId = session.professorId ?? null
        state.ready = true
        state.error = null
    },

    CLEAR_SESSION(state) {
        state.authenticated = false
        state.username = null
        state.displayName = ''
        state.roles = []
        // Clearing these matters: a stale id left behind here is an id the next signed-in
        // user's profile page would fetch.
        state.studentId = null
        state.professorId = null
        state.ready = true
    },

    SET_LOADING(state, loading) {
        state.loading = loading
    },

    SET_ERROR(state, error) {
        state.error = error
    }
}
