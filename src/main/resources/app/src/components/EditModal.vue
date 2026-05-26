<script setup>
defineProps({
  title: {
    type: String,
    default: 'Edit'
  },
  error: {
    type: String,
    default: null
  },
  saveLabel: { type: String, default: 'Save Changes' }
})

const emit = defineEmits(['save', 'cancel'])
</script>

<template>
  <div class="modal-overlay" @click.self="emit('cancel')">
    <div class="modal">
      <h2>{{ title }}</h2>

      <!-- SLOT — parent injects its own form fields here -->
      <slot />
      <!--  ↑
        Each parent puts its own fields here
        EditModal doesn't know or care what fields exist
        it just provides the shell
      -->

      <p v-if="error" class="error">{{ error }}</p>

      <div class="modal-actions">
        <button @click="emit('cancel')" class="cancel-btn">
          Cancel
        </button>
        <button @click="emit('save')" class="save-btn">
          {{ saveLabel}}
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}

.modal {
  background: white;
  padding: 32px;
  border-radius: 12px;
  width: 480px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
}

.modal h2 {
  margin: 0;
  color: #2c3e50;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 8px;
}

.cancel-btn {
  padding: 10px 20px;
  background: #ecf0f1;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
}

.cancel-btn:hover {
  background-color: #dfe6e9;
}

.save-btn {
  padding: 10px 20px;
  background-color: #3498db;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
}

.save-btn:hover {
  background-color: #2980b9;
}

.error {
  color: #e74c3c;
  font-size: 14px;
  margin: 0;
}
</style>