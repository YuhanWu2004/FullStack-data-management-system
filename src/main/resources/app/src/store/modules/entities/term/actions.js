import { apiFetch } from '../../../../api/http'
const API_URL = '/api/term'

export default {

    async fetchTerms({ commit }) {
        commit('SET_LOADING', true)
        commit('SET_ERROR', null)
        try {
            const response = await apiFetch(API_URL)
            const terms = await response.json()
            commit('SET_TERMS', terms)
        } catch (error) {
            commit('SET_ERROR', error.message || 'Failed to load terms')
        } finally {
            commit('SET_LOADING', false)
        }
    },

    async fetchCurrentTerm({ commit }) {
        commit('SET_ERROR', null)
        try {
            const response = await apiFetch(`${API_URL}/current`)
            const text = await response.text()
            // No current term is set yet (a brand-new database, before staff picks one) —
            // the endpoint responds 200 with an empty body rather than an object.
            commit('SET_CURRENT', text ? JSON.parse(text) : null)
        } catch (error) {
            commit('SET_ERROR', error.message || 'Failed to load the current term')
        }
    },

    async createTerm({ commit }, termData) {
        commit('SET_LOADING', true)
        commit('SET_ERROR', null)
        try {
            const payload = {
                name: termData.name,
                startDate: termData.startDate || null,
                endDate: termData.endDate || null
            }
            const response = await apiFetch(API_URL, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            })
            const newTerm = await response.json()
            commit('ADD_TERM', newTerm)
        } catch (error) {
            commit('SET_ERROR', error.message || 'Failed to create term')
        } finally {
            commit('SET_LOADING', false)
        }
    },

    async updateTerm({ commit }, termData) {
        commit('SET_LOADING', true)
        commit('SET_ERROR', null)
        try {
            const response = await apiFetch(API_URL, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(termData)
            })
            const updatedTerm = await response.json()
            commit('UPDATE_TERM', updatedTerm)
        } catch (error) {
            commit('SET_ERROR', error.message || 'Failed to update term')
        } finally {
            commit('SET_LOADING', false)
        }
    },

    async deleteTerm({ commit }, id) {
        commit('SET_LOADING', true)
        commit('SET_ERROR', null)
        try {
            await apiFetch(`${API_URL}/${id}`, { method: 'DELETE' })
            commit('DELETE_TERM', id)
        } catch (error) {
            commit('SET_ERROR', error.message || 'Failed to delete term')
        } finally {
            commit('SET_LOADING', false)
        }
    },

    async setCurrentTerm({ commit, dispatch }, id) {
        commit('SET_LOADING', true)
        commit('SET_ERROR', null)
        try {
            const response = await apiFetch(`${API_URL}/${id}/current`, { method: 'PUT' })
            const updatedTerm = await response.json()
            commit('SET_CURRENT', updatedTerm)
            // Setting one term current un-sets whichever term held it before; refetching
            // the list is simpler and less error-prone than hand-patching every other
            // row's flag locally.
            await dispatch('fetchTerms')
        } catch (error) {
            commit('SET_ERROR', error.message || 'Failed to set the current term')
        } finally {
            commit('SET_LOADING', false)
        }
    }
}
