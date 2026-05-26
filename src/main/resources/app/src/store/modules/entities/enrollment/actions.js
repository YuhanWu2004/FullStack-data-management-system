const API_URL = 'http://localhost:8080/api/enrollment'

export default {
    async fetchEnrollments({ commit, state }, { page, size, searchStudentId, searchCourseId, searchId}={}) {
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

            } else if (searchStudentId && String(searchStudentId).trim() !== ''
                && searchCourseId && String(searchCourseId).trim() !== '') {
                console.log('StudentId  and courseId');

                url = `${API_URL}/search/StudentIdAndCourseId?studentId=${encodeURIComponent(searchStudentId)}&courseId=${encodeURIComponent(searchCourseId)}&page=${currentPage}&size=${pageSize}`
                console.log("url", url)
            }
            else if ( searchStudentId && String(searchStudentId).trim() !== '') {
                console.log('StudentId case')

                url = `${API_URL}/search/studentId?value=${encodeURIComponent(searchStudentId)}&page=${currentPage}&size=${pageSize}`
                console.log("url", url)

            } else if (searchCourseId && String(searchCourseId).trim() !== '') {
                url = `${API_URL}/search/courseName?value=${encodeURIComponent(searchCourseId)}&page=${currentPage}&size=${pageSize}`
            } else {
                console.log('ELSE case')

                console.log('getting page for enrollment: ', searchId)
                url = `${API_URL}?page=${currentPage}&size=${pageSize}`
                console.log("url", url)

            }

            const response = await fetch(url)
            const data = await response.json()
            console.log('enrollments data:', data.enrollments)
            console.log('first enrollment:', data[0])           // ← add this
            console.log('course data:', data[0]?.course)

            commit('SET_ENROLLMENTS', data.enrollments)
            commit('SET_PAGINATION', {
                total: data.total,
                totalPages: data.totalPages,
                page: data.page,
                size: data.size
            })
        } catch (error) {
            console.log(error)
            commit('SET_ERROR', 'Failed to load enrollments')
        } finally {
            commit('SET_LOADING', false)
        }
    },

    async changePage({ commit, dispatch, state }, page) {
        commit('SET_PAGE', page)
        await dispatch('fetchEnrollments', { page })
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