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

// ── AUTOCOMPLETE SEARCH ───────────────────────
const studentSearchQuery = ref('')
const professorSearchQuery = ref('')

const studentSearchResults = ref([])
const professorSearchResults = ref([])

const showStudentDropdown = ref(false)
const showProfessorDropdown = ref(false)

let modalSearchTimeout = null

// --- Pagination State for Dropdowns ---
const studentSearchPage = ref(0)
const studentSearchTotalPages = ref(0)
const isLoadingMoreStudents = ref(false)

const professorSearchPage = ref(0)
const professorSearchTotalPages = ref(0)
const isLoadingMoreProfessors = ref(false)


// --- Student Search ---
function handleStudentSearch() {
  clearTimeout(modalSearchTimeout)
  showStudentDropdown.value = true

  modalSearchTimeout = setTimeout(async () => {
    if (!studentSearchQuery.value) {
      studentSearchResults.value = []
      studentSearchPage.value = 0
      studentSearchTotalPages.value = 0
      return
    }

    try {
      studentSearchPage.value = 0
      const query = encodeURIComponent(studentSearchQuery.value)

      const res = await fetch(`http://localhost:8080/api/student/search/name?value=${query}&page=0&size=10`)
      const data = await res.json()

      studentSearchResults.value = data.students || data.content || data
      studentSearchTotalPages.value = data.totalPages || 0

    } catch (err) {
      console.error("Failed to fetch students for autocomplete", err)
      studentSearchResults.value = []
    }
  }, 300)
}

async function loadMoreStudents() {
  if (isLoadingMoreStudents.value) return
  isLoadingMoreStudents.value = true

  try {
    studentSearchPage.value++
    const query = encodeURIComponent(studentSearchQuery.value)

    const res = await fetch(`http://localhost:8080/api/student/search/name?value=${query}&page=${studentSearchPage.value}&size=10`)
    const data = await res.json()

    const newStudents = data.students || data.content || data
    studentSearchResults.value = [...studentSearchResults.value, ...newStudents]

  } catch (err) {
    console.error("Failed to load more students", err)
    studentSearchPage.value--
  } finally {
    isLoadingMoreStudents.value = false
  }
}

function selectStudent(student) {
  selectedUserId.value = student.id
  studentSearchQuery.value = `${student.firstName} ${student.lastName}`
  showStudentDropdown.value = false
}


// --- Professor Search ---
function handleProfessorSearch() {
  clearTimeout(modalSearchTimeout)
  showProfessorDropdown.value = true

  modalSearchTimeout = setTimeout(async () => {
    if (!professorSearchQuery.value) {
      professorSearchResults.value = []
      professorSearchPage.value = 0
      professorSearchTotalPages.value = 0
      return
    }

    try {
      professorSearchPage.value = 0
      const query = encodeURIComponent(professorSearchQuery.value)

      const res = await fetch(`http://localhost:8080/api/professor/search/name?value=${query}&page=0&size=10`)
      const data = await res.json()

      professorSearchResults.value = data.professors || data.content || data
      professorSearchTotalPages.value = data.totalPages || 0

    } catch (err) {
      console.error("Failed to fetch professors for autocomplete", err)
      professorSearchResults.value = []
    }
  }, 300)
}

async function loadMoreProfessors() {
  if (isLoadingMoreProfessors.value) return
  isLoadingMoreProfessors.value = true

  try {
    professorSearchPage.value++
    const query = encodeURIComponent(professorSearchQuery.value)

    const res = await fetch(`http://localhost:8080/api/professor/search/name?value=${query}&page=${professorSearchPage.value}&size=10`)
    const data = await res.json()

    const newProfessors = data.professors || data.content || data
    professorSearchResults.value = [...professorSearchResults.value, ...newProfessors]

  } catch (err) {
    console.error("Failed to load more professors", err)
    professorSearchPage.value--
  } finally {
    isLoadingMoreProfessors.value = false
  }
}

function selectProfessor(professor) {
  selectedUserId.value = professor.id
  professorSearchQuery.value = `${professor.firstName} ${professor.lastName}`
  showProfessorDropdown.value = false
}

function hideDropdowns() {
  setTimeout(() => {
    showStudentDropdown.value = false
    showProfessorDropdown.value = false
  }, 200)
}

// ── LIFECYCLE ─────────────────────────────────
onMounted(() => {
  // no longer pre-fetching full lists — search is on-demand
})

// ── FUNCTIONS ─────────────────────────────────
function selectRole(role) {
  selectedRole.value = role
  selectedUserId.value = null
  studentSearchQuery.value = ''
  professorSearchQuery.value = ''
  studentSearchResults.value = []
  professorSearchResults.value = []
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
    userId: selectedUserId.value
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

      <!-- student autocomplete -->
      <div v-if="selectedRole === 'student'" class="form-group autocomplete-container">
        <label>Select your profile</label>
        <input
            v-model="studentSearchQuery"
            @input="handleStudentSearch"
            @blur="hideDropdowns"
            placeholder="Type to search student..."
            class="autocomplete-input"
        />
        <ul v-if="showStudentDropdown && studentSearchResults.length > 0" class="dropdown-list">
          <li
              v-for="student in studentSearchResults"
              :key="student.id"
              @mousedown.prevent="selectStudent(student)"
          >
            {{ student.firstName }} {{ student.lastName }}
          </li>
          <li
              v-if="studentSearchPage < studentSearchTotalPages - 1"
              @mousedown.prevent="loadMoreStudents"
              class="load-more-item"
          >
            {{ isLoadingMoreStudents ? 'Loading...' : 'Load more students...' }}
          </li>
        </ul>
        <div v-if="showStudentDropdown && studentSearchResults.length === 0 && studentSearchQuery" class="dropdown-list empty">
          No students found
        </div>
      </div>

      <!-- professor autocomplete -->
      <div v-if="selectedRole === 'professor'" class="form-group autocomplete-container">
        <label>Select your profile</label>
        <input
            v-model="professorSearchQuery"
            @input="handleProfessorSearch"
            @blur="hideDropdowns"
            placeholder="Type to search professor..."
            class="autocomplete-input"
        />
        <ul v-if="showProfessorDropdown && professorSearchResults.length > 0" class="dropdown-list">
          <li
              v-for="professor in professorSearchResults"
              :key="professor.id"
              @mousedown.prevent="selectProfessor(professor)"
          >
            {{ professor.firstName }} {{ professor.lastName }}
          </li>
          <li
              v-if="professorSearchPage < professorSearchTotalPages - 1"
              @mousedown.prevent="loadMoreProfessors"
              class="load-more-item"
          >
            {{ isLoadingMoreProfessors ? 'Loading...' : 'Load more professors...' }}
          </li>
        </ul>
        <div v-if="showProfessorDropdown && professorSearchResults.length === 0 && professorSearchQuery" class="dropdown-list empty">
          No professors found
        </div>
      </div>

      <p v-if="error" class="error">{{ error }}</p>

      <button @click="confirm" class="confirm-btn">
        Continue →
      </button>
    </div>
  </div>
</template>