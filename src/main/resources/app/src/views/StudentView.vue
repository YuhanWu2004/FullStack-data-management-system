<script setup>
import {ref, computed, onMounted, watch} from 'vue'
import { usePagination } from '../composables/usePagination'
import { useStore } from 'vuex'
import Pagination from '../components/Pagination.vue'
import DeleteModal from '../components/DeleteModal.vue'
import EditModal from '../components/EditModal.vue'

const store = useStore()
const selectedStudent = ref(null)

// ── READ FROM STORE ──────────────────────────
const students = computed(() => store.getters['student/students'])
const loading = computed(() => store.getters['student/loading'])
const error = computed(() => store.getters['student/error'])

const totalItems = computed(() => store.getters['student/totalItems'])
const totalPages = computed(() => store.getters['student/totalPages'])
const currentPage = computed(() => store.getters['student/currentPage'])
const pageSize = computed(() => store.getters['student/pageSize'])

// ── SEARCH ───────────────────────────────────
const searchFirstName = ref('')
const searchLastName = ref('')
const searchId = ref('')
const searchMinGpa = ref('')

// ── MODAL STATE ───────────────────────────────
const showCreateModal = ref(false)
const showEditModal = ref(false)
const showDeleteModal = ref(false)
const formError = ref(null)

const form = ref({
  id: null,
  firstName: '',
  lastName: '',
  gpa: 0,
  dateOfBirth: ''
})


// ── SEARCH FUNCTIONS ──────────────────────────
let searchTimeout = null
watch(searchFirstName, (newVal) => {
  clearTimeout(searchTimeout)
  searchTimeout = setTimeout(() => {
    store.dispatch('student/fetchStudents', {
      page: 0,
      size: 10,
      searchFirstName: searchFirstName.value
    })
  }, 500)
})

watch(searchLastName, (newVal) => {
  clearTimeout(searchTimeout)
  searchTimeout = setTimeout(() => {
    store.dispatch('student/fetchStudents', {
      page: 0,
      size: 10,
      searchLastName: searchLastName.value
    })
  }, 500)
})
watch(searchId, (newVal) => {
  clearTimeout(searchTimeout)
  searchTimeout = setTimeout(() => {
    store.dispatch('student/fetchStudents', {
      page: 0,
      size: 10,
      searchId: searchId.value
    })
  }, 300)
})

watch(searchMinGpa, (newVal) => {
  clearTimeout(searchTimeout)
  searchTimeout = setTimeout(() => {
    store.dispatch('student/fetchStudents', {
      page: 0,
      size: 10,
      searchMinGpa: searchMinGpa.value
    })
  }, 300)
})

// ── PAGINATION ────────────────────────────────
function onPageChanged(page) {
  store.dispatch('student/changePage', page - 1)
}
function resetPage() {
  currentPage.value = 1    // call this when search changes
}


// ── SEARCH FUNCTIONS ──────────────────────────
function clearSearch() {
  searchFirstName.value = ''
  searchLastName.value = ''
  searchId.value = ''
  searchMinGpa.value = ''
  store.dispatch('student/fetchStudents', { page: 0, size: 10})
}
function onSearchInput() {
  resetPage()
}

// ── VALIDATION ────────────────────────────────
function validateForm() {
  if (!form.value.firstName.trim()) {
    formError.value = 'First name is required'
    return false
  }
  if (!form.value.lastName.trim()) {
    formError.value = 'Last name is required'
    return false
  }
  if (form.value.gpa < 0 || form.value.gpa > 4) {
    formError.value = 'GPA must be between 0 and 4'
    return false
  }
  formError.value = null
  return true
}

// ── CREATE ────────────────────────────────────
function openCreateModal() {
  form.value = { id: null, firstName: '', lastName: '', gpa: 0, dateOfBirth: '' }
  formError.value = null
  showCreateModal.value = true
}

async function onCreateSaved() {
  if (!validateForm()) return
  await store.dispatch('student/createStudent', form.value)
  showCreateModal.value = false
}

// ── EDIT ──────────────────────────────────────
function openEditModal(student) {
  form.value = { ...student }
  selectedStudent.value = student
  formError.value = null
  showEditModal.value = true
}

async function onEditSaved() {
  if (!validateForm()) return
  await store.dispatch('student/updateStudent', form.value)
  showEditModal.value = false
  selectedStudent.value = null
}

// ── DELETE ────────────────────────────────────
function openDeleteModal(student) {
  selectedStudent.value = student
  showDeleteModal.value = true
}

async function onDeleteConfirmed() {
  await store.dispatch('student/deleteStudent', selectedStudent.value.id)
  showDeleteModal.value = false
  selectedStudent.value = null
}

// ── LIFECYCLE ─────────────────────────────────
onMounted(() => {
  store.dispatch('student/fetchStudents')
})
</script>

<template>
  <div class="student-page">

    <!-- PAGE HEADER -->
    <div class="page-header">
      <div>
        <h1>Students</h1>
        <p class="total-count">Total: {{ totalItems }} students</p>
      </div>
      <button @click="openCreateModal" class="add-btn">
        + Add Student
      </button>
    </div>

    <!-- ERROR -->
    <p v-if="error" class="error-message">{{ error }}</p>

    <!-- SEARCH BAR -->
    <div class="search-bar">
      <input
          v-model="searchId"
          placeholder="Search by ID"
          type="number"
          @input="onSearchInput"
      />
      <input
          v-model="searchFirstName"
          placeholder="Search by first name"
          @input="onSearchInput"
      />
      <input
          v-model="searchLastName"
          placeholder="Search by last name"
          @input="onSearchInput"
      />
      <input
          v-model="searchMinGpa"
          placeholder="Min GPA (e.g. 3.5)"
          type="number"
          step="0.1"
          min="0"
          max="4"
          @input="onSearchInput"
      />
      <button @click="clearSearch" class="clear-btn">Clear</button>
    </div>

    <!-- LOADING -->
    <div v-if="loading" class="loading">Loading students...</div>

    <!-- TABLE -->
    <div v-else>
      <table>
        <thead>
        <tr>
          <th>ID</th>
          <th>First Name</th>
          <th>Last Name</th>
          <th>GPA</th>
          <th>Date of Birth</th>
          <th>Actions</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="student in students" :key="student.id">
          <td>{{ student.id }}</td>
          <td>{{ student.firstName }}</td>
          <td>{{ student.lastName }}</td>
          <td>{{ student.gpa }}</td>
          <td>{{ student.dateOfBirth }}</td>
          <td class="actions">
            <button @click="openEditModal(student)" class="edit-btn">
              Edit
            </button>
            <button @click="openDeleteModal(student)" class="delete-btn">
              Delete
            </button>
          </td>
        </tr>
        <tr v-if="students.length === 0">
          <td colspan="6" class="empty-state">No students found</td>
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

    <!-- CREATE MODAL — outside table, outside v-for -->
    <EditModal
        v-if="showCreateModal"
        title="Add Student"
        saveLabel="Create Student"
        :error="formError"
        @save="onCreateSaved"
        @cancel="showCreateModal = false"
    >
      <div class="form-group">
        <label>First Name *</label>
        <input v-model="form.firstName" placeholder="First name" />
      </div>
      <div class="form-group">
        <label>Last Name *</label>
        <input v-model="form.lastName" placeholder="Last name" />
      </div>
      <div class="form-group">
        <label>GPA (0 - 4) *</label>
        <input v-model.number="form.gpa" type="number" step="0.1" min="0" max="4" />
      </div>
      <div class="form-group">
        <label>Date of Birth *</label>
        <input v-model="form.dateOfBirth" type="date" />
      </div>
    </EditModal>

    <!-- EDIT MODAL — outside table, outside v-for -->
    <EditModal
        v-if="showEditModal && selectedStudent"
        title="Edit Student"
        saveLabel="Save Changes"
        :error="formError"
        @save="onEditSaved"
        @cancel="showEditModal = false"
    >
      <div class="form-group">
        <label>First Name *</label>
        <input v-model="form.firstName" placeholder="First name" />
      </div>
      <div class="form-group">
        <label>Last Name *</label>
        <input v-model="form.lastName" placeholder="Last name" />
      </div>
      <div class="form-group">
        <label>GPA (0 - 4)</label>
        <input v-model.number="form.gpa" type="number" step="0.1" min="0" max="4" />
      </div>
      <div class="form-group">
        <label>Date of Birth</label>
        <input v-model="form.dateOfBirth" type="date" />
      </div>
    </EditModal>

    <!-- DELETE MODAL — outside table, outside v-for -->
    <DeleteModal
        v-if="showDeleteModal && selectedStudent"
        title="Delete Student"
        :message="`Are you sure you want to delete ${selectedStudent.firstName} ${selectedStudent.lastName}?`"
        @confirm="onDeleteConfirmed"
        @cancel="showDeleteModal = false"
    />

  </div>
</template>

<style scoped>
.student-page {
  padding: 24px;
}
</style>