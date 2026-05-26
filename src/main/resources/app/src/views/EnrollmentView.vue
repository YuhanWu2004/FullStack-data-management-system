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
const students = computed(() => store.getters['student/students'])
const allStudents = computed(() => store.getters['student/allStudents'])
const courses = computed(() => store.getters['course/courses'])
const allCourses = computed(() => store.getters['course/allCourses'])
const programs = computed(() => store.getters['program/programs'])
const allPrograms = computed(() => store.getters['program/allPrograms'])
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



// ── FILTERED ────────────────────────────────

// ── SEARCH FUNCTIONS ──────────────────────────
let searchTimeout = null

watch(
    [searchStudentId, searchCourseId, searchId],
    () => {
      clearTimeout(searchTimeout)

      searchTimeout = setTimeout(() => {
        store.dispatch('enrollment/fetchEnrollments', {
          page: 0,
          size: 10,
          searchStudentId: searchStudentId.value,
          searchCourseId: searchCourseId.value,
          searchId: searchId.value
        })
      }, 500)
    }
)
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
  store.dispatch('student/fetchStudents')
  store.dispatch('student/fetchAllStudents')
  store.dispatch('course/fetchCourses')
  store.dispatch('course/fetchAllCourses')
  store.dispatch('program/fetchPrograms')
  store.dispatch('program/fetchAllPrograms')
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
      <div class="form-group">
        <label>Student *</label>
        <select v-model="form.studentId">
          <option value="" disabled>Select a student</option>
          <option
              v-for="student in allStudents"
              :key="student.id"
              :value="student.id"
          >
            {{ student.firstName }} {{ student.lastName }}
          </option>
        </select>
      </div>
      <div class="form-group">
        <label>Course *</label>
        <select v-model="form.courseId">
          <option value="" disabled>Select a course</option>
          <option
              v-for="course in allCourses"
              :key="course.id"
              :value="course.id"
          >
            {{ course.name }}
          </option>
        </select>
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