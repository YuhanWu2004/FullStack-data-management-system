import {commit} from "lodash/seq";

export default {
    SET_ROLE(state, { role, userId, name }) {
        state.role = role
        state.name = name
        state.userId = userId
        // commit('SET_ROLE', { role, name, userId })
    },
    CLEAR_ROLE(state) {
        state.role = null
        state.name = ''
    }
}