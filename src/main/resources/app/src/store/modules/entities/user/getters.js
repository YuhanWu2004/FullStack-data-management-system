export default {
    role: (state) => state.role,
    name: (state) => state.name,
    userId: (state) => state.userId,

    allowedRoutes: (state) => {
        switch (state.role) {
            case 'student':
                return ['courses', 'program', 'student-profile']
            case 'professor':
                return ['courses', 'program', 'professor-profile']
            case 'staff':
                return ['students', 'courses', 'professors', 'programs', 'enrollments', 'assignments']
            default:
                return []
        }
    },

    canAccess: (state) => (routeName) => {
        switch (state.role) {
            case 'student':
                return ['courses', 'programs', 'studentProfile'].includes(routeName)
            case 'professor':
                return ['courses',  'programs', 'professorProfile'].includes(routeName)
            case 'staff':
                return ['students', 'courses', 'professors', 'programs', 'enrollments', 'assignments'].includes(routeName)
            default:
                return false
        }
    }
}