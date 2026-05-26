<script setup>
import { ref, computed, onMounted } from 'vue'
import { useStore } from 'vuex'

const store = useStore()

// ── STATE ─────────────────────────────────────
const profile = ref(null)
const enrollments = ref([])
const loading = ref(false)
const error = ref(null)

// ── GET LOGGED IN USER ID ─────────────────────
const userId = computed(() => store.getters['user/userId'])

// ── FETCH DIRECTLY FROM API ───────────────────
async function fetchProfile() {
  loading.value = true
  error.value = null
  try {
    // fetch this specific student by id
    const response = await fetch(`http://localhost:8080/api/student/${userId.value}`)
    const data = await response.json()

    profile.value = data.students[0]
    console.log(profile.value)
  } catch (err) {
    error.value = 'Failed to load profile'
    console.log('error:', err)
  } finally {
    loading.value = false
  }
}

async function fetchEnrollments() {
  try {
    console.log(userId.value)
    console.log(profile.value)
    const response = await fetch(
        `http://localhost:8080/api/enrollment/search/studentId?value=${userId.value}`
    )
    enrollments.value = await response.json()
    console.log('enrollments:', enrollments.value)
  } catch (err) {
    console.log('enrollment error:', err)
  }
}

// ── LIFECYCLE ─────────────────────────────────
onMounted(async () => {
  if (!userId.value) {
    error.value = 'No user id found — please log in again'
    return
  }
  await fetchProfile()
  await fetchEnrollments()
})
</script>

<template>
  <div class="profile-page">

    <!-- LOADING -->
    <div v-if="loading" class="loading">Loading profile...</div>

    <!-- ERROR -->
    <p v-if="error" class="error-message">{{ error }}</p>

    <!-- PERSONAL INFO -->
    <div class="profile-card" v-if="profile">
      <h1>My Profile</h1>
      <div class="info-grid">
        <div class="info-item">
          <label>First Name</label>
          <p>{{ profile.firstName }}</p>
        </div>
        <div class="info-item">
          <label>Last Name</label>
          <p>{{ profile.lastName }}</p>
        </div>
        <div class="info-item">
          <label>GPA</label>
          <p>{{ profile.gpa ?? 'N/A' }}</p>
        </div>
        <div class="info-item">
          <label>Date of Birth</label>
          <p>{{ profile.dateOfBirth ?? 'N/A' }}</p>
        </div>
        <div class="info-item">
          <label>Program</label>
          <p>{{ profile.program?.name ?? 'Not enrolled in a program' }}</p>
        </div>
      </div>
    </div>

    <!-- MY COURSES -->
    <div class="courses-section">
      <h2>My Courses</h2>
      <table v-if="enrollments.size > 0">
        <thead>
        <tr>
          <th>Course ID</th>
          <th>Course Name</th>
          <th>Grade</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="enrollment in enrollments.enrollments" :key="enrollment.id">
          <td>{{ enrollment.course?.id }}</td>
          <td>{{ enrollment.course?.name }}</td>
          <td>{{ enrollment.grade ?? 'Not graded' }}</td>
        </tr>
        </tbody>
      </table>

      <p v-else-if="!loading" class="empty-state">
        Not enrolled in any courses yet
      </p>
    </div>

  </div>
</template>

<style scoped>
.profile-page { padding: 24px; }

.profile-card {
  background: white;
  padding: 24px;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.1);
  margin-bottom: 24px;
}

.profile-card h1 {
  margin: 0 0 16px 0;
  color: #2c3e50;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.info-item label {
  font-size: 12px;
  color: #7f8c8d;
  text-transform: uppercase;
  font-weight: 600;
}

.info-item p {
  font-size: 16px;
  color: #2c3e50;
  margin-top: 4px;
}

.courses-section {
  background: white;
  padding: 24px;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.1);
}

.courses-section h2 {
  margin: 0 0 16px 0;
  color: #2c3e50;
}
</style>