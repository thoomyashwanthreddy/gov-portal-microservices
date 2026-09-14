<template>
  <div>
    <div class="page-header">
      <h1>{{ authStore.isCaseWorker ? 'All Service Requests' : 'My Service Requests' }}</h1>
      <router-link v-if="authStore.isCitizen" to="/submit" class="btn btn-primary">
        + New Request
      </router-link>
    </div>

    <p v-if="requestsStore.loading" class="status-text">Loading…</p>
    <p v-else-if="requestsStore.error" class="status-text error">{{ requestsStore.error }}</p>
    <p v-else-if="requestsStore.requests.length === 0" class="status-text">
      No service requests yet.
    </p>

    <RequestCard
      v-for="request in requestsStore.requests"
      :key="request.id"
      :request="request"
      :is-case-worker="authStore.isCaseWorker"
      @update-status="handleUpdateStatus"
    />
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useRequestsStore } from '@/stores/requests'
import RequestCard from '@/components/RequestCard.vue'

const authStore = useAuthStore()
const requestsStore = useRequestsStore()

onMounted(() => {
  requestsStore.fetchRequests()
})

async function handleUpdateStatus({ id, status }) {
  await requestsStore.updateStatus(id, status)
}
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

h1 {
  font-size: 20px;
  margin: 0;
}

.status-text {
  color: var(--color-muted);
  font-size: 14px;
}

.status-text.error {
  color: var(--color-danger);
}
</style>
