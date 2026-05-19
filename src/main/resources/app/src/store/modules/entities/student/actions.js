const API_URL = 'http://localhost:8080/api/student'

export default {

    async fetchStudents({ commit }) {
        commit('SET_LOADING', true)
        commit('SET_ERROR', null)
        try {
            const response = await fetch(API_URL)
            const data = await response.json()
            commit('SET_STUDENTS', data)
        } catch (error) {
            commit('SET_ERROR', 'Failed to load students')
        } finally {
            commit('SET_LOADING', false)
        }
    },

    async createStudent({ commit }, studentData) {
        commit('SET_LOADING', true)
        commit('SET_ERROR', null)
        try {
            const response = await fetch(API_URL, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(studentData)
            })
            const newStudent = await response.json()
            commit('ADD_STUDENT', newStudent)
        } catch (error) {
            commit('SET_ERROR', 'Failed to create student')
        } finally {
            commit('SET_LOADING', false)
        }
    },

    async updateStudent({ commit }, studentData) {
        console.log('studentData received:', studentData)
        commit('SET_LOADING', true)
        commit('SET_ERROR', null)
        try {
            const response = await fetch(API_URL, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(studentData)
            })
            const updatedStudent = await response.json()
            commit('UPDATE_STUDENT', updatedStudent)
        } catch (error) {
            commit('SET_ERROR', 'Failed to update student')
        } finally {
            commit('SET_LOADING', false)
        }
    },

    async deleteStudent({ commit }, id) {
        commit('SET_LOADING', true)
        commit('SET_ERROR', null)
        try {
            await fetch(`${API_URL}/${id}`, {
                method: 'DELETE'
            })
            commit('DELETE_STUDENT', id)
        } catch (error) {
            commit('SET_ERROR', 'Failed to delete student')
        } finally {
            commit('SET_LOADING', false)
        }
    },

    selectStudent({ commit }, student) {
        commit('SET_SELECTED_STUDENT', student)
    },

    clearSelectedStudent({ commit }) {
        commit('SET_SELECTED_STUDENT', null)
    }
}