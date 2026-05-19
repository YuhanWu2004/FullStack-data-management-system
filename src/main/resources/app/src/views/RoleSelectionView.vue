<script setup>
import { RouterLink } from 'vue-router'
</script>

<template>
  <div>
    <h1>Student Management System</h1>
    <p>Select a section to get started:</p>

    <div>
      <RouterLink to="/students">
        <button>Students</button>
      </RouterLink>
      <RouterLink to="/courses">
        <button>Courses</button>
      </RouterLink>
      <RouterLink to="/professors">
        <button>Professors</button>
      </RouterLink>
    </div>
  </div>
</template>