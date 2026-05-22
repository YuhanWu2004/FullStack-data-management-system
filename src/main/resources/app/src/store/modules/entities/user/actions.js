export default {
    setRole({ commit }, { role, name, userId}) {
        commit('SET_ROLE', { role, name, userId })
    },
    clearRole({ commit }) {
        commit('CLEAR_ROLE')
    }
}