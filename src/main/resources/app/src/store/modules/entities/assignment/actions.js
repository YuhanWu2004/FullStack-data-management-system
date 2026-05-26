const API_URL = 'http://localhost:8080/api/assignment'

export default {
    async fetchAssignments({ commit, state }, { page, size, searchProfessorId, searchCourseId, searchId }={}) {
        commit('SET_LOADING', true)
        commit('SET_ERROR', null)
        try {
            const currentPage = page ?? state.currentPage
            const pageSize = size ?? state.pageSize
            let url
            if (searchId !== undefined && searchId !== null && searchId !== '') {
                console.log('searchId: ', searchId)
                url = `${API_URL}/${searchId}`
                console.log("url", url)

            } else if (searchProfessorId && String(searchProfessorId).trim() !== ''
                && searchCourseId && String(searchCourseId).trim() !== '') {
                console.log('StudentId  and courseId');

                url = `${API_URL}/search/ProfessorIdAndCourseId?professorId=${encodeURIComponent(searchProfessorId)}&courseId=${encodeURIComponent(searchCourseId)}&page=${currentPage}&size=${pageSize}`
                console.log("url", url)
            }else if ( searchCourseId && searchCourseId.trim()) {
                url = `${API_URL}/search/courseId?value=${encodeURIComponent(searchCourseId)}&page=${currentPage}&size=${pageSize}`
            } else if (searchProfessorId && searchProfessorId.trim()) {
                url = `${API_URL}/search/professorId?value=${encodeURIComponent(searchProfessorId)}&page=${currentPage}&size=${pageSize}`
            } else {
                console.log('ELSE case')
                console.log('getting page: ', searchId)
                url = `${API_URL}?page=${currentPage}&size=${pageSize}`
                console.log("url", url)
            }

            const response = await fetch(url)
            const data = await response.json()
            console.log('Assignment response data:', data.assignments)

            commit('SET_ASSIGNMENTS', data.assignments)
            commit('SET_PAGINATION', {
                total: data.total,
                totalPages: data.totalPages,
                page: data.page,
                size: data.size
            })
        } catch (error) {
            commit('SET_ERROR', 'Failed to load assignments')
        } finally {
            commit('SET_LOADING', false)
        }
    },

    async changePage({ commit, dispatch, state }, page) {
        commit('SET_PAGE', page)
        await dispatch('fetchAssignments', { page })   
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