const API_URL = 'http://localhost:8080/api/enrollment'

export default {
    async fetchEnrollments({ commit }) {
        commit('SET_LOADING', true)
        commit('SET_ERROR', null)
        try {
            const response = await fetch(API_URL)
            const data = await response.json()
            console.log('enrollments data:', data)
            console.log('first enrollment:', data[0])           // ← add this
            console.log('course data:', data[0]?.course)        // ← add this
            commit('SET_ENROLLMENTS', data)
        } catch (error) {
            console.log(error)
            commit('SET_ERROR', 'Failed to load enrollments')
        } finally {
            commit('SET_LOADING', false)
        }
    },

    async createEnrollment({ commit }, enrollmentData) {
        commit('SET_LOADING', true)
        commit('SET_ERROR', null)
        try {
            const payload = {
                student: { id: enrollmentData.studentId },
                course: { id: enrollmentData.courseId },
                grade: enrollmentData.grade || null
            }
            console.log('sending payload:', payload)
            const response = await fetch(API_URL, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            })
            const newEnrollment = await response.json()
            commit('ADD_ENROLLMENT', newEnrollment)
        } catch (error) {
            commit('SET_ERROR', 'Failed to create enrollment')
        } finally {
            commit('SET_LOADING', false)
        }
    },

    async deleteEnrollment({ commit }, id) {
        commit('SET_LOADING', true)
        commit('SET_ERROR', null)
        try {
            await fetch(`${API_URL}/${id}`, { method: 'DELETE' })
            commit('DELETE_ENROLLMENT', id)
        } catch (error) {
            commit('SET_ERROR', 'Failed to delete enrollment')
        } finally {
            commit('SET_LOADING', false)
        }
    }
}