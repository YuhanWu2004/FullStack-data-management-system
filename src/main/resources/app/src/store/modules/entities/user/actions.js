export default {
    setRole({ commit }, { role, name }) {
        commit('SET_ROLE', { role, name })
    },
    clearRole({ commit }) {
        commit('CLEAR_ROLE')
    }
}