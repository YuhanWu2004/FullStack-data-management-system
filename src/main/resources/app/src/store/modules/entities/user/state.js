export default {
    // Everything below is filled in from GET /api/auth/me. Nothing here is chosen by the
    // browser any more — that was the whole problem with the old role picker.
    authenticated: false,
    username: null,
    displayName: '',
    roles: [],
    studentId: null,
    professorId: null,

    // True once the session has been checked at least once, so the router knows the
    // difference between "signed out" and "not asked yet".
    ready: false,
    loading: false,
    error: null
}
