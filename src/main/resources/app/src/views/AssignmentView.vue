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
const loading = computed(() => store.getters['assignment/loading'])
const error = computed(() => store.getters['assignment/error'])

const totalItems = computed(() => store.getters['assignment/totalItems'])
const totalPages = computed(() => store.getters['assignment/totalPages'])
const currentPage = computed(() => store.getters['assignment/currentPage'])
const pageSize = computed(() => store.getters['assignment/pageSize'])

// ── SEARCH ────────────────────────────────────
const searchProfessorId = ref('')
const searchProfessorName = ref('')
const searchCourseId = ref('')
const searchCourseName = ref('')
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

// ── MODAL AUTOCOMPLETE SEARCH ─────────────────
const professorSearchQuery = ref('')
const courseSearchQuery = ref('')

const professorSearchResults = ref([])
const courseSearchResults = ref([])

const showProfessorDropdown = ref(false)
const showCourseDropdown = ref(false)

let modalSearchTimeout = null

// --- Pagination State for Dropdowns ---
const professorSearchPage = ref(0)
const professorSearchTotalPages = ref(0)
const isLoadingMoreProfessors = ref(false)

const courseSearchPage = ref(0)
const courseSearchTotalPages = ref(0)
const isLoadingMoreCourses = ref(false)

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
    } catch (error) {
      console.error("Failed to fetch professors for autocomplete", error)
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
  } catch (error) {
    console.error("Failed to load more professors", error)
    professorSearchPage.value-- 
  } finally {
    isLoadingMoreProfessors.value = false
  }
}

function selectProfessor(prof) {
  form.value.professorId = prof.id
  professorSearchQuery.value = `${prof.firstName} ${prof.lastName}`
  showProfessorDropdown.value = false
}

// --- Course Search ---
function handleCourseSearch() {
  clearTimeout(modalSearchTimeout)
  showCourseDropdown.value = true
  
  modalSearchTimeout = setTimeout(async () => {
    if (!courseSearchQuery.value) {
      courseSearchResults.value = []
      courseSearchPage.value = 0
      courseSearchTotalPages.value = 0
      return
    }
    
    try {
      courseSearchPage.value = 0
      const query = encodeURIComponent(courseSearchQuery.value)

      const res = await fetch(`http://localhost:8080/api/course/search/name?value=${query}&page=0&size=10`)
      const data = await res.json()
      
      courseSearchResults.value = data.courses || data.content || data
      courseSearchTotalPages.value = data.totalPages || 0
    } catch (error) {
      console.error("Failed to fetch courses for autocomplete", error)
      courseSearchResults.value = []
    }
  }, 300)
}

async function loadMoreCourses() {
  if (isLoadingMoreCourses.value) return
  isLoadingMoreCourses.value = true

  try {
    courseSearchPage.value++ 
    const query = encodeURIComponent(courseSearchQuery.value)

    const res = await fetch(`http://localhost:8080/api/course/search/name?value=${query}&page=${courseSearchPage.value}&size=10`)
    const data = await res.json()
    
    const newCourses = data.courses || data.content || data
    courseSearchResults.value = [...courseSearchResults.value, ...newCourses]
  } catch (error) {
    console.error("Failed to load more courses", error)
    courseSearchPage.value-- 
  } finally {
    isLoadingMoreCourses.value = false
  }
}

function selectCourse(course) {
  form.value.courseId = course.id
  courseSearchQuery.value = course.name
  showCourseDropdown.value = false
}

function hideDropdowns() {
  showProfessorDropdown.value = false
  showCourseDropdown.value = false
}

// ── SEARCH FUNCTIONS ──────────────────────────


watch(
    [searchProfessorId, searchCourseId, searchId, searchProfessorName, searchCourseName],
    () => {
      clearTimeout(searchTimeout)

      searchTimeout = setTimeout(() => {
        store.dispatch('assignment/fetchAssignments', {
          page: 0,
          size: 10,
          searchProfessorId: searchProfessorId.value,
          searchCourseId: searchCourseId.value,
          searchId: searchId.value,
          searchProfessorName: searchProfessorName.value,
          searchCourseName: searchCourseName.value
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
  searchProfessorName.value = ''
  searchCourseName.value = ''
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
  professorSearchQuery.value = ''
  courseSearchQuery.value = ''
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
      <input v-model="searchProfessorName" placeholder="professor name" @input="onSearchInput" />
      <input v-model="searchCourseName" placeholder="course name" @input="onSearchInput" />
      <input v-model="searchId" placeholder="assignment Id" @input="onSearchInput" />
      <input v-model="searchProfessorId" placeholder="professor Id" @input="onSearchInput" />
      <input v-model="searchCourseId" placeholder="course Id" @input="onSearchInput" />
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
      <div class="form-group autocomplete-container">
        <label>Professor *</label>
        <input 
            v-model="professorSearchQuery"
            @input="handleProfessorSearch"
            @blur="hideDropdowns"
            placeholder="Type to search professor..."
            class="autocomplete-input"
        />
        <ul v-if="showProfessorDropdown && professorSearchResults.length > 0" class="dropdown-list">
          <li 
              v-for="prof in professorSearchResults" 
              :key="prof.id"
              @mousedown.prevent="selectProfessor(prof)"
          >
            {{ prof.firstName }} {{ prof.lastName }}
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

      <div class="form-group autocomplete-container">
        <label>Course *</label>
        <input 
            v-model="courseSearchQuery"
            @input="handleCourseSearch"
            @blur="hideDropdowns"
            placeholder="Type to search course..."
            class="autocomplete-input"
        />
        <ul v-if="showCourseDropdown && courseSearchResults.length > 0" class="dropdown-list">
          <li 
              v-for="course in courseSearchResults" 
              :key="course.id"
              @mousedown.prevent="selectCourse(course)"
          >
            {{ course.name }}
          </li>
          <li 
              v-if="courseSearchPage < courseSearchTotalPages - 1" 
              @mousedown.prevent="loadMoreCourses"
              class="load-more-item"
          >
            {{ isLoadingMoreCourses ? 'Loading...' : 'Load more courses...' }}
          </li>
        </ul>
        <div v-if="showCourseDropdown && courseSearchResults.length === 0 && courseSearchQuery" class="dropdown-list empty">
          No courses found
        </div>
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