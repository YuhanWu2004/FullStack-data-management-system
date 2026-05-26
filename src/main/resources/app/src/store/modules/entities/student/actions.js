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

    async fetchStudents({ commit, state }, { page, size, searchFirstName, searchLastName, searchId, searchMinGpa }={}) {
        console.log(searchFirstName)
        commit('SET_LOADING', true)
        commit('SET_ERROR', null)
        try{
            const currentPage = page ?? state.currentPage
            const pageSize = size ?? state.pageSize
            let url
            if (searchId !== undefined && searchId !== null && searchId !== '') {
                console.log('searchId: ', searchId)
                url = `${API_URL}/${searchId}`
                console.log("url", url)

            } else if ( searchFirstName && searchFirstName.trim()) {
                url = `${API_URL}/search/firstName?value=${encodeURIComponent(searchFirstName)}&page=${currentPage}&size=${pageSize}`
            } else if (searchLastName && searchLastName.trim()) {
                url = `${API_URL}/search/lastName?value=${encodeURIComponent(searchLastName)}&page=${currentPage}&size=${pageSize}`
            } else if (searchMinGpa && searchMinGpa >= 0 && searchMinGpa <= 4) {
                console.log('getting gpa: ', searchMinGpa)


                url = `${API_URL}/search/gpaGreater?value=${searchMinGpa}&page=${currentPage}&size=${pageSize}`
                console.log("url", url)

            } else {
                console.log('ELSE case')

                console.log('getting page: ', searchId)
                url = `${API_URL}?page=${currentPage}&size=${pageSize}`
                console.log("url", url)

            }


            const response = await fetch(url)
            const data = await response.json()
            console.log('students response:', data)


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