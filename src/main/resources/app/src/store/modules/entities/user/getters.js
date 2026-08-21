export default {
    authenticated: (state) => state.authenticated,
    username: (state) => state.username,
    displayName: (state) => state.displayName,
    studentId: (state) => state.studentId,
    professorId: (state) => state.professorId,
    ready: (state) => state.ready,
    loading: (state) => state.loading,
    error: (state) => state.error,

    /**
     * Roles in the short form the routes use. The server speaks Spring Security's
     * convention ("ROLE_STAFF"); routes and templates read better as "staff", so the
     * translation happens once, here, instead of at every comparison.
     */
    roles: (state) => state.roles.map((role) => role.replace(/^ROLE_/, '').toLowerCase()),

    isStaff: (state, getters) => getters.roles.includes('staff'),
    isProfessor: (state, getters) => getters.roles.includes('professor'),
    isStudent: (state, getters) => getters.roles.includes('student'),

    /**
     * Does the signed-in user hold any of the roles a route asks for? A route with no
     * requirement is open to anyone signed in.
     */
    hasAnyRole: (state, getters) => (required) => {
        if (!required || required.length === 0) return true
        return required.some((role) => getters.roles.includes(role))
    },

    /** For the sidebar badge — one label, even when someone holds several roles. */
    roleLabel: (state, getters) => getters.roles.join(' · ')
}
