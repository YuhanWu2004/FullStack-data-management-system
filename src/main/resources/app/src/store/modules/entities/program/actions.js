const API_URL = 'http://localhost:8080/api/program'

export default {
    async fetchPrograms({ commit }) {
        commit('SET_LOADING', true)
        commit('SET_ERROR', null)
        try {
            const response = await fetch(API_URL)
            const data = await response.json()
            commit('SET_PROGRAMS', data)
        } catch (error) {
            commit('SET_ERROR', 'Failed to load programs')
        } finally {
            commit('SET_LOADING', false)
        }
    },

    async createProgram({ commit }, programData) {
        commit('SET_LOADING', true)
        commit('SET_ERROR', null)
        try {
            const payload = {
                name: programData.name
            }
            const response = await fetch(API_URL, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            })
            const newProgram = await response.json()
            commit('ADD_PROGRAM', newProgram)
        } catch (error) {
            commit('SET_ERROR', 'Failed to create program')
        } finally {
            commit('SET_LOADING', false)
        }
    },

    async updateProgram({ commit }, programData) {
        commit('SET_LOADING', true)
        commit('SET_ERROR', null)
        try {
            const response = await fetch(API_URL, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(programData)
            })
            const updatedProgram = await response.json()
            commit('UPDATE_PROGRAM', updatedProgram)
        } catch (error) {
            commit('SET_ERROR', 'Failed to update program')
        } finally {
            commit('SET_LOADING', false)
        }
    },

    async deleteProgram({ commit }, id) {
        commit('SET_LOADING', true)
        commit('SET_ERROR', null)
        try {
            await fetch(`${API_URL}/${id}`, { method: 'DELETE' })
            commit('DELETE_PROGRAM', id)
        } catch (error) {
            commit('SET_ERROR', 'Failed to delete program')
        } finally {
            commit('SET_LOADING', false)
        }
    }
}