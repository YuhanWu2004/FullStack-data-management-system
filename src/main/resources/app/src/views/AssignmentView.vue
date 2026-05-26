<script setup>
import {ref, computed, onMounted, watch} from 'vue'
import { useStore } from 'vuex'
import { usePagination } from '../composables/usePagination'
import DeleteModal from '../components/DeleteModal.vue'
import EditModal from '../components/EditModal.vue'
import Pagination from '../components/Pagination.vue'

const store = useStore()

// ── READ FROM STORE ───────────────────────────
const assignments = computed(() => store.getters['assignment/assignments'])
const professors = computed(() => store.getters['professor/professors'])
const allProfessors = computed(() => store.getters['professor/allProfessors'])
const courses = computed(() => store.getters['course/courses'])
const allCourses = computed(() => store.getters['course/allCourses'])
const loading = computed(() => store.getters['assignment/loading'])
const error = computed(() => store.getters['assignment/error'])

const totalItems = computed(() => store.getters['enrollment/totalItems'])
const totalPages = computed(() => store.getters['enrollment/totalPages'])
const currentPage = computed(() => store.getters['enrollment/currentPage'])
const pageSize = computed(() => store.getters['enrollment/pageSize'])

// ── SEARCH ────────────────────────────────────
const searchProfessorId = ref('')
const searchCourseId = ref('')
const searchId = ref('')

// ── MODAL ─────────────────────────────────────
const showCreateModal = ref(false)
const showDeleteModal = ref(false)
const selectedAssignment = ref(null)
const formError = ref(null)

const form = ref({
  professorId: null,
  courseId: null
})

// ── SEARCH FUNCTIONS ──────────────────────────

let searchTimeout = null

watch(
    [searchProfessorId, searchCourseId, searchId],
    () => {
      clearTimeout(searchTimeout)

      searchTimeout = setTimeout(() => {
        store.dispatch('assignment/fetchAssignments', {
          page: 0,
          size: 10,
          searchProfessorId: searchProfessorId.value,
          searchCourseId: searchCourseId.value,
          searchId: searchId.value
        })
      }, 500)
    }
)

// ── PAGINATION ────────────────────────────────
function onPageChanged(page) {
  store.dispatch('assignment/changePage', page - 1)
}
function resetPage() {
  currentPage.value = 1
}

// ── SEARCH FUNCTIONS ──────────────────────────
function clearSearch() {
  searchProfessorId.value = ''
  searchCourseId.value = ''
  searchId.value = ''
  store.dispatch('assignment/fetchAssignments', {page: 0, size: 10})
}

function onSearchInput() {
  resetPage()
}

// ── VALIDATION ────────────────────────────────
function validateForm() {
  if (!form.value.professorId) {
    formError.value = 'Please select a professor'
    return false
  }
  if (!form.value.courseId) {
    formError.value = 'Please select a course'
    return false
  }
  formError.value = null
  return true
}

// ── CREATE ────────────────────────────────────
function openCreateModal() {
  form.value = { professorId: null, courseId: null }
  formError.value = null
  showCreateModal.value = true
}

async function onCreateSaved() {
  if (!validateForm()) return
  await store.dispatch('assignment/createAssignment', form.value)
  showCreateModal.value = false
}

// ── DELETE ────────────────────────────────────
function openDeleteModal(assignment) {
  selectedAssignment.value = assignment
  showDeleteModal.value = true
}

async function onDeleteConfirmed() {
  await store.dispatch('assignment/deleteAssignment', selectedAssignment.value.id)
  showDeleteModal.value = false
  selectedAssignment.value = null
}

// ── LIFECYCLE ─────────────────────────────────
onMounted(() => {
  store.dispatch('assignment/fetchAssignments')
  store.dispatch('professor/fetchProfessors')
  store.dispatch('professor/fetchAllProfessors')
  store.dispatch('course/fetchCourses')
  store.dispatch('course/fetchAllCourses')
})
</script>

<template>
  <div class="assignment-page">

    <div class="page-header">
      <div>
        <h1>Professor Assignments</h1>
        <p class="total-count">Total: {{ totalItems }} assignments</p>
      </div>
      <button @click="openCreateModal" class="add-btn">
        + Assign Professor
      </button>
    </div>

    <p v-if="error" class="error-message">{{ error }}</p>

    <div class="search-bar">
      <input
          v-model="searchId"
          placeholder="Search by assignment Id"
          @input="onSearchInput"
      />
      <input v-model="searchProfessorId" placeholder="Search by professor Id" @input="onSearchInput" />
      <input v-model="searchCourseId" placeholder="Search by course Id" @input="onSearchInput" />
      <button @click="clearSearch" class="clear-btn">Clear</button>
    </div>

    <div v-if="loading" class="loading">Loading assignments...</div>

    <div v-else>
      <table>
        <thead>
        <tr>
          <th>ID</th>
          <th>Professor</th>
          <th>Course</th>
          <th>Actions</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="assignment in assignments" :key="assignment.id">
          <td>{{ assignment.id }}</td>
          <td>{{ assignment.professor?.firstName }} {{ assignment.professor?.lastName }}</td>
          <td>{{ assignment.course?.name }}</td>
          <td class="actions">
            <button @click="openDeleteModal(assignment)" class="delete-btn">Remove</button>
          </td>
        </tr>
        <tr v-if="assignments.length === 0">
          <td colspan="4" class="empty-state">No assignments found</td>
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

    <EditModal
        v-if="showCreateModal"
        title="Assign Professor"
        saveLabel="Assign"
        :error="formError"
        @save="onCreateSaved"
        @cancel="showCreateModal = false"
    >
      <div class="form-group">
        <label>Professor *</label>
        <select v-model="form.professorId">
          <option value="" disabled>Select a professor</option>
          <option v-for="prof in allProfessors" :key="prof.id" :value="prof.id">
            {{ prof.firstName }} {{ prof.lastName }}
          </option>
        </select>
      </div>
      <div class="form-group">
        <label>Course *</label>
        <select v-model="form.courseId">
          <option value="" disabled>Select a course</option>
          <option v-for="course in allCourses" :key="course.id" :value="course.id">
            {{ course.name }}
          </option>
        </select>
      </div>
    </EditModal>

    <DeleteModal
        v-if="showDeleteModal && selectedAssignment"
        title="Remove Assignment"
        :message="`Are you sure you want to remove ${selectedAssignment.professor?.firstName} ${selectedAssignment.professor?.lastName} from ${selectedAssignment.course?.name}?`"
        @confirm="onDeleteConfirmed"
        @cancel="showDeleteModal = false"
    />

  </div>
</template>

<style scoped>
.assignment-page { padding: 24px; }

select {
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  width: 100%;
  background: white;
  cursor: pointer;
}

select:focus {
  outline: none;
  border-color: #3498db;
}
</style>