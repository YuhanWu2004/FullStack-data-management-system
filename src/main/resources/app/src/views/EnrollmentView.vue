<script setup>
import {ref, computed, onMounted, watch} from 'vue'
import { useStore } from 'vuex'
import { usePagination } from '../composables/usePagination'
import DeleteModal from '../components/DeleteModal.vue'
import EditModal from '../components/EditModal.vue'
import Pagination from '../components/Pagination.vue'

const store = useStore()

// ── READ FROM STORE ───────────────────────────
const enrollments = computed(() => store.getters['enrollment/enrollments'])
const loading = computed(() => store.getters['assignment/loading'])
const error = computed(() => store.getters['assignment/error'])

const totalItems = computed(() => store.getters['assignment/totalItems'])
const totalPages = computed(() => store.getters['assignment/totalPages'])
const currentPage = computed(() => store.getters['assignment/currentPage'])
const pageSize = computed(() => store.getters['assignment/pageSize'])

// ── SEARCH ────────────────────────────────────
const searchStudentId = ref('')
const searchCourseId = ref('')
const searchId = ref('')

const searchStudentName = ref('')
const searchCourseName = ref('')

// ── MODAL ─────────────────────────────────────
const showCreateModal = ref(false)
// const showEnrollProgramModal = ref(false)
const showDeleteModal = ref(false)
const selectedEnrollment = ref(null)
const formError = ref(null)

const form = ref({
  studentId: null,
  courseId: null,
  grade: null
})

// ── MODAL AUTOCOMPLETE SEARCH ─────────────────
const studentSearchQuery = ref('')
const courseSearchQuery = ref('')

const studentSearchResults = ref([])
const courseSearchResults = ref([])

const showStudentDropdown = ref(false)
const showCourseDropdown = ref(false)

let modalSearchTimeout = null

// --- NEW: Pagination State for Dropdowns ---
const studentSearchPage = ref(0)
const studentSearchTotalPages = ref(0)
const isLoadingMoreStudents = ref(false)


const courseSearchPage = ref(0)
const courseSearchTotalPages = ref(0)
const isLoadingMoreCourses = ref(false)

// ── FILTERED ────────────────────────────────

// ── SEARCH FUNCTIONS ──────────────────────────
let searchTimeout = null
watch(
    [searchStudentId, searchCourseId, searchId, searchStudentName, searchCourseName],
    () => {
      clearTimeout(searchTimeout)

      searchTimeout = setTimeout(() => {
        store.dispatch('enrollment/fetchEnrollments', {
          page: 0,
          size: 10,
          searchStudentId: searchStudentId.value,
          searchCourseId: searchCourseId.value,
          searchId: searchId.value,
          searchStudentName: searchStudentName.value,
          searchCourseName: searchCourseName.value
        })
      }, 300)
    }
)


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
  form.value.studentId = student.id
  studentSearchQuery.value = `${student.firstName} ${student.lastName}`
  showStudentDropdown.value = false
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
      // Reset page to 0 for a brand new search
      courseSearchPage.value = 0 
      const query = encodeURIComponent(courseSearchQuery.value)

      const res = await fetch(`http://localhost:8080/api/course/search/name?value=${query}&page=0&size=10`)
      const data = await res.json()
      
      // Assign results and track total pages from backend
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
    
    // Append the newly fetched courses to the end of the existing list
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

// Hide dropdowns when clicking outside the input (using a small delay so the click registers first)
function hideDropdowns() {
  setTimeout(() => {
    showStudentDropdown.value = false
    showCourseDropdown.value = false
  }, 200)
}

// ── PAGINATION ────────────────────────────────
function onPageChanged(page) {
  store.dispatch('enrollment/changePage', page - 1)
}
function resetPage() {
  currentPage.value = 1
}

// ── SEARCH FUNCTIONS ──────────────────────────

function clearSearch() {
  searchStudentId.value = ''
  searchCourseId.value = ''
  searchId.value = ''
  searchStudentName.value = ''
  searchCourseName.value = ''
  store.dispatch('enrollment/fetchEnrollments', {page: 0, size: 10})
}

function onSearchInput() {
  resetPage()
}

// ── VALIDATION ────────────────────────────────
function validateForm() {
  if (!form.value.studentId) {
    formError.value = 'Please select a student'
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
  form.value = { studentId: null, courseId: null, grade: null }
  formError.value = null
  showCreateModal.value = true
  studentSearchQuery.value = ''
  courseSearchQuery.value = ''
}

async function onCreateSaved() {
  if (!validateForm()) return
  await store.dispatch('enrollment/createEnrollment', form.value)
  showCreateModal.value = false
}

// ── DELETE ────────────────────────────────────
function openDeleteModal(enrollment) {
  selectedEnrollment.value = enrollment
  showDeleteModal.value = true
}

async function onDeleteConfirmed() {
  await store.dispatch('enrollment/deleteEnrollment', selectedEnrollment.value.id)
  showDeleteModal.value = false
  selectedEnrollment.value = null
}

// ── LIFECYCLE ─────────────────────────────────
onMounted(() => {
  store.dispatch('enrollment/fetchEnrollments')
})
</script>

<template>
  <div class="enrollment-page">

    <!-- PAGE HEADER -->
    <div class="page-header">
      <div>
        <h1>Student Enrollments</h1>
        <p class="total-count">Total: {{ totalItems }} enrollments</p>
      </div>
      <button @click="openCreateModal" class="add-btn">
        + Enroll Course
      </button>
    </div>

    <!-- ERROR -->
    <p v-if="error" class="error-message">{{ error }}</p>

    <!-- SEARCH BAR -->
    <div class="search-bar">
      <input
          v-model="searchStudentName"
          placeholder="Search by student name"
          @input="onSearchInput"
      />
      
      <input
          v-model="searchCourseName"
          placeholder="Search by course name"
          @input="onSearchInput"
      />
      <input
          v-model="searchId"
          placeholder="Search by enrollment Id"
          @input="onSearchInput"
      />
      <input
          v-model="searchStudentId"
          placeholder="Search by student Id"
          @input="onSearchInput"
      />
      <input
          v-model="searchCourseId"
          placeholder="Search by course Id"
          @input="onSearchInput"
      />
      <button @click="clearSearch" class="clear-btn">Clear</button>
    </div>

    <!-- LOADING -->
    <div v-if="loading" class="loading">Loading enrollments...</div>

    <!-- TABLE -->
    <div v-else>
      <table>
        <thead>
        <tr>
          <th>ID</th>
          <th>Student</th>
          <th>Course</th>
          <th>Grade</th>
          <th>Program</th>
          <th>Actions</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="enrollment in enrollments" :key="enrollment.id">
          <td>{{ enrollment.id }}</td>
          <td>{{ enrollment.student?.firstName }} {{ enrollment.student?.lastName }}</td>
          <td>{{  enrollment.course?.name }}</td>
          <td>{{ enrollment.grade ?? 'Not graded' }}</td>
          <td>{{ enrollment.student?.program?.name}}</td>
          <td class="actions">
            <button @click="openDeleteModal(enrollment)" class="delete-btn">
              Remove
            </button>
          </td>
        </tr>
        <tr v-if="enrollments.length === 0">
          <td colspan="5" class="empty-state">No enrollments found</td>
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
        title="Enroll Student to Course"
        saveLabel="Enroll"
        :error="formError"
        @save="onCreateSaved"
        @cancel="showCreateModal = false"
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

      <div class="form-group">
        <label>Grade (optional)</label>
        <input
            v-model.number="form.grade"
            type="number"
            placeholder="Enter grade"
            min="0"
            max="100"
        />
      </div>


    </EditModal>

    <!-- DELETE MODAL -->
    <DeleteModal
        v-if="showDeleteModal && selectedEnrollment"
        title="Remove Enrollment"
        :message="`Are you sure you want to remove ${selectedEnrollment.student?.firstName} ${selectedEnrollment.student?.lastName} from ${selectedEnrollment.course?.name}?`"
        @confirm="onDeleteConfirmed"
        @cancel="showDeleteModal = false"
    />

  </div>
</template>

<style scoped>
.enrollment-page {
  padding: 24px;
}
</style>