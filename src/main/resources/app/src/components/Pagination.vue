<script setup>
// ── PROPS — data passed IN from parent ────────
const props = defineProps({
  currentPage: {
    type: Number,
    required: true
  },
  totalPages: {
    type: Number,
    required: true
  },
  totalItems: {
    type: Number,
    required: true
  },
  pageSize: {
    type: Number,
    required: true
  }
})

// ── EMITS — events sent OUT to parent ─────────
const emit = defineEmits(['page-changed'])

// ── FUNCTIONS ─────────────────────────────────
function goToPage(page) {
  if (page >= 1 && page <= props.totalPages) {
    emit('page-changed', page)   // ← tell parent which page was clicked
  }
}

// showing X - Y of Z
function showingFrom() {
  return props.totalItems === 0
      ? 0
      : (props.currentPage - 1) * props.pageSize + 1
}

function showingTo() {
  return Math.min(props.currentPage * props.pageSize, props.totalItems)
}
</script>

<template>
  <div class="pagination" v-if="totalPages > 1">

    <!-- previous button -->
    <button
        @click="goToPage(currentPage - 1)"
        :disabled="currentPage === 1"
        class="page-btn">
      ← Previous
    </button>

    <!-- page number buttons -->
    <button
        v-for="page in totalPages"
        :key="page"
        :class="['page-btn', { active: page === currentPage }]"
        @click="goToPage(page)">
      {{ page }}
    </button>

    <!-- next button -->
    <button
        @click="goToPage(currentPage + 1)"
        :disabled="currentPage === totalPages"
        class="page-btn">
      Next →
    </button>

    <!-- showing info -->
    <span class="page-info">
      Showing {{ showingFrom() }} - {{ showingTo() }}
      of {{ totalItems }}
    </span>

  </div>
</template>

<style scoped>
.pagination {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-top: 16px;
  flex-wrap: wrap;
}

.page-btn {
  padding: 6px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  background: white;
  font-size: 14px;
}

.page-btn:hover:not(:disabled) {
  background-color: #f0f0f0;
}

.page-btn.active {
  background-color: #3498db;
  color: white;
  border-color: #3498db;
}

.page-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.page-info {
  color: #7f8c8d;
  font-size: 14px;
  margin-left: 8px;
}
</style>