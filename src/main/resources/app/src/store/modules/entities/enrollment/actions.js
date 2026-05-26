const API_URL = 'http://localhost:8080/api/enrollment'

export default {
    async fetchEnrollments({ commit, state }, { page, size, searchStudentId, searchCourseId, searchId, searchStudentName, searchCourseName } = {}) {
        commit('SET_LOADING', true)
        commit('SET_ERROR', null)
        try {
            const currentPage = page ?? state.currentPage
            const pageSize = size ?? state.pageSize
            let url

            // 1. Search by exact Enrollment ID
            if (searchId !== undefined && searchId !== null && searchId !== '') {
                url = `${API_URL}/${searchId}`
            } 
            // 2. Search by BOTH Student ID and Course ID
            else if (searchStudentId && String(searchStudentId).trim() !== '' && 
                     searchCourseId && String(searchCourseId).trim() !== '') {
                url = `${API_URL}/search/StudentIdAndCourseId?studentId=${encodeURIComponent(searchStudentId)}&courseId=${encodeURIComponent(searchCourseId)}&page=${currentPage}&size=${pageSize}`
            }
            // 3. Search by Student ID only
            else if (searchStudentId && String(searchStudentId).trim() !== '') {
                url = `${API_URL}/search/studentId?value=${encodeURIComponent(searchStudentId)}&page=${currentPage}&size=${pageSize}`
            } 
            // 4. Search by Course ID only
            else if (searchCourseId && String(searchCourseId).trim() !== '') {
                url = `${API_URL}/search/courseId?value=${encodeURIComponent(searchCourseId)}&page=${currentPage}&size=${pageSize}` 
            } 
            // 5. NEW: Search by Student Name (First, Last, or Full)
            else if (searchStudentName && String(searchStudentName).trim() !== '') {
                url = `${API_URL}/search/studentName?value=${encodeURIComponent(searchStudentName)}&page=${currentPage}&size=${pageSize}`
            }
            // 6. NEW: Search by Course Name
            else if (searchCourseName && String(searchCourseName).trim() !== '') {
                console.log("hiii")
                url = `${API_URL}/search/courseName?value=${encodeURIComponent(searchCourseName)}&page=${currentPage}&size=${pageSize}`
            } 
            // 7. Default: Get all paginated
            else {
                url = `${API_URL}?page=${currentPage}&size=${pageSize}`
            }

            console.log("Fetching from URL:", url)

            const response = await fetch(url)
            const data = await response.json()
            
            // Fixed the logging to target the enrollments array properly
            console.log('enrollments data:', data.enrollments)
            if (data.enrollments && data.enrollments.length > 0) {
                console.log('first enrollment:', data.enrollments[0])           
                console.log('course data:', data.enrollments[0]?.course)
            }

            commit('SET_ENROLLMENTS', data.enrollments)
            commit('SET_PAGINATION', {
                total: data.total,
                totalPages: data.totalPages,
                page: data.page,
                size: data.size
            })
        } catch (error) {
            console.error("Fetch Error: ", error)
            commit('SET_ERROR', 'Failed to load enrollments')
        } finally {
            commit('SET_LOADING', false)
        }
    },

    async changePage({ commit, dispatch, state }, payload) {
        // Updated to pass the entire payload down so search queries aren't lost on page turns
        commit('SET_PAGE', payload.page)
        await dispatch('fetchEnrollments', payload)
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