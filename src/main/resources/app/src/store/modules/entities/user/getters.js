export default {
    role: (state) => state.role,
    name: (state) => state.name,

    allowedRoutes: (state) => {
        switch (state.role) {
            case 'student':
                return ['courses']
            case 'professor':
                return ['courses', 'students']
            case 'staff':
                return ['students', 'courses', 'professors', 'programs']
            default:
                return []
        }
    },

    canAccess: (state) => (routeName) => {
        switch (state.role) {
            case 'student':
                return ['courses'].includes(routeName)
            case 'professor':
                return ['courses', 'students'].includes(routeName)
            case 'staff':
                return ['students', 'courses', 'professors', 'programs'].includes(routeName)
            default:
                return false
        }
    }
}