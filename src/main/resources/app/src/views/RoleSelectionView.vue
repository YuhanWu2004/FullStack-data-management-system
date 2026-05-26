<script setup>
import { computed, onMounted, ref } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'

const store = useStore()
const router = useRouter()

const name = ref('')
const selectedUserId = ref(null)
const selectedRole = ref(null)
const error = ref(null)

const students = computed(() => store.getters['student/students'])
const professors = computed(() => store.getters['professor/professors'])

// ── LIFECYCLE ─────────────────────────────────
onMounted(() => {
  store.dispatch('student/fetchStudents')
  store.dispatch('professor/fetchProfessors')
  // auto-login
  // store.dispatch('user/setRole', {
  //     role: 'student',
  //     name: 'Michelle Wu',
  //     userId: 2160
  //   })
  // router.push('../programs')

  store.dispatch('user/setRole', {
    role: 'staff',
    name: 'Michelle'
  })
  router.push('../enrollments')
})

// ── FUNCTIONS ─────────────────────────────────
function selectRole(role) {
  selectedRole.value = role
  selectedUserId.value = null
  error.value = null
}

function confirm() {
  if (!name.value.trim()) {
    error.value = 'Please enter your name'
    return
  }

  if (!selectedRole.value) {
    error.value = 'Please select a role'
    return
  }

  if (selectedRole.value !== 'staff' && !selectedUserId.value) {
    error.value = 'Please select who you are'
    return
  }

  store.dispatch('user/setRole', {
    role: selectedRole.value,
    name: name.value.trim(),
    userId: selectedUserId.value   // null for staff, id for student/professor
  })

  const allowedRoutes = store.getters['user/allowedRoutes']
  router.push('/' + allowedRoutes[0])
}
</script>

<template>
  <div class="role-selection">
    <div class="card">
      <h1>Welcome</h1>
      <p>Please enter your name and select your role to continue.</p>

      <!-- name input -->
      <input
          v-model="name"
          placeholder="Enter your name"
          class="name-input"
      />

      <!-- role buttons -->
      <div class="role-buttons">
        <button
            :class="{ active: selectedRole === 'student' }"
            @click="selectRole('student')">
          Student
        </button>
        <button
            :class="{ active: selectedRole === 'professor' }"
            @click="selectRole('professor')">
          Professor
        </button>
        <button
            :class="{ active: selectedRole === 'staff' }"
            @click="selectRole('staff')">
          Staff
        </button>
      </div>

      <!-- student dropdown -->
      <div v-if="selectedRole === 'student'" class="form-group">
        <label>Select your profile</label>
        <select v-model="selectedUserId">
          <option value="" disabled>Select yourself</option>
          <option
              v-for="student in students"
              :key="student.id"
              :value="student.id"
          >
            {{ student.firstName }} {{ student.lastName }}
          </option>
        </select>
      </div>

      <!-- professor dropdown -->
      <div v-if="selectedRole === 'professor'" class="form-group">
        <label>Select your profile</label>
        <select v-model="selectedUserId">
          <option value="" disabled>Select yourself</option>
          <option
              v-for="professor in professors"
              :key="professor.id"
              :value="professor.id"
          >
            {{ professor.firstName }} {{ professor.lastName }}
          </option>
        </select>
      </div>

      <p v-if="error" class="error">{{ error }}</p>

      <button @click="confirm" class="confirm-btn">
        Continue →
      </button>
    </div>
  </div>
</template>