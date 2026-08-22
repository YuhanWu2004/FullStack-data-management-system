<script setup>
import { ref, computed, onMounted } from 'vue'
import { useStore } from 'vuex'
import DeleteModal from '../components/DeleteModal.vue'
import EditModal from '../components/EditModal.vue'

const store = useStore()

// ── READ FROM STORE ────────────────────────────
const terms = computed(() => store.getters['term/terms'])
const loading = computed(() => store.getters['term/loading'])
const error = computed(() => store.getters['term/error'])

// ── MODAL STATE ─────────────────────────────────
const showCreateModal = ref(false)
const showEditModal = ref(false)
const showDeleteModal = ref(false)
const selectedTerm = ref(null)
const formError = ref(null)

const form = ref({
  id: null,
  name: '',
  startDate: '',
  endDate: ''
})

// ── VALIDATION ──────────────────────────────────
function validateForm() {
  if (!form.value.name.trim()) {
    formError.value = 'Term name is required'
    return false
  }
  formError.value = null
  return true
}

// ── CREATE ──────────────────────────────────────
function openCreateModal() {
  form.value = { id: null, name: '', startDate: '', endDate: '' }
  formError.value = null
  showCreateModal.value = true
}

async function onCreateSaved() {
  if (!validateForm()) return
  await store.dispatch('term/createTerm', form.value)
  showCreateModal.value = false
}

// ── EDIT ────────────────────────────────────────
function openEditModal(term) {
  form.value = {
    id: term.id,
    name: term.name,
    startDate: term.startDate || '',
    endDate: term.endDate || ''
  }
  selectedTerm.value = term
  formError.value = null
  showEditModal.value = true
}

async function onEditSaved() {
  if (!validateForm()) return
  await store.dispatch('term/updateTerm', form.value)
  showEditModal.value = false
  selectedTerm.value = null
}

// ── DELETE ──────────────────────────────────────
function openDeleteModal(term) {
  selectedTerm.value = term
  showDeleteModal.value = true
}

async function onDeleteConfirmed() {
  await store.dispatch('term/deleteTerm', selectedTerm.value.id)
  showDeleteModal.value = false
  selectedTerm.value = null
}

// ── SET CURRENT ─────────────────────────────────
async function onSetCurrent(term) {
  await store.dispatch('term/setCurrentTerm', term.id)
}

// ── LIFECYCLE ───────────────────────────────────
onMounted(() => {
  store.dispatch('term/fetchTerms')
})
</script>

<template>
  <div class="terms-page">

    <div class="page-header">
      <div>
        <h1>Terms</h1>
        <p class="total-count">Total: {{ terms.length }} terms</p>
      </div>
      <div class="header-actions">
        <button @click="openCreateModal" class="add-btn">
          + Add Term
        </button>
      </div>
    </div>

    <p v-if="error" class="error-message">{{ error }}</p>

    <div v-if="loading" class="loading">Loading terms...</div>

    <div v-else>
      <table>
        <thead>
        <tr>
          <th>ID</th>
          <th>Name</th>
          <th>Start Date</th>
          <th>End Date</th>
          <th>Current</th>
          <th>Actions</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="term in terms" :key="term.id">
          <td>{{ term.id }}</td>
          <td>{{ term.name }}</td>
          <td>{{ term.startDate ?? '—' }}</td>
          <td>{{ term.endDate ?? '—' }}</td>
          <td>
            <span v-if="term.current" class="current-badge">Current</span>
            <button v-else @click="onSetCurrent(term)" class="edit-btn">
              Set Current
            </button>
          </td>
          <td class="actions">
            <button @click="openEditModal(term)" class="edit-btn">Edit</button>
            <button @click="openDeleteModal(term)" class="delete-btn" :disabled="term.current">
              Delete
            </button>
          </td>
        </tr>
        <tr v-if="terms.length === 0">
          <td colspan="6" class="empty-state">No terms found</td>
        </tr>
        </tbody>
      </table>
    </div>

    <!-- CREATE MODAL -->
    <EditModal
        v-if="showCreateModal"
        title="Add Term"
        saveLabel="Create Term"
        :error="formError"
        @save="onCreateSaved"
        @cancel="showCreateModal = false"
    >
      <div class="form-group">
        <label>Term Name *</label>
        <input v-model="form.name" placeholder='e.g. "Fall 2026"' />
      </div>
      <div class="form-group">
        <label>Start Date</label>
        <input v-model="form.startDate" type="date" />
      </div>
      <div class="form-group">
        <label>End Date</label>
        <input v-model="form.endDate" type="date" />
      </div>
    </EditModal>

    <!-- EDIT MODAL -->
    <EditModal
        v-if="showEditModal && selectedTerm"
        title="Edit Term"
        saveLabel="Save Changes"
        :error="formError"
        @save="onEditSaved"
        @cancel="showEditModal = false"
    >
      <div class="form-group">
        <label>Term Name *</label>
        <input v-model="form.name" placeholder='e.g. "Fall 2026"' />
      </div>
      <div class="form-group">
        <label>Start Date</label>
        <input v-model="form.startDate" type="date" />
      </div>
      <div class="form-group">
        <label>End Date</label>
        <input v-model="form.endDate" type="date" />
      </div>
    </EditModal>

    <!-- DELETE MODAL -->
    <DeleteModal
        v-if="showDeleteModal && selectedTerm"
        title="Delete Term"
        :message="`Are you sure you want to delete ${selectedTerm.name}? Any enrollment or assignment still pointing at it will be blocked from deletion by the database until they're moved to a different term.`"
        @confirm="onDeleteConfirmed"
        @cancel="showDeleteModal = false"
    />

  </div>
</template>

<style scoped>
.terms-page { padding: 24px; }

.current-badge {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 12px;
  background-color: #3498db;
  color: white;
  font-size: 12px;
  font-weight: 600;
}
</style>
