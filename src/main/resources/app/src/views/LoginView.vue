<script setup>
import { computed, ref } from 'vue'
import { useStore } from 'vuex'
import { useRoute, useRouter } from 'vue-router'
import { firstAllowedPath } from '../router/access'

const store = useStore()
const router = useRouter()
const route = useRoute()

const username = ref('')
const password = ref('')

const loading = computed(() => store.getters['user/loading'])
const error = computed(() => store.getters['user/error'])

async function submit() {
  if (!username.value.trim() || !password.value) {
    store.commit('user/SET_ERROR', 'Enter your username and password.')
    return
  }

  const ok = await store.dispatch('user/login', {
    username: username.value.trim(),
    password: password.value
  })

  if (!ok) {
    password.value = ''
    return
  }

  // Go where they were headed, or to the first page their roles allow. The landing page
  // is derived from the routes rather than hard-coded per role, so adding a route cannot
  // leave this list out of date.
  const redirect = route.query.redirect
  router.replace(redirect || firstAllowedPath(store.getters['user/roles']))
}
</script>

<template>
  <div class="login-page">
    <form class="card" @submit.prevent="submit">
      <h1>Sign in</h1>
      <p class="subtitle">Management System</p>

      <label class="field">
        <span>Username</span>
        <input
            v-model="username"
            type="text"
            autocomplete="username"
            autofocus
            :disabled="loading"
        />
      </label>

      <label class="field">
        <span>Password</span>
        <input
            v-model="password"
            type="password"
            autocomplete="current-password"
            :disabled="loading"
        />
      </label>

      <p v-if="error" class="error">{{ error }}</p>

      <button type="submit" class="submit-btn" :disabled="loading">
        {{ loading ? 'Signing in…' : 'Sign in' }}
      </button>
    </form>
  </div>
</template>

<style scoped>
.login-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background-color: #f5f6fa;
}

.card {
  background: white;
  padding: 40px;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  width: 380px;
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.card h1 {
  margin: 0;
  color: #2c3e50;
  font-size: 24px;
}

.subtitle {
  margin: -14px 0 0 0;
  color: #7f8c8d;
  font-size: 14px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.field span {
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: #7f8c8d;
}

.field input {
  padding: 10px 12px;
  border: 1px solid #dcdde1;
  border-radius: 6px;
  font-size: 15px;
}

.field input:focus {
  outline: 2px solid #3498db;
  outline-offset: 1px;
  border-color: #3498db;
}

.submit-btn {
  padding: 12px;
  background-color: #3498db;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 15px;
  font-weight: 600;
}

.submit-btn:hover:not(:disabled) {
  background-color: #2980b9;
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.error {
  margin: 0;
  color: #e74c3c;
  font-size: 14px;
}
</style>
