<template>
  <header class="navbar">
    <div class="navbar-inner">
      <router-link to="/" class="brand">Gov Portal</router-link>
      <nav class="links">
        <router-link to="/" class="link">My Requests</router-link>
        <router-link v-if="authStore.isCitizen" to="/submit" class="link">New Request</router-link>
      </nav>
      <div class="user">
        <span class="username">{{ authStore.user?.username }}</span>
        <span class="role-badge" :class="authStore.isCaseWorker ? 'role-worker' : 'role-citizen'">
          {{ authStore.isCaseWorker ? 'Case Worker' : 'Citizen' }}
        </span>
        <button class="btn btn-secondary" @click="handleLogout">Log out</button>
      </div>
    </div>
  </header>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const router = useRouter()

function handleLogout() {
  authStore.logout()
  router.push({ name: 'login' })
}
</script>

<style scoped>
.navbar {
  background: var(--color-surface);
  border-bottom: 1px solid var(--color-border);
}

.navbar-inner {
  max-width: 960px;
  margin: 0 auto;
  padding: 14px 20px;
  display: flex;
  align-items: center;
  gap: 24px;
}

.brand {
  font-weight: 700;
  font-size: 18px;
  color: var(--color-primary-dark);
  text-decoration: none;
}

.links {
  display: flex;
  gap: 16px;
  flex: 1;
}

.link {
  color: var(--color-muted);
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
}

.link.router-link-exact-active {
  color: var(--color-primary);
}

.user {
  display: flex;
  align-items: center;
  gap: 10px;
}

.username {
  font-size: 14px;
  font-weight: 600;
}

.role-badge {
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.03em;
  padding: 3px 8px;
  border-radius: 999px;
}

.role-citizen {
  background: #dbeafe;
  color: #1d4ed8;
}

.role-worker {
  background: #dcfce7;
  color: #15803d;
}
</style>
