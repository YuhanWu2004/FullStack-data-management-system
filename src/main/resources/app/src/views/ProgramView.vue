<script setup>
import {ref, computed, onMounted, watch} from 'vue'
import { useStore } from 'vuex'
import Pagination from '../components/Pagination.vue'
import DeleteModal from '../components/DeleteModal.vue'
import EditModal from '../components/EditModal.vue'

const store = useStore()
const selectedProgram = ref(null)


// ── READ FROM STORE ───────────────────────────
const role = computed(() => store.getters['user/role'])
const programs = computed(() => store.getters['program/programs'])
const students = computed(() => store.getters['student/students'])
const loading = computed(() => store.getters['program/loading'])
const error = computed(() => store.getters['program/error'])

const totalItems = computed(() => store.getters['program/totalItems'])
const totalPages = computed(() => store.getters['program/totalPages'])
const currentPage = computed(() => store.getters['program/currentPage'])
const pageSize = computed(() => store.getters['program/pageSize'])

const searchName = ref('')
const searchId = ref('')

const showCreateModal = ref(false)
const showEditModal = ref(false)
const showDeleteModal = ref(false)
const showEnrollProgramModal = ref(false)
const programEnrollmentFormError = ref(null)
const formError = ref(null)

const form = ref({
  id: null,
  name: ''
})

const programEnrollmentForm = ref({
  studentId: null,
  programId: null
})


// ── MODAL AUTOCOMPLETE SEARCH ─────────────────
const studentSearchQuery = ref('')
const programSearchQuery = ref('')

const studentSearchResults = ref([])
const programSearchResults = ref([])

const showStudentDropdown = ref(false)
const showProgramDropdown = ref(false)

let modalSearchTimeout = null

// --- NEW: Pagination State for Dropdowns ---
const studentSearchPage = ref(0)
const studentSearchTotalPages = ref(0)
const isLoadingMoreStudents = ref(false)


const programSearchPage = ref(0)
const programSearchTotalPages = ref(0)
const isLoadingMorePrograms = ref(false)


// ── FILTERED ──────────────────────────────────

function getStudentCount(programId) {
  return students.value.filter(s => s.program?.id === programId).length
}
// ── SEARCH FUNCTIONS ──────────────────────────
let searchTimeout = null
watch([searchName, searchId], (newVal) => {
  clearTimeout(searchTimeout)
  searchTimeout = setTimeout(() => {
    store.dispatch('program/fetchPrograms', {
      page: 0,
      size: 10,
      searchName: searchName.value,
      searchId: searchId.value

    })
  }, 300)
})


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

      studentSearchResults.value = data.students || data
      studentSearchTotalPages.value = data.totalPages || 0

    } catch (error) {
      console.error("Failed to fetch students for autocomplete", error)
      studentSearchResults.value = []
    }
  }, 300)
}

async function loadMoreStudents() {
  if (isLoadingMoreStudents.value) return
  isLoadingMoreStudents.value = true

  try {
    // Increment the page
    studentSearchPage.value++
    const query = encodeURIComponent(studentSearchQuery.value)

    const res = await fetch(`http://localhost:8080/api/student/search/name?value=${query}&page=${studentSearchPage.value}&size=10`)
    const data = await res.json()

    const newStudents = data.students || data.content || data

    // Append the new students to the end of the existing list
    studentSearchResults.value = [...studentSearchResults.value, ...newStudents]

  } catch (error) {
    console.error("Failed to load more students", error)
    // Revert page increment if the network fails
    studentSearchPage.value--
  } finally {
    isLoadingMoreStudents.value = false
  }
}

function selectStudent(student) {
  programEnrollmentForm.value.studentId = student.id
  studentSearchQuery.value = `${student.firstName} ${student.lastName}`
  showStudentDropdown.value = false
}


// --- Program Search ---
function handleProgramSearch() {
  clearTimeout(modalSearchTimeout)
  showProgramDropdown.value = true

  modalSearchTimeout = setTimeout(async () => {
    if (!programSearchQuery.value) {
      programSearchResults.value = []
      programSearchPage.value = 0
      programSearchTotalPages.value = 0
      return
    }

    try {
      // Reset page to 0 for a brand new search
      programSearchPage.value = 0
      const query = encodeURIComponent(programSearchQuery.value)

      const res = await fetch(`http://localhost:8080/api/program/search/name?value=${query}&page=0&size=10`)
      const data = await res.json()

      // Assign results and track total pages from backend
      programSearchResults.value = data.programs || data.content || data
      programSearchTotalPages.value = data.totalPages || 0

    } catch (error) {
      console.error("Failed to fetch programs for autocomplete", error)
      programSearchResults.value = []
    }
  }, 300)
}

async function loadMoreCourses() {
  if (isLoadingMorePrograms.value) return
  isLoadingMorePrograms.value = true

  try {
    programSearchPage.value++
    const query = encodeURIComponent(programSearchQuery.value)

    const res = await fetch(`http://localhost:8080/api/program/search/name?value=${query}&page=${programSearchPage.value}&size=10`)
    const data = await res.json()

    const newPrograms = data.programs || data.content || data

    // Append the newly fetched programs to the end of the existing list
    programSearchResults.value = [...programSearchResults.value, ...newPrograms]

  } catch (error) {
    console.error("Failed to load more programs", error)
    programSearchPage.value--
  } finally {
    isLoadingMorePrograms.value = false
  }
}

function selectProgram(program) {
  programEnrollmentForm.value.programId = program.id
  programSearchQuery.value = program.name
  showProgramDropdown.value = false
}

// Hide dropdowns when clicking outside the input (using a small delay so the click registers first)
function hideDropdowns() {
  setTimeout(() => {
    showStudentDropdown.value = false
    showProgramDropdown.value = false
  }, 200)
}

// ── PAGINATION ────────────────────────────────
function onPageChanged(page) {
  store.dispatch('program/changePage', page - 1)
}
function resetPage() {
  currentPage.value = 1
}

// ── SEARCH FUNCTIONS ──────────────────────────
function clearSearch() {
  searchName.value = ''
  searchId.value = ''
  store.dispatch('program/fetchPrograms', { page: 0 })
}

function onSearchInput() {
  resetPage()
}

// ── VALIDATION ────────────────────────────────
function validateForm() {
  if (!form.value.name.trim()) {
    formError.value = 'Program name is required'
    return false
  }
  formError.value = null
  return true
}

function validateEnrollProgramForm() {
  if (!programEnrollmentForm.value.programId) {
    programEnrollmentFormError.value = 'Please select a program'
    return false
  }
  if (!programEnrollmentForm.value.studentId) {
    programEnrollmentFormError.value = 'Please select a student'
    return false
  }
  programEnrollmentFormError.value = null
  return true
}

// ── CREATE ────────────────────────────────────
function openCreateModal() {
  form.value = { id: null, name: '' }
  formError.value = null
  showCreateModal.value = true
}

async function onCreateSaved() {
  if (!validateForm()) return
  await store.dispatch('program/createProgram', form.value)
  showCreateModal.value = false
}

function openEnrollProgramModal() {
  programEnrollmentForm.value = { studentId: null, programId: null }
  programEnrollmentFormError.value = null
  showEnrollProgramModal.value = true
}

async function onEnrollProgramSaved() {
  console.log("we're hererere!!!")
  if (!validateEnrollProgramForm()) return
  // build the payload — student object with program
  const payload = {
    id: programEnrollmentForm.value.studentId,
    program: { id: programEnrollmentForm.value.programId }
  }

  console.log('enrolling student to program:', payload)
  await store.dispatch('student/updateStudent', payload)
  showEnrollProgramModal.value = false
}

// ── EDIT ──────────────────────────────────────
function openEditModal(program) {
  form.value = { ...program }
  selectedProgram.value = program
  formError.value = null
  showEditModal.value = true
}

async function onEditSaved() {
  if (!validateForm()) return
  await store.dispatch('program/updateProgram', form.value)
  showEditModal.value = false
  selectedProgram.value = null
}

// ── DELETE ────────────────────────────────────
function openDeleteModal(program) {
  selectedProgram.value = program
  showDeleteModal.value = true
}

async function onDeleteConfirmed() {
  await store.dispatch('program/deleteProgram', selectedProgram.value.id)
  showDeleteModal.value = false
  selectedProgram.value = null
}

// ── LIFECYCLE ─────────────────────────────────
onMounted(() => {
  store.dispatch('program/fetchPrograms', { page: 0, size: 10})
})
</script>

<template>
  <div class="program-page">

    <div class="page-header">
      <div>
        <h1>Programs</h1>
        <p class="total-count">Total: {{ totalItems }} programs</p>
      </div>
      <div class="header-actions" v-if="role === 'staff'">
      <button @click="openCreateModal" class="add-btn">
        + Add Program
      </button>
      <button @click="openEnrollProgramModal" class="add-btn">
        + Enroll Student in Program
      </button>
      </div>
    </div>

    <p v-if="error" class="error-message">{{ error }}</p>

    <div class="search-bar">
      <input v-model="searchId" placeholder="Search by ID" type="number" @input="onSearchInput" />
      <input v-model="searchName" placeholder="Search by name" @input="onSearchInput" />
      <button @click="clearSearch" class="clear-btn">Clear</button>
    </div>

    <div v-if="loading" class="loading">Loading programs...</div>

    <div v-else>
      <table>
        <thead>
        <tr>
          <th>ID</th>
          <th>Program Name</th>
          <th>Enrollments</th>
          <th v-if="role === 'staff'">Actions</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="program in programs" :key="program.id">
          <td>{{ program.id }}</td>
          <td>{{ program.name }}</td>
          <td>{{ getStudentCount(program.id) }}</td>
          <td v-if="role === 'staff'" class="actions">
            <button @click="openEditModal(program)" class="edit-btn">Edit</button>
            <button @click="openDeleteModal(program)" class="delete-btn">Delete</button>
          </td>
        </tr>
        <tr v-if="programs.length === 0">
          <td colspan="3" class="empty-state">No programs found</td>
        </tr>
        </tbody>
      </table>

      <Pagination
          :current-page="currentPage + 1"
          :total-pages="totalPages"
          :total-items="totalItems"
          :page-size="pageSize"
          @page-changed="onPageChanged"
      />
    </div>

    <!-- CREATE MODAL -->
    <EditModal
        v-if="showCreateModal"
        title="Add Program"
        saveLabel="Create Program"
        :error="formError"
        @save="onCreateSaved"
        @cancel="showCreateModal = false"
    >
      <div class="form-group">
        <label>Program Name *</label>
        <input v-model="form.name" placeholder="Program name" />
      </div>
    </EditModal>

    <EditModal
                v-if="showEnrollProgramModal"
                title="Enroll Student to Program"
                saveLabel="Enroll"
                :error="formError"
                @save="onEnrollProgramSaved"
                @cancel="showEnrollProgramModal = false"
            >
              <div class="form-group autocomplete-container">
                <label>Student *</label>
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

              <div class="form-group autocomplete-container">
                <label>Program *</label>
                <input
                    v-model="programSearchQuery"
                    @input="handleProgramSearch"
                    @blur="hideDropdowns"
                    placeholder="Type to search program..."
                    class="autocomplete-input"
                />
                <ul v-if="showProgramDropdown && programSearchResults.length > 0" class="dropdown-list">
                  <li
                      v-for="program in programSearchResults"
                      :key="program.id"
                      @mousedown.prevent="selectProgram(program)"
                  >
                    {{ program.name }}
                  </li>

                  <li
                      v-if="programSearchPage < programSearchTotalPages - 1"
                      @mousedown.prevent="loadMorePrograms"
                      class="load-more-item"
                  >
                    {{ isLoadingMorePrograms ? 'Loading...' : 'Load more programs...' }}
                  </li>
                </ul>
                <div v-if="showProgramDropdown && programSearchResults.length === 0 && programSearchQuery" class="dropdown-list empty">
                  No programs found
                </div>
              </div>


    </EditModal>

    <!-- EDIT MODAL -->
    <EditModal
        v-if="showEditModal && selectedProgram"
        title="Edit Program"
        saveLabel="Save Changes"
        :error="formError"
        @save="onEditSaved"
        @cancel="showEditModal = false"
    >
      <div class="form-group">
        <label>Program Name *</label>
        <input v-model="form.name" placeholder="Program name" />
      </div>
    </EditModal>

    <!-- DELETE MODAL -->
    <DeleteModal
        v-if="showDeleteModal && selectedProgram"
        title="Delete Program"
        :message="`Are you sure you want to delete ${selectedProgram.name}?`"
        @confirm="onDeleteConfirmed"
        @cancel="showDeleteModal = false"
    />

  </div>
</template>

<style scoped>
.program-page { padding: 24px; }
</style>