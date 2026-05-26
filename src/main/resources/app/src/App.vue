<script setup>
import { computed } from 'vue'
import { RouterView, RouterLink, useRouter } from 'vue-router'
import { useStore } from 'vuex'

const store = useStore()
const router = useRouter()

// read from user module
const role = computed(() => store.getters['user/role'])
const name = computed(() => store.getters['user/name'])
const canAccess = (routeName) => store.getters['user/canAccess'](routeName)

// all possible sidebar links
const allLinks = [
  { name: 'students',   label: 'Students',   path: '/students' },
  { name: 'courses',    label: 'Courses',     path: '/courses' },
  { name: 'professors', label: 'Professors',  path: '/professors' },
  { name: 'programs',   label: 'Programs',    path: '/programs' },
  { name: 'enrollments', label: 'Enrollments',    path: '/enrollments' },
  { name: 'assignments',   label: 'Assignments',    path: '/assignments' },
  { name: 'studentProfile',   label: 'Student Profile',    path: '/student/studentProfileView' },
  { name: 'professorProfile',   label: 'Professor Profile',    path: '/professor/professorProfileView' }

]

function logout() {
  store.dispatch('user/clearRole')
  router.push('/')
}
</script>

<template>
  <div class="layout">

    <!-- SIDEBAR — only show when role is selected -->
    <aside class="sidebar" v-if="role">
      <div class="sidebar-header">
        <h2>Management System</h2>
        <p class="welcome">Hello, {{ name }}</p>
        <p class="role-badge">{{ role }}</p>
      </div>

      <nav>
        <template v-for="link in allLinks" :key="link.name">
          <RouterLink
              v-if="canAccess(link.name)"
              :to="link.path">
            {{ link.label }}
          </RouterLink>
        </template>
      </nav>

      <button @click="logout" class="logout-btn">
        Switch Role
      </button>
    </aside>

    <!-- CONTENT AREA -->
    <main class="content">
      <RouterView />
    </main>

  </div>
</template>

<style scoped>
.layout {
  display: flex;
  height: 100vh;
}

.sidebar {
  width: 150px;
  background-color: #2c3e50;
  padding: 24px 16px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.sidebar-header {
  margin-bottom: 24px;
}

.sidebar h2 {
  color: white;
  font-size: 16px;
  margin-bottom: 8px;
}

.welcome {
  color: #ecf0f1;
  font-size: 14px;
  margin: 4px 0;
}

.role-badge {
  color: #3498db;
  font-size: 12px;
  text-transform: uppercase;
  margin: 4px 0;
}

.sidebar nav {
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 1;
}

.sidebar nav a {
  color: #bdc3c7;
  text-decoration: none;
  padding: 10px 12px;
  border-radius: 6px;
  font-size: 14px;
}

.sidebar nav a:hover {
  background-color: #34495e;
  color: white;
}

.sidebar nav a.router-link-active {
  background-color: #3498db;
  color: white;
}

.content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  background-color: #f5f6fa;
}

.logout-btn {
  padding: 10px;
  background-color: #e74c3c;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  margin-top: 16px;
}
</style>