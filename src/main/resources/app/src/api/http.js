/**
 * The single door every API call goes through.
 *
 * Three jobs, all of which used to be missing:
 *  1. Attach the CSRF token. Session cookies are sent by the browser automatically, which
 *     is exactly why a write needs a token the browser will not send on its own.
 *  2. Treat a non-2xx response as a failure. `fetch` only rejects on network errors, so
 *     without this a 500 resolves normally and its error body gets parsed as if it were
 *     data — which is how a failed create ends up appearing in the table as a row.
 *  3. Notice 401 in one place and hand control back to the login screen.
 */

const SAFE_METHODS = ['GET', 'HEAD', 'OPTIONS', 'TRACE']

export class ApiError extends Error {
  constructor(status, message) {
    super(message)
    this.name = 'ApiError'
    this.status = status
  }
}

/** Spring's CookieCsrfTokenRepository writes XSRF-TOKEN; it expects X-XSRF-TOKEN back. */
function readCsrfToken() {
  const match = document.cookie.match(/(?:^|;\s*)XSRF-TOKEN=([^;]*)/)
  return match ? decodeURIComponent(match[1]) : null
}

// The router registers a handler here so a session that expired mid-session lands the user
// back on the login screen instead of showing a wall of failed panels.
let unauthenticatedHandler = null

export function setUnauthenticatedHandler(handler) {
  unauthenticatedHandler = handler
}

async function readErrorMessage(response, fallback) {
  try {
    const text = await response.text()
    if (!text) return fallback
    const parsed = JSON.parse(text)
    return parsed.message || parsed.error || fallback
  } catch {
    return fallback
  }
}

/**
 * Drop-in replacement for `fetch`. Resolves with the Response on success, throws
 * {@link ApiError} otherwise — so the `try/catch` already wrapped around these calls
 * finally sees server-side failures.
 */
export async function apiFetch(url, options = {}) {
  const method = (options.method || 'GET').toUpperCase()
  const headers = { ...(options.headers || {}) }

  if (!SAFE_METHODS.includes(method)) {
    const token = readCsrfToken()
    if (token) {
      headers['X-XSRF-TOKEN'] = token
    }
  }

  const response = await fetch(url, {
    credentials: 'same-origin',
    ...options,
    headers
  })

  if (response.ok) {
    return response
  }

  if (response.status === 401) {
    if (unauthenticatedHandler) unauthenticatedHandler()
    throw new ApiError(401, 'Your session has ended. Please sign in again.')
  }

  if (response.status === 403) {
    throw new ApiError(403, await readErrorMessage(
      response, 'You do not have permission to do that.'))
  }

  throw new ApiError(response.status, await readErrorMessage(
    response, `Request failed (${response.status})`))
}

/** Convenience for the common case: GET and parse JSON. */
export async function apiGet(url) {
  const response = await apiFetch(url)
  return response.json()
}

/** Ensures a CSRF cookie exists before the first write of the session. */
export async function primeCsrfToken() {
  if (readCsrfToken()) return
  try {
    await fetch('/api/auth/me', { credentials: 'same-origin' })
  } catch {
    // Offline or the server is down — the caller's own request will report it.
  }
}
