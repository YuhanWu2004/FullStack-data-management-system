import { apiFetch, primeCsrfToken } from '../../../../api/http'

export default {
    /**
     * Asks the server who we are. Called once on boot by the router guard, which is also
     * what makes a page refresh keep you signed in — the session lives in a cookie, so
     * there is nothing for the client to persist.
     */
    async fetchSession({ commit }) {
        try {
            const response = await apiFetch('/api/auth/me')
            const data = await response.json()
            if (data.authenticated) {
                commit('SET_SESSION', data)
            } else {
                commit('CLEAR_SESSION')
            }
        } catch (error) {
            // Server unreachable. Treat it as signed out so the app lands on the login
            // screen with a message rather than an empty shell.
            commit('CLEAR_SESSION')
            commit('SET_ERROR', 'Could not reach the server.')
        }
    },

    async login({ commit, dispatch }, { username, password }) {
        commit('SET_LOADING', true)
        commit('SET_ERROR', null)
        try {
            // The login POST is itself a state-changing request, so it needs a CSRF token
            // like any other.
            await primeCsrfToken()

            await apiFetch('/api/auth/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: new URLSearchParams({ username, password })
            })

            // Roles come from the server, never from the login form.
            await dispatch('fetchSession')
            return true
        } catch (error) {
            commit('CLEAR_SESSION')
            commit('SET_ERROR',
                error.status === 401
                    ? 'Incorrect username or password.'
                    : error.message || 'Could not sign in.')
            return false
        } finally {
            commit('SET_LOADING', false)
        }
    },

    async logout({ commit }) {
        try {
            await apiFetch('/api/auth/logout', { method: 'POST' })
        } catch (error) {
            // Even if the call fails, drop the local session — staying "signed in" in the
            // UI while the server disagrees is the worse outcome.
        } finally {
            commit('CLEAR_SESSION')
        }
    },

    /** Used by the 401 handler in http.js when a session expires mid-use. */
    sessionExpired({ commit }) {
        commit('CLEAR_SESSION')
        commit('SET_ERROR', 'Your session has ended. Please sign in again.')
    }
}
