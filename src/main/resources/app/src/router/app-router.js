import { createRouter, createWebHashHistory } from 'vue-router'
import store from '../store/store'
import { firstAllowedPath, isAllowed, registerRoutes } from './access'
import { setUnauthenticatedHandler } from '../api/http'

import LoginView from '../views/LoginView.vue'
import NoAccessView from '../views/NoAccessView.vue'
import StudentView from '../views/StudentView.vue'
import CourseView from '../views/CourseView.vue'
import EnrollmentView from '../views/EnrollmentView.vue'
import AssignmentView from '../views/AssignmentView.vue'
import ProfessorView from '../views/ProfessorView.vue'
import ProgramView from '../views/ProgramView.vue'
import StudentProfileView from '../views/student/StudentProfileView.vue'
import ProfessorProfileView from '../views/professor/ProfessorProfileView.vue'

/**
 * `meta.roles` is the access rule, `meta.label` + `meta.nav` build the sidebar. Adding a
 * page means adding one entry here and nothing else.
 *
 * These rules decide what the UI *offers*. They are not the security boundary — every
 * endpoint behind them is independently guarded on the server, because a router guard only
 * ever governs a browser that chooses to obey it.
 */
const routes = [
  {
    path: '/',
    name: 'login',
    component: LoginView,
    meta: { public: true }
  },
  {
    path: '/no-access',
    name: 'no-access',
    component: NoAccessView
  },

  // ── Staff ────────────────────────────────────
  {
    path: '/students',
    name: 'students',
    component: StudentView,
    meta: { roles: ['staff'], label: 'Students', nav: true }
  },
  {
    path: '/courses',
    name: 'courses',
    component: CourseView,
    meta: { roles: ['staff', 'professor', 'student'], label: 'Courses', nav: true }
  },
  {
    path: '/programs',
    name: 'programs',
    component: ProgramView,
    meta: { roles: ['staff'], label: 'Programs', nav: true }
  },
  {
    path: '/professors',
    name: 'professors',
    component: ProfessorView,
    meta: { roles: ['staff'], label: 'Professors', nav: true }
  },
  {
    path: '/enrollments',
    name: 'enrollments',
    component: EnrollmentView,
    meta: { roles: ['staff'], label: 'Enrollments', nav: true }
  },
  {
    path: '/assignments',
    name: 'assignments',
    component: AssignmentView,
    meta: { roles: ['staff'], label: 'Assignments', nav: true }
  },

  // ── Student ──────────────────────────────────
  {
    path: '/student/profile',
    name: 'studentProfile',
    component: StudentProfileView,
    meta: { roles: ['student'], label: 'My Profile', nav: true }
  },

  // ── Professor ────────────────────────────────
  {
    path: '/professor/profile',
    name: 'professorProfile',
    component: ProfessorProfileView,
    meta: { roles: ['professor'], label: 'My Profile', nav: true }
  }
]

registerRoutes(routes)

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

/**
 * Route guard.
 *
 * The previous version nested the role comparison inside `if (!role)` and placed it after
 * a `return`, so it never ran and every role could open every page. The two checks are
 * siblings, not nested: first "are you signed in", then "are you the right kind of user".
 */
router.beforeEach(async (to) => {
  // On a cold load we do not yet know whether a session cookie is valid. Ask once; this is
  // also what makes a page refresh keep you signed in.
  if (!store.getters['user/ready']) {
    await store.dispatch('user/fetchSession')
  }

  const authenticated = store.getters['user/authenticated']
  const roles = store.getters['user/roles']

  if (to.name === 'login') {
    return authenticated ? firstAllowedPath(roles) : true
  }

  if (!authenticated) {
    // Remember where they were going so signing in resumes it.
    return to.fullPath === '/'
        ? { name: 'login' }
        : { name: 'login', query: { redirect: to.fullPath } }
  }

  if (to.name === 'no-access') {
    return true
  }

  if (!isAllowed(to, roles)) {
    const fallback = firstAllowedPath(roles)
    // Guard against bouncing forever if the fallback is the route we just refused.
    return fallback === to.path ? { name: 'no-access' } : fallback
  }

  return true
})

// A session that expires while the app is open should land on the login screen rather than
// filling every panel with failures.
setUnauthenticatedHandler(() => {
  store.dispatch('user/sessionExpired')
  if (router.currentRoute.value.name !== 'login') {
    router.replace({ name: 'login' })
  }
})

export default router
