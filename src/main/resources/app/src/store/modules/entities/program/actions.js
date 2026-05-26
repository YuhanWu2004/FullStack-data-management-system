const API_URL = 'http://localhost:8080/api/program'

export default {

    async fetchAllPrograms({ commit }) {
        commit('SET_ERROR', null)
        try {
            const response = await fetch(`${API_URL}?page=0&size=10000`)  // large size
            const data = await response.json()

            commit('SET_ALL_PROGRAM', data.programs)
        } catch (error) {
            commit('SET_ERROR', 'Failed to load all programs')
        }
    },
    async fetchPrograms({ commit, state }, { page, size, searchName, searchId } = {}) {
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
            const response = await fetch(url)
            const data = await response.json()
            // console.log('response:', data)


            commit('SET_PROGRAMS', data.programs)
            commit('SET_PAGINATION', {
                total: data.total,
                totalPages: data.totalPages,
                page: data.page,
                size: data.size
            })

        } catch (error) {
            commit('SET_ERROR', 'Failed to load programs')
        } finally {
            commit('SET_LOADING', false)
        }
    },

    async changePage({ commit, dispatch, state }, page) {
        commit('SET_PAGE', page)
        await dispatch('fetchPrograms', { page })
    },



    async createProgram({ commit }, programData) {
        commit('SET_LOADING', true)
        commit('SET_ERROR', null)
        try {
            const payload = {
                name: programData.name
            }
            const response = await fetch(API_URL, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            })
            const newProgram = await response.json()
            commit('ADD_PROGRAM', newProgram)
        } catch (error) {
            commit('SET_ERROR', 'Failed to create program')
        } finally {
            commit('SET_LOADING', false)
        }
    },

    async updateProgram({ commit }, programData) {
        commit('SET_LOADING', true)
        commit('SET_ERROR', null)
        try {
            const response = await fetch(API_URL, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(programData)
            })
            const updatedProgram = await response.json()
            commit('UPDATE_PROGRAM', updatedProgram)
        } catch (error) {
            commit('SET_ERROR', 'Failed to update program')
        } finally {
            commit('SET_LOADING', false)
        }
    },

    async deleteProgram({ commit }, id) {
        commit('SET_LOADING', true)
        commit('SET_ERROR', null)
        try {
            await fetch(`${API_URL}/${id}`, { method: 'DELETE' })
            commit('DELETE_PROGRAM', id)
        } catch (error) {
            commit('SET_ERROR', 'Failed to delete program')
        } finally {
            commit('SET_LOADING', false)
        }
    }
}