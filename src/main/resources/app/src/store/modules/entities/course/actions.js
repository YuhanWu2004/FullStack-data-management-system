const API_URL = '/api/course'

export default {

    async fetchAllCourses({commit}) {
        commit('SET_ERROR', null)
        try {
            const response = await fetch(`${API_URL}?page=0&size=10000`)  // large size
            const data = await response.json()
            console.log(data)

            commit('SET_ALL_COURSES', data.courses)
        } catch (error) {
            commit('SET_ERROR', 'Failed to load all courses')
        }
    },

    async fetchCourses({ commit, state }, { page, size, searchName, searchId } = {}) {
        commit('SET_LOADING', true)
        commit('SET_ERROR', null)
        try {
            const currentPage = page ?? state.currentPage
            const pageSize = size ?? state.pageSize
            let url
            if (searchId !== undefined && searchId !== null && searchId !== '') {
                url = `${API_URL}/search/id?value=${searchId}`
            } else if (searchName && searchName.trim()) {
                url = `${API_URL}/search/name?value=${encodeURIComponent(searchName)}&page=${currentPage}&size=${pageSize}`
            } else {
                url = `${API_URL}?page=${currentPage}&size=${pageSize}`
            }
            console.log('fetching:', url)
            const response = await fetch(url)
            const data = await response.json()
            console.log('response:', data)

            commit('SET_COURSES', data.courses)
            commit('SET_PAGINATION', {
                total: data.total,
                totalPages: data.totalPages,
                page: data.page,
                size: data.size
            })
        } catch (error) {
            console.log('error:', error)
            commit('SET_ERROR', 'Failed to load courses')
        } finally {
            commit('SET_LOADING', false)
        }
    },

    // change page
    async changePage({ commit, dispatch, state }, page) {
        commit('SET_PAGE', page)
        await dispatch('fetchCourses', { page })
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
            const updatedCourse = await response.json()
            commit('UPDATE_COURSE', updatedCourse)
        } catch(error) {
            console.log(error)
            commit('SET_ERROR', 'Failed to update course')
        } finally {
            commit('SET_LOADING', false)
        }
    },

    async deleteCourse({ commit }, courseId) {
        commit('SET_LOADING', true)
        commit('SET_ERROR', null)
        try{
            await fetch(`${API_URL}/${courseId}`, {
                method: 'DELETE'
            })
            commit('DELETE_COURSE', courseId)
        } catch (error) {
            commit('SET_ERROR', 'Failed to delete course')
        } finally {
            commit('SET_LOADING', false)
        }
    }
}