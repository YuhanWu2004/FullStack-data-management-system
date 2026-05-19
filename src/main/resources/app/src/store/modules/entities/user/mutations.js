export default {
    SET_ROLE(state, { role, name }) {
        state.role = role
        state.name = name
    },
    CLEAR_ROLE(state) {
        state.role = null
        state.name = ''
    }
}