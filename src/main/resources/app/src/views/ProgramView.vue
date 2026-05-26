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

// ── FILTERED ──────────────────────────────────

function getStudentCount(programId) {
  return students.value.filter(s => s.program?.id === programId).length
}
// ── SEARCH FUNCTIONS ──────────────────────────
let searchTimeout = null
watch(searchName, (newVal) => {
  clearTimeout(searchTimeout)
  searchTimeout = setTimeout(() => {
    store.dispatch('program/fetchPrograms', {
      page: 0,
      size: 10,
      searchName: searchName.value
    })
  }, 300)
})

watch(searchId, (newVal) => {
  clearTimeout(searchTimeout)
  searchTimeout = setTimeout(() => {
    store.dispatch('program/fetchPrograms', {
      page: 0,
      size: 10,
      searchId: searchId.value
    })
  }, 300)
})
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
              <div class="form-group">
                <label>Student *</label>
                <select v-model="programEnrollmentForm.studentId">
                  <option value="" disabled>Select a student</option>
                  <option
                      v-for="student in students"
                      :key="student.id"
                      :value="student.id"
                  >
                    {{ student.firstName }} {{ student.lastName }}
                  </option>
                </select>
              </div>
              <div class="form-group">
                <label>Program *</label>        <!-- ← add this -->
                <select v-model="programEnrollmentForm.programId">
                  <option value="" disabled>Select a program</option>
                  <option
                      v-for="program in programs"
                      :key="program.id"
                      :value="program.id"
                  >
                    {{ program.name }}
                  </option>
                </select>
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