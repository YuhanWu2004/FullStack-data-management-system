<script setup>
import {ref, computed, onMounted, watch} from "vue";
import Pagination from "../components/Pagination.vue";
import DeleteModal from "../components/DeleteModal.vue";
import EditModal from "../components/EditModal.vue";
import {usePagination} from "../composables/usePagination";

import {useStore} from "vuex";
import {useRouter} from "vue-router";

const store = useStore()
const router = useRouter()
const role = computed(() => store.getters['user/role'])

const courses = computed(() => store.getters['course/courses'])
const loading = computed(() => store.getters['course/loading'])
const error = computed(() => store.getters['course/error'])

const totalItems = computed(() => store.getters['course/totalItems'])
const totalPages = computed(() => store.getters['course/totalPages'])
const currentPage = computed(() => store.getters['course/currentPage'])
const pageSize = computed(() => store.getters['course/pageSize'])

const searchCourseName = ref('')
const searchId = ref('')

const showCreateModal = ref(false)
const showDeleteModal = ref(false)
const showEditModal = ref(false)
const isEditing = ref(false)
const formError = ref(null)
const selectedCourse = ref(null)


const form = ref({
  id: null,
  name: ''
})


const filteredCourses = computed(() => {
  console.log('filteredCourses:', courses.value)
  return courses.value.filter(course => {
    const matchName = (course.name || '')
        .toLowerCase()
        .includes(searchCourseName.value.toLowerCase())
    const matchId = searchId.value === ''
        ? true
        : String(course.id).includes(String(searchId.value))
    return matchName && matchId;
  })
})

// ── Search ────────────────────────────────

let searchTimeout = null

watch(searchCourseName, (newVal) => {
  clearTimeout(searchTimeout)
  searchTimeout = setTimeout(() => {
    store.dispatch('course/fetchCourses', {
      page: 0,
      size: 10,
      searchName: searchCourseName.value
    })
  }, 300)
})

watch(searchId, (newVal) => {
  clearTimeout(searchTimeout)
  searchTimeout = setTimeout(() => {
    store.dispatch('course/fetchCourses', {
      page: 0,
      size: 10,
      searchId: searchId.value
    })
  }, 300)
})

// ── PAGINATION ────────────────────────────────
function onPageChanged(page) {
  // Pagination component uses 1-based, Spring uses 0-based
  store.dispatch('course/changePage', page - 1)
}
function resetPage() {
  currentPage.value = 1    // call this when search changes
}

// ── SEARCH FUNCTIONS ──────────────────────────
function clearSearch() {
  searchCourseName.value = ''
  searchId.value = ''
  store.dispatch('course/fetchCourses', { page: 0 })
}

function onSearchInput() {
  resetPage()
}

// ── Routing ────────────────────────────────

function goToEnrollments() {
  router.push('/enrollments')
}
function goToAssignments() {
  router.push('/assignments')
}


// ── VALIDATION ────────────────────────────────
function validateForm() {
  if (!form.value.name.trim()) {
    formError.value = 'Course name is required'
    return false
  }
  formError.value = null
  return true
}

// ── CREATE ────────────────────────────────────
function openCreateModal() {
  form.value = { id: null, name: '' }
  formError.value = null
  showCreateModal.value = true    // ← correct ref
}
async function onCreateSaved() {
  if (!validateForm()) return
  await store.dispatch('course/createCourse', form.value)
  showCreateModal.value = false
}


// ── EDIT ──────────────────────────────────────
function openEditModal(course) {
  form.value = { ...course }
  selectedCourse.value = course
  formError.value = null
  showEditModal.value = true
}

async function onEditSaved() {
  if (!validateForm()) return
  await store.dispatch('course/updateCourse', form.value)
  showEditModal.value = false
  selectedCourse.value = null
}

// ── DELETE ────────────────────────────────────
function openDeleteModal(course) {
  selectedCourse.value = course
  showDeleteModal.value = true
}

async function onDeleteConfirmed() {
  await store.dispatch('course/deleteCourse', selectedCourse.value.id)
  showDeleteModal.value = false
  selectedCourse.value = null
}
// ── LIFECYCLE ─────────────────────────────────
onMounted(() => {
  store.dispatch('course/fetchCourses', { page: 0, size: 10})
})
</script>

<template>
  <div class="course-page">
    <!-- PAGE HEADER -->
    <div class="page-header">
      <div>
        <h1>Courses</h1>
        <p class="total-count">Total: {{ totalItems }} courses</p>
      </div>

      <div class="header-actions" v-if="role === 'staff'">
        <RouterLink to="/enrollments" class="nav-btn">
          Manage Enrollments
        </RouterLink>
        <RouterLink to="/assignments" class="nav-btn">
          Manage Assignments
        </RouterLink>
        <button @click="openCreateModal" class="add-btn">
          + Add Course
        </button>
      </div>

      <div class="header-actions" v-if="role === 'student'">
        <RouterLink to="/enrollments" class="nav-btn">
          Manage Enrollments
        </RouterLink>
      </div>

      <div class="header-actions" v-if="role === 'professor'">
        <RouterLink to="/assignments" class="nav-btn">
          Manage Assignments
        </RouterLink>
      </div>

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
          v-model="searchCourseName"
          placeholder="Search by course name"
          @input="onSearchInput"
      />

      <button @click="clearSearch" class="clear-btn">Clear</button>
    </div>

    <!-- LOADING -->
    <div v-if="loading" class="loading">Loading courses...</div>

    <!-- TABLE -->
    <div v-else>
      <table>
        <thead>
        <tr>
          <th>ID</th>
          <th>Course Name</th>
          <th v-if="role === 'staff'">Actions</th>
        </tr>
        </thead>
        <tbody>

        <tr v-for="course in courses" :key="course.id">
          <td>{{ course.id }}</td>
          <td>{{ course.name }}</td>
          <td  v-if="role === 'staff'" class="actions">
            <button @click="openEditModal(course)" class="edit-btn">
              Edit
            </button>
            <button @click="openDeleteModal(course)" class="delete-btn">
              Delete
            </button>
          </td>
        </tr>
        <tr v-if="courses.length === 0">
          <td colspan="6" class="empty-state">No courses found</td>
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
        title="Add Course"
        saveLabel="Create Course"
        :error="formError"
        @save="onCreateSaved"
        @cancel="showCreateModal = false"
    >
      <div class="form-group">
        <label>Course Name *</label>
        <input v-model="form.name" placeholder="Name" />
      </div>
    </EditModal>

    <!-- EDIT MODAL — outside table, outside v-for -->
    <EditModal
        v-if="showEditModal && selectedCourse"
        title="Edit Course"
        saveLabel="Save Changes"
        :error="formError"
        @save="onEditSaved"
        @cancel="showEditModal = false"
    >
      <div class="form-group">
        <label>Name *</label>
        <input v-model="form.name" placeholder="Course Name" />
      </div>
    </EditModal>
    <!-- DELETE MODAL — outside table, outside v-for -->
    <DeleteModal
        v-if="showDeleteModal && selectedCourse"
        title="Delete Course"
        :message="`Are you sure you want to delete ${selectedCourse.name} ?`"
        @confirm="onDeleteConfirmed"
        @cancel="showDeleteModal = false"
    />

  </div>
</template>
<style scoped>
.course-page {
  padding: 24px;
}
</style>