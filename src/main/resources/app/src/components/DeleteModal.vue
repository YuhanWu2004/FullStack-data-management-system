<script setup>
defineProps({
  title: {
    type: String,
    default: 'Delete Item'
  },
  message: {
    type: String,
    required: true
  },
  warning: {
    type: String,
    default: 'This action cannot be undone'
  }
})

const emit = defineEmits(['confirm', 'cancel'])  // ← destructure emit
</script>

<template>
  <div class="modal-overlay" @click.self="emit('cancel')">  <!-- ← fixed class -->
    <div class="modal">                                      <!-- ← fixed class -->
      <div class="icon">⚠️</div>
      <h2>{{ title }}</h2>
      <p class="message">{{ message }}</p>
      <p class="warning">{{ warning }}</p>

      <div class="modal-actions">                            <!-- ← moved inside modal -->
        <button @click="emit('cancel')" class="cancel-btn">Cancel</button>
        <button @click="emit('confirm')" class="delete-btn">Delete</button>
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
  width: 400px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
  text-align: center;
}

.icon {
  font-size: 48px;
}

h2 {
  margin: 0;
  color: #2c3e50;
  font-size: 20px;
}

.message {
  margin: 0;
  color: #2c3e50;
  font-size: 16px;
}

.warning {
  margin: 0;
  color: #e74c3c;
  font-size: 14px;
}

.modal-actions {
  display: flex;
  gap: 12px;
  margin-top: 8px;
  width: 100%;
}

.cancel-btn {
  flex: 1;
  padding: 12px;
  background: #ecf0f1;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  color: #2c3e50;
}

.cancel-btn:hover {
  background-color: #dfe6e9;
}

.delete-btn {
  flex: 1;
  padding: 12px;
  background-color: #e74c3c;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
}

.delete-btn:hover {
  background-color: #c0392b;
}
</style>