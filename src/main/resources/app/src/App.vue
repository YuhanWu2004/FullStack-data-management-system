<script setup>
import { computed } from 'vue'
import { RouterView, RouterLink, useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { navItems } from './router/access'

const store = useStore()
const router = useRouter()

// read from user module — all of it server-supplied via GET /api/auth/me
const authenticated = computed(() => store.getters['user/authenticated'])
const displayName = computed(() => store.getters['user/displayName'])
const roleLabel = computed(() => store.getters['user/roleLabel'])

/**
 * The sidebar is derived from route meta rather than a second hand-written list, so a link
 * can no longer point at a path that does not exist or be filtered by a name that does not
 * match its route.
 */
const links = computed(() => navItems(store.getters['user/roles']))

async function logout() {
  await store.dispatch('user/logout')
  router.push({ name: 'login' })
}
</script>

<template>
  <div class="layout">

    <!-- SIDEBAR — only once there is a session -->
    <aside class="sidebar" v-if="authenticated">
      <div class="sidebar-header">
        <h2>Management System</h2>
        <p class="welcome">Hello, {{ displayName }}</p>
        <p class="role-badge">{{ roleLabel }}</p>
      </div>

      <nav>
        <RouterLink
            v-for="link in links"
            :key="link.name"
            :to="link.path">
          {{ link.label }}
        </RouterLink>
      </nav>

      <button @click="logout" class="logout-btn">
        Sign out
      </button>
    </aside>

    <!-- CONTENT AREA -->
    <main class="content" :class="{ 'content-plain': !authenticated }">
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

/* The login screen centres itself, so it gets no page padding to fight with. */
.content-plain {
  padding: 0;
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