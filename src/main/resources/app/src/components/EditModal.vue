<script setup>
defineProps({
  title: {
    type: String,
    default: 'Edit'
  },
  error: {
    type: String,
    default: null
  }
})

defineEmits(["save", 'cancel'])

</script>


<template>
  <div class="modal-overlay" @click.self="emit('cancel')">
    <div class="modal>">
      <h2>{{title}}</h2>
      <p>{{message}}</p>
      <p class="warning">{{warning}}</p>
    </div>
    <div class="modal-actions" >
      <button @click="emit('cancel')" class="cancel-btn">Cancel</button>
      <button @click="emit('confirm')" class="delete-btn">Delete</button>
    </div>
  </div>
</template>