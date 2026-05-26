<script setup>
import {ref, computed, onMounted, watch} from 'vue'
import { useStore } from 'vuex'
import Pagination from '../components/Pagination.vue'
import DeleteModal from '../components/DeleteModal.vue'
import EditModal from '../components/EditModal.vue'

const store = useStore()
const selectedProfessor = ref(null)

// ── READ FROM STORE ───────────────────────────
const professors = computed(() => store.getters['professor/professors'])
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
  programId: null
})

// ── PROGRAM AUTOCOMPLETE ──────────────────────
const programSearchQuery = ref('')
const programSearchResults = ref([])
const showProgramDropdown = ref(false)
const programSearchPage = ref(0)
const programSearchTotalPages = ref(0)
const isLoadingMorePrograms = ref(false)

let programSearchTimeout = null

function handleProgramSearch() {
  clearTimeout(programSearchTimeout)
  showProgramDropdown.value = true

  programSearchTimeout = setTimeout(async () => {
    if (!programSearchQuery.value) {
      programSearchResults.value = []
      programSearchPage.value = 0
      programSearchTotalPages.value = 0
      return
    }

    try {
      programSearchPage.value = 0
      const query = encodeURIComponent(programSearchQuery.value)

      const res = await fetch(`http://localhost:8080/api/program/search/name?value=${query}&page=0&size=10`)
      const data = await res.json()

      programSearchResults.value = data.programs || data.content || data
      programSearchTotalPages.value = data.totalPages || 0

    } catch (err) {
      console.error("Failed to fetch programs for autocomplete", err)
      programSearchResults.value = []
    }
  }, 300)
}

async function loadMorePrograms() {
  if (isLoadingMorePrograms.value) return
  isLoadingMorePrograms.value = true

  try {
    programSearchPage.value++
    const query = encodeURIComponent(programSearchQuery.value)

    const res = await fetch(`http://localhost:8080/api/program/search/name?value=${query}&page=${programSearchPage.value}&size=10`)
    const data = await res.json()

    const newPrograms = data.programs || data.content || data
    programSearchResults.value = [...programSearchResults.value, ...newPrograms]

  } catch (err) {
    console.error("Failed to load more programs", err)
    programSearchPage.value--
  } finally {
    isLoadingMorePrograms.value = false
  }
}

function selectProgram(program) {
  form.value.programId = program.id
  programSearchQuery.value = program.name
  showProgramDropdown.value = false
}

function hideProgramDropdown() {
  setTimeout(() => {
    showProgramDropdown.value = false
  }, 200)
}

function resetProgramSearch() {
  programSearchQuery.value = ''
  programSearchResults.value = []
  programSearchPage.value = 0
  programSearchTotalPages.value = 0
}

// ── SEARCH ────────────────────────────────────
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
    formError.value = 'Program is required'
    return false
  }
  formError.value = null
  return true
}

// ── CREATE ────────────────────────────────────
function openCreateModal() {
  form.value = { id: null, firstName: '', lastName: '', programId: null }
  formError.value = null
  showCreateModal.value = true
  resetProgramSearch()
}

async function onCreateSaved() {
  if (!validateForm()) return
  await store.dispatch('professor/createProfessor', form.value)
  showCreateModal.value = false
}

// ── EDIT ──────────────────────────────────────
function openEditModal(professor) {
  form.value = { ...professor, programId: professor.program?.id ?? null }
  selectedProfessor.value = professor
  formError.value = null
  showEditModal.value = true
  // Pre-fill the search box with the existing program name
  programSearchQuery.value = professor.program?.name ?? ''
  programSearchResults.value = []
}

async function onEditSaved() {
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

    <!-- SHARED PROGRAM AUTOCOMPLETE SNIPPET (used in both modals) -->
    <!-- extracted as an inline template block via v-if on each modal -->

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
      <div class="form-group autocomplete-container">
        <label>Program *</label>
        <input
            v-model="programSearchQuery"
            @input="handleProgramSearch"
            @blur="hideProgramDropdown"
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
      <div class="form-group autocomplete-container">
        <label>Program *</label>
        <input
            v-model="programSearchQuery"
            @input="handleProgramSearch"
            @blur="hideProgramDropdown"
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