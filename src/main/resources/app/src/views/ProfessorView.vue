<script setup>
import {ref, computed, onMounted, watch} from 'vue'
import { useStore } from 'vuex'
import { usePagination } from '../composables/usePagination'
import Pagination from '../components/Pagination.vue'
import DeleteModal from '../components/DeleteModal.vue'
import EditModal from '../components/EditModal.vue'

const store = useStore()
const selectedProfessor = ref(null)

// ── READ FROM STORE ───────────────────────────
const professors = computed(() => store.getters['professor/professors'])
const programs = computed(() => store.getters['program/programs'])
const loading = computed(() => store.getters['professor/loading'])
const error = computed(() => store.getters['professor/error'])

const totalItems = computed(() => store.getters['professor/totalItems'])
const totalPages = computed(() => store.getters['professor/totalPages'])
const currentPage = computed(() => store.getters['professor/currentPage'])
const pageSize = computed(() => store.getters['professor/pageSize'])

// ── SEARCH ────────────────────────────────────
const searchName = ref('')
const searchId = ref('')

// ── MODAL ─────────────────────────────────────
const showCreateModal = ref(false)
const showEditModal = ref(false)
const showDeleteModal = ref(false)
const formError = ref(null)

const form = ref({
  id: null,
  firstName: '',
  lastName: '',
  programId: ''
})


// ── SEARCH FUNCTIONS ──────────────────────────
let searchTimeout = null

watch(
    [searchName, searchId],
    () => {
      clearTimeout(searchTimeout)

      searchTimeout = setTimeout(() => {
        store.dispatch('professor/fetchProfessors', {
          page: 0,
          size: 10,
          searchName: searchName.value,
          searchId: searchId.value
        })
      }, 500)
    }
)
// ── PAGINATION ────────────────────────────────
function onPageChanged(page) {
  store.dispatch('professor/changePage', page - 1)
}
function resetPage() {
  currentPage.value = 1
}

// ── SEARCH FUNCTIONS ──────────────────────────
function clearSearch() {
  searchName.value = ''
  searchId.value = ''
  store.dispatch('professor/fetchProfessors', {page: 0, size: 10})
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
  if (!form.value.programId) {
    formError.value = 'Program ID is required'
  }
  formError.value = null
  return true
}

// ── CREATE ────────────────────────────────────
function openCreateModal() {
  form.value = { id: null, firstName: '', lastName: '', programId: null }
  formError.value = null
  showCreateModal.value = true
}

async function onCreateSaved() {
  if (!validateForm()) return
  await store.dispatch('professor/createProfessor', form.value)
  showCreateModal.value = false
}

// ── EDIT ──────────────────────────────────────
function openEditModal(professor) {
  form.value = { ...professor }
  console.log('editing professor:', form.value)
  selectedProfessor.value = professor
  formError.value = null
  showEditModal.value = true
}

async function onEditSaved() {
  console.log('form.value at save time:', form.value)   // ← add this
  console.log('id at save time:', form.value.id)        // ← add this
  if (!validateForm()) return
  await store.dispatch('professor/updateProfessor', form.value)
  showEditModal.value = false
  selectedProfessor.value = null
}

// ── DELETE ────────────────────────────────────
function openDeleteModal(professor) {
  selectedProfessor.value = professor
  showDeleteModal.value = true
}

async function onDeleteConfirmed() {
  await store.dispatch('professor/deleteProfessor', selectedProfessor.value.id)
  showDeleteModal.value = false
  selectedProfessor.value = null
}

// ── LIFECYCLE ─────────────────────────────────
onMounted(() => {
  store.dispatch('professor/fetchProfessors')
  store.dispatch('program/fetchPrograms')
})
</script>

<template>
  <div class="professor-page">

    <div class="page-header">
      <div>
        <h1>Professors</h1>
        <p class="total-count">Total: {{ totalItems }} professors</p>
      </div>
      <button @click="openCreateModal" class="add-btn">
        + Add Professor
      </button>
    </div>

    <p v-if="error" class="error-message">{{ error }}</p>

    <div class="search-bar">
      <input v-model="searchId" placeholder="Search by ID" type="number" @input="onSearchInput" />
      <input v-model="searchName" placeholder="Search by name" @input="onSearchInput" />
      <button @click="clearSearch" class="clear-btn">Clear</button>
    </div>

    <div v-if="loading" class="loading">Loading professors...</div>

    <div v-else>
      <table>
        <thead>
        <tr>
          <th>ID</th>
          <th>First Name</th>
          <th>Last Name</th>
          <th>Program</th>
          <th>Actions</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="professor in professors" :key="professor.id">
          <td>{{ professor.id }}</td>
          <td>{{ professor.firstName }}</td>
          <td>{{ professor.lastName }}</td>
          <td>{{ professor?.program?.name || 'N/A' }}</td>
          <td class="actions">
            <button @click="openEditModal(professor)" class="edit-btn">Edit</button>
            <button @click="openDeleteModal(professor)" class="delete-btn">Delete</button>
          </td>
        </tr>
        <tr v-if="professors.length === 0">
          <td colspan="5" class="empty-state">No professors found</td>
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
        title="Add Professor"
        saveLabel="Create Professor"
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
        <label>Program *</label>
        <select v-model="form.programId">
          <option value="" disabled> Select a program</option>
          <option
            v-for="program in programs"
            :key="program.id"
            :value="program.id"
           >
            {{program.name}}
          </option>
        </select>
      </div>
    </EditModal>

    <!-- EDIT MODAL -->
    <EditModal
        v-if="showEditModal && selectedProfessor"
        title="Edit Professor"
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
        <label>Program *</label>
        <select v-model="form.programId">
          <option value="" disabled> Select a program</option>
          <option
              v-for="program in programs"
              :key="program.id"
              :value="program.id"
          >
            {{program.name}}
          </option>
        </select>
      </div>
    </EditModal>

    <!-- DELETE MODAL -->
    <DeleteModal
        v-if="showDeleteModal && selectedProfessor"
        title="Delete Professor"
        :message="`Are you sure you want to delete ${selectedProfessor.firstName} ${selectedProfessor.lastName}?`"
        @confirm="onDeleteConfirmed"
        @cancel="showDeleteModal = false"
    />

  </div>
</template>

<style scoped>
.professor-page { padding: 24px; }
</style>