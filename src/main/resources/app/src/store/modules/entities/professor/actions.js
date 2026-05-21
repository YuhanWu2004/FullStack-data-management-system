const API_URL = 'http://localhost:8080/api/professor'

export default {
    async fetchProfessors({ commit }) {
        commit('SET_LOADING', true)
        commit('SET_ERROR', null)
        try {
            const response = await fetch(API_URL)
            const data = await response.json()
            console.log("professor data", data)
            commit('SET_PROFESSORS', data)
        } catch (error) {
            commit('SET_ERROR', 'Failed to load professors')
        } finally {
            commit('SET_LOADING', false)
        }
    },

    async createProfessor({ commit }, professorData) {
        commit('SET_LOADING', true)
        commit('SET_ERROR', null)
        try {
            const payload = {
                firstName: professorData.firstName,
                lastName: professorData.lastName,
                department: professorData.department || null
            }
            const response = await fetch(API_URL, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            })
            const newProfessor = await response.json()
            commit('ADD_PROFESSOR', newProfessor)
        } catch (error) {
            commit('SET_ERROR', 'Failed to create professor')
        } finally {
            commit('SET_LOADING', false)
        }
    },

    async updateProfessor({ commit }, professorData) {
        commit('SET_LOADING', true)
        commit('SET_ERROR', null)
        try {
            const response = await fetch(API_URL, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(professorData)
            })
            const updatedProfessor = await response.json()
            commit('UPDATE_PROFESSOR', updatedProfessor)
        } catch (error) {
            commit('SET_ERROR', 'Failed to update professor')
        } finally {
            commit('SET_LOADING', false)
        }
    },

    async deleteProfessor({ commit }, id) {
        commit('SET_LOADING', true)
        commit('SET_ERROR', null)
        try {
            await fetch(`${API_URL}/${id}`, { method: 'DELETE' })
            commit('DELETE_PROFESSOR', id)
        } catch (error) {
            commit('SET_ERROR', 'Failed to delete professor')
        } finally {
            commit('SET_LOADING', false)
        }
    }
}