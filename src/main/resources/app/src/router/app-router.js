import { createRouter, createWebHashHistory } from 'vue-router'
import store from '../store/store'
import RoleSelectionView from '../views/RoleSelectionView.vue'
import StudentView from '../views/StudentView.vue'
import CourseView from "../views/CourseView.vue";
import EnrollmentView from "../views/EnrollmentView.vue";
import AssignmentView from "../views/AssignmentView.vue";
import ProfessorView from "../views/ProfessorView.vue";
import ProgramView from "../views/ProgramView.vue";
import StudentProfileView from "../views/student/StudentProfileView.vue";
import ProfessorProfileView from "../views/professor/ProfessorProfileView.vue";

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    {
      path: '/',
      name: 'role-selection',
      component: RoleSelectionView
    },

      // staff routes
    {
      path: '/students',
      name: 'students',
      component: StudentView,
      meta: { requiresRole: ['staff'] }
    },
    {
      path: '/courses',
      name: 'courses',
      component: CourseView,
      meta: { requiresRole: ['staff'] }
    },
    {
      path: '/enrollments',
      name: 'enrollments',
      component: EnrollmentView,
      meta: { requiresRole: ['staff'] }
    },
    {
      path: '/assignments',
      name: 'assignments',
      component: AssignmentView,
      meta: { requiresRole: ['staff'] }
    },
    {
      path: '/programs',
      name: 'programs',
      component: ProgramView,
      meta: { requiresRole: ['staff'] }
    },
    {
      path: '/professors',
      name: 'professors',
      component: ProfessorView,
      meta: { requiresRole: ['staff'] }
    },
      // Student routes
    {
      path: '/student/studentProfileView',
      name: 'studentProfileView',
      component: StudentProfileView,
      meta: { requiresRole: ['student'] }
    },
    // Professor routes
    {
      path: '/professor/professorProfileView',
      name: 'professorProfileView',
      component: ProfessorProfileView,
      meta: { requiresRole: ['professor'] }
    },



  ]
})

// route guard
router.beforeEach((to) => {
  const role = store.getters['user/role']

  if (to.meta.requiresRole) {
    if (!role) {
      return {path: '/'}
      if (!to.meta.requiresRole.includes(role)) {
        return {path: '/'}
      }
    }
  }
})

export default router