const API_URL = 'http://localhost:8080/api/student'

export default {

    async fetchAllStudents({commit}) {
        commit('SET_ERROR', null)
        try {
            const response = await fetch(`${API_URL}?page=0&size=10000`)
            const data = await response.json()

            commit('SET_ALL_STUDENTS', data.students)
        } catch (error) {
            commit('SET_ERROR', 'Failed to load all students')
        }
    },

    async fetchStudents({ commit, state }, { page, size, searchName, searchId, searchMinGpa }={}) {
        commit('SET_LOADING', true)
        commit('SET_ERROR', null)
        try{
            const currentPage = page ?? state.currentPage
            const pageSize = size ?? state.pageSize
            let url
            // 1. Search by exact ID
            if (searchId !== undefined && searchId !== null && searchId !== '') {
                url = `${API_URL}/${searchId}`
            } 
            // 2. NEW: Unified Search by Name (First, Last, or Full)
            else if (searchName && String(searchName).trim() !== '') {
                url = `${API_URL}/search/name?value=${encodeURIComponent(searchName)}&page=${currentPage}&size=${pageSize}`
            } 
            // 3. Search by Minimum GPA
            else if (searchMinGpa !== undefined && searchMinGpa !== null && searchMinGpa >= 0 && searchMinGpa <= 4) {
                url = `${API_URL}/search/gpaGreater?value=${searchMinGpa}&page=${currentPage}&size=${pageSize}`
            } 
            // 4. Default: Get all paginated
            else {
                url = `${API_URL}?page=${currentPage}&size=${pageSize}`
            }
            const response = await fetch(url)
            const data = await response.json()

            commit('SET_STUDENTS', data.students)
            commit('SET_PAGINATION', {
                total: data.total,
                totalPages: data.totalPages,
                page: data.page,
                size: data.size
            })
        } catch (error) {
            commit('SET_ERROR', 'Failed to load students')
        } finally {
            commit('SET_LOADING', false)
        }
    },

    async changePage({ commit, dispatch, state }, page) {
        commit('SET_PAGE', page)
        await dispatch('fetchStudents', { page })
    },


    async createStudent({ commit }, studentData) {
        commit('SET_LOADING', true)
        commit('SET_ERROR', null)
        try {
            // sanitize data before sending
            const payload = {
                firstName: studentData.firstName,
                lastName: studentData.lastName,
                gpa: studentData.gpa || null,
                dateOfBirth: studentData.dateOfBirth || null,  // ← empty string becomes null
                program: studentData.program || null
            }
            const response = await fetch(API_URL, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
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