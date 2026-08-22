<script setup>
import { computed } from 'vue'
import { useStore } from 'vuex'

const store = useStore()
const displayName = computed(() => store.getters['user/displayName'])
const roleLabel = computed(() => store.getters['user/roleLabel'])
</script>

<template>
  <div class="no-access">
    <h1>Nothing to show yet</h1>
    <p>
      You are signed in as <strong>{{ displayName }}</strong>{{ roleLabel ? ` (${roleLabel})` : '' }},
      but that account has no pages assigned to it.
    </p>
    <p class="hint">Ask a registrar to add a role to your account.</p>
  </div>
</template>

<style scoped>
.no-access {
  max-width: 520px;
  padding: 32px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
}

.no-access h1 {
  margin: 0 0 12px 0;
  color: #2c3e50;
  font-size: 22px;
}

.no-access p {
  color: #57606f;
  line-height: 1.6;
  margin: 0 0 8px 0;
}

.hint {
  color: #7f8c8d;
  font-size: 14px;
}
</style>
