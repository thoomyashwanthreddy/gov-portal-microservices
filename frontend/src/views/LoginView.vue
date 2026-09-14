<template>
  <div class="login-screen">
    <form class="card login-card" @submit.prevent="handleSubmit">
      <h1>Gov Portal</h1>
      <p class="subtitle">Sign in to submit or manage service requests</p>

      <label class="field">
        <span>Username</span>
        <input v-model="username" type="text" autocomplete="username" required />
      </label>

      <label class="field">
        <span>Password</span>
        <input v-model="password" type="password" autocomplete="current-password" required />
      </label>

      <p v-if="error" class="error">{{ error }}</p>

      <button class="btn btn-primary" type="submit" :disabled="loading">
        {{ loading ? 'Signing in…' : 'Sign in' }}
      </button>

      <p class="hint">
        Try <code>jane.citizen</code> / <code>citizen123</code> (citizen) or
        <code>worker.bob</code> / <code>worker123</code> (case worker) — seeded by the
        Keycloak realm import.
      </p>
    </form>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const username = ref('')
const password = ref('')
const loading = ref(false)
const error = ref('')

const authStore = useAuthStore()
const router = useRouter()
const route = useRoute()

async function handleSubmit() {
  loading.value = true
  error.value = ''
  try {
    await authStore.login(username.value, password.value)
    router.push(route.query.redirect || '/')
  } catch (err) {
    error.value = err.response?.data?.message || 'Invalid username or password.'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-screen {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-bg);
}

.login-card {
  width: 360px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

h1 {
  margin: 0;
  font-size: 22px;
  color: var(--color-primary-dark);
}

.subtitle {
  margin: 0 0 6px;
  color: var(--color-muted);
  font-size: 14px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text);
}

.error {
  color: var(--color-danger);
  font-size: 13px;
  margin: 0;
}

.hint {
  margin-top: 10px;
  font-size: 12px;
  color: var(--color-muted);
  line-height: 1.5;
}
</style>
