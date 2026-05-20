const API_URL = '/api/course'

export default {
    async fetchCourses({ commit, state, dispatch }) {
        commit('SET_LOADING', true)
        commit('SET_ERROR', null)
        try {
            const response = await fetch(API_URL)
            const data = await response.json()
            commit('SET_STUDENTS', data)
        } catch (error) {
            commit('SET_ERROR', 'Failed to load courses')
        } finally {
            commit('SET_LOADING', false)
        }
    },

    async createCourse({ commit }, courseData) {
        commit('SET_LOADING', true)
        commit('SET_ERROR', null)
        try {
            const response = await fetch(API_URL, {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(courseData)
            })
            const newCourse = await response.json()
            commit('ADD_COURSE', newCourse)
        } catch (error) {
            commit('SET_ERROR', 'Failed to create course')
        } finally {
            commit('SET_LOADING', false)
        }
    },

    async updateCourse({ commit }, courseData) {
        commit('SET_LOADING', true)
        commit('SET_ERROR', null)
        try {
            const response = await fetch(API_URL, {
                method: 'PUT',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(courseData)
            })
            const updatedStudent = await response.json()
            commit('SET_STUDENTS', updatedStudent)
        }catch(error) {
            commit('SET_ERROR', 'Failed to update course')
        }
    },

    async deleteCourse({ commit }, courseId) {
        commit('SET_LOADING', true)
        commit('SET_ERROR', null)
        try{
            await fetch(`${API_URL}/${courseId}`, {
                method: 'DELETE'
            })
            commit('DELETE_STUDENT', courseId)
        } catch (error) {
            commit('SET_ERROR', 'Failed to delete course')
        } finally {
            commit('SET_LOADING', false)
        }
    }
}