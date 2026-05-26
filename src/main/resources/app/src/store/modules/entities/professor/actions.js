import {commit} from "lodash/seq";

const API_URL = 'http://localhost:8080/api/professor'

export default {

    async fetchAllProfessors({commit}) {
        commit('SET_ERROR', null)
        try {
            const responds = await fetch(`${API_URL}?page=0&size=10000`)
            const data = await responds.json()
            console.log("fetching all professors!", data)
            commit('SET_ALL_PROFESSORS', data.professors)
        } catch (error) {
            commit('SET_ERROR', 'Failed to load all students')
        }
    },
    async fetchProfessors({ commit, state }, { page, size, searchFirstName, searchLastName, searchId }={}) {
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

            } else if ( searchFirstName && searchFirstName.trim()) {
                url = `${API_URL}/search/firstName?firstName=${encodeURIComponent(searchFirstName)}&page=${currentPage}&size=${pageSize}`
            } else if (searchLastName && searchLastName.trim()) {
                url = `${API_URL}/search/lastName?lastName=${encodeURIComponent(searchLastName)}&page=${currentPage}&size=${pageSize}`
            } else {
                console.log('ELSE case')

                console.log('getting page: ', searchId)
                url = `${API_URL}?page=${currentPage}&size=${pageSize}`
                console.log("url", url)

            }
            const response = await fetch(url)
            const data = await response.json()
            console.log("professor data", data.professors)
            commit('SET_PROFESSORS', data.professors)
            commit('SET_PAGINATION', {
                total: data.total,
                totalPages: data.totalPages,
                page: data.page,
                size: data.size
            })
        } catch (error) {
            commit('SET_ERROR', 'Failed to load professors')
        } finally {
            commit('SET_LOADING', false)
        }
    },

     async changePage({ commit, dispatch, state }, page) {
        commit('SET_PAGE', page)
        await dispatch('fetchProfessors', { page })
    },


    async createProfessor({ commit }, professorData) {
        commit('SET_LOADING', true)
        commit('SET_ERROR', null)
        try {
            const payload = {
                firstName: professorData.firstName,
                lastName: professorData.lastName,
                program: {id: professorData.programId}
            }
            console.log("playload", payload)
            const response = await fetch(API_URL, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            })
            const newProfessor = await response.json()
            commit('ADD_PROFESSOR', newProfessor)
        } catch (error) {
            commit('SET_ERROR', 'Failed to create professor')
        } finally {
            commit('SET_LOADING', false)
        }
    },

    async updateProfessor({ commit }, professorData) {
        commit('SET_LOADING', true)
        commit('SET_ERROR', null)
        try {
            const payload = {
                id: professorData.id,
                firstName: professorData.firstName,
                lastName: professorData.lastName,
                program: {id: professorData.programId}
            }
            console.log("update professor", payload)
            const response = await fetch(API_URL, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            })
            const updatedProfessor = await response.json()
            commit('UPDATE_PROFESSOR', updatedProfessor)
        } catch (error) {
            commit('SET_ERROR', 'Failed to update professor')
        } finally {
            commit('SET_LOADING', false)
        }
    },

    async deleteProfessor({ commit }, id) {
        commit('SET_LOADING', true)
        commit('SET_ERROR', null)
        try {
            await fetch(`${API_URL}/${id}`, { method: 'DELETE' })
            commit('DELETE_PROFESSOR', id)
        } catch (error) {
            commit('SET_ERROR', 'Failed to delete professor')
        } finally {
            commit('SET_LOADING', false)
        }
    }
}