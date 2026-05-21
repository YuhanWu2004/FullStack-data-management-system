const API_URL = 'http://localhost:8080/api/assignment'

export default {
    async fetchAssignments({ commit }) {
        commit('SET_LOADING', true)
        commit('SET_ERROR', null)
        try {
            const response = await fetch(API_URL)
            const data = await response.json()
            commit('SET_ASSIGNMENTS', data)
        } catch (error) {
            commit('SET_ERROR', 'Failed to load assignments')
        } finally {
            commit('SET_LOADING', false)
        }
    },

    async createAssignment({ commit }, assignmentData) {
        commit('SET_LOADING', true)
        commit('SET_ERROR', null)
        try {
            const payload = {
                professor: { id: assignmentData.professorId },
                course: { id: assignmentData.courseId }
            }
            const response = await fetch(API_URL, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            })
            const newAssignment = await response.json()
            commit('ADD_ASSIGNMENT', newAssignment)
        } catch (error) {
            commit('SET_ERROR', 'Failed to create assignment')
        } finally {
            commit('SET_LOADING', false)
        }
    },

    async deleteAssignment({ commit }, id) {
        commit('SET_LOADING', true)
        commit('SET_ERROR', null)
        try {
            await fetch(`${API_URL}/${id}`, { method: 'DELETE' })
            commit('DELETE_ASSIGNMENT', id)
        } catch (error) {
            commit('SET_ERROR', 'Failed to delete assignment')
        } finally {
            commit('SET_LOADING', false)
        }
    }
}