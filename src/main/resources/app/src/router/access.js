/**
 * One place that answers "may this role see this route?".
 *
 * Previously the same question was answered twice — once by `meta.requiresRole` in the
 * router and once by a hand-maintained `allLinks` array plus a `canAccess` getter in
 * App.vue — using two different vocabularies (paths in one, route names in the other).
 * They drifted, and the profile links stopped rendering. Route meta is now the only
 * source of truth, and the sidebar is derived from it.
 *
 * This module deliberately imports nothing: the router registers its table here, so views
 * can ask about access without importing the router and creating a cycle.
 */

let routeTable = []

export function registerRoutes(routes) {
  routeTable = routes
}

/** A route with no `meta.roles` is open to anyone signed in. */
export function isAllowed(route, roles) {
  const required = route && route.meta ? route.meta.roles : null
  if (!required || required.length === 0) return true
  return required.some((role) => roles.includes(role))
}

/** Sidebar entries this set of roles may reach, in route-declaration order. */
export function navItems(roles) {
  return routeTable
      .filter((route) => route.meta && route.meta.nav && isAllowed(route, roles))
      .map((route) => ({
        name: route.name,
        label: route.meta.label,
        path: route.path
      }))
}

/** Where to send someone after signing in: the first page their roles allow. */
export function firstAllowedPath(roles) {
  const items = navItems(roles)
  return items.length > 0 ? items[0].path : '/no-access'
}
