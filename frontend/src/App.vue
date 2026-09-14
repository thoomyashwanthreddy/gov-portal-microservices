<template>
  <div id="layout">
    <NavBar v-if="showNav" />
    <main class="content">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import NavBar from '@/components/NavBar.vue'

const route = useRoute()
const showNav = computed(() => !route.meta.public)
</script>

<style>
:root {
  --color-bg: #f4f6f8;
  --color-surface: #ffffff;
  --color-primary: #1d4ed8;
  --color-primary-dark: #1e3a8a;
  --color-text: #1f2937;
  --color-muted: #6b7280;
  --color-border: #e5e7eb;
  --color-success: #15803d;
  --color-warning: #b45309;
  --color-danger: #b91c1c;
  --radius: 8px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
}

* {
  box-sizing: border-box;
}

body {
  margin: 0;
  background: var(--color-bg);
  color: var(--color-text);
}

#layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.content {
  flex: 1;
  max-width: 960px;
  width: 100%;
  margin: 0 auto;
  padding: 24px 20px 48px;
}

button {
  font-family: inherit;
  cursor: pointer;
}

.btn {
  border: none;
  border-radius: var(--radius);
  padding: 10px 18px;
  font-size: 14px;
  font-weight: 600;
  transition: opacity 0.15s ease;
}

.btn:hover {
  opacity: 0.9;
}

.btn-primary {
  background: var(--color-primary);
  color: white;
}

.btn-secondary {
  background: var(--color-surface);
  color: var(--color-text);
  border: 1px solid var(--color-border);
}

input,
select,
textarea {
  font-family: inherit;
  font-size: 14px;
  padding: 9px 12px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
  width: 100%;
  background: var(--color-surface);
  color: var(--color-text);
}

input:focus,
select:focus,
textarea:focus {
  outline: 2px solid var(--color-primary);
  outline-offset: 1px;
}

.card {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
  padding: 20px;
}
</style>
