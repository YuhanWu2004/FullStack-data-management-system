<script setup>
import { apiFetch } from '../../api/http'
import { ref, computed, onMounted, watch } from 'vue'
import { useStore } from 'vuex'

const store = useStore()

// ── STATE ─────────────────────────────────────
const profile = ref(null)
const enrollments = ref([])
const loading = ref(false)
const error = ref(null)

// ── GET LOGGED IN USER ID ─────────────────────
const userId = computed(() => store.getters['user/studentId'])

// ── TERM FILTER ────────────────────────────────
// Defaults to "my courses this term" — the review that preceded this feature flagged
// showing every enrollment ever made, with no filter, as easy to misread as "current".
const showAllHistory = ref(false)
const currentTermId = computed(() => store.getters['term/currentTermId'])
const currentTermName = computed(() => store.getters['term/current']?.name ?? '')

// ── FETCH DIRECTLY FROM API ───────────────────
async function fetchProfile() {
  loading.value = true
  error.value = null
  try {
    // fetch this specific student by id
    const response = await apiFetch(`/api/student/${userId.value}`)
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
    const termParam = showAllHistory.value || !currentTermId.value
        ? ''
        : `&termId=${currentTermId.value}`
    const response = await apiFetch(
        `/api/enrollment/search/studentId?value=${userId.value}&size=100${termParam}`
    )
    enrollments.value = await response.json()
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
  await store.dispatch('term/fetchCurrentTerm')
  await fetchProfile()
  await fetchEnrollments()
})

watch(showAllHistory, fetchEnrollments)
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
      <div class="courses-header">
        <h2>My Courses</h2>
        <label class="history-toggle">
          <input type="checkbox" v-model="showAllHistory" />
          Show full history (not just {{ currentTermName || 'the current term' }})
        </label>
      </div>
      <table v-if="enrollments.total > 0">
        <thead>
        <tr>
          <th>Course ID</th>
          <th>Course Name</th>
          <th>Term</th>
          <th>Grade</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="enrollment in enrollments.enrollments" :key="enrollment.id">
          <td>{{ enrollment.course?.id }}</td>
          <td>{{ enrollment.course?.name }}</td>
          <td>{{ enrollment.term?.name ?? '—' }}</td>
          <td>{{ enrollment.grade ?? 'Not graded' }}</td>
        </tr>
        </tbody>
      </table>

      <p v-else-if="!loading" class="empty-state">
        Not enrolled in any courses {{ showAllHistory ? 'yet' : `in ${currentTermName || 'the current term'}` }}
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
  margin: 0;
  color: #2c3e50;
}

.courses-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.history-toggle {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #57606f;
  cursor: pointer;
}
</style>