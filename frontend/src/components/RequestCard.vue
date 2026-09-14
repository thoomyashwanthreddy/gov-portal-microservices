<template>
  <article class="card request-card">
    <div class="request-header">
      <div>
        <h3>{{ request.category }}</h3>
        <p class="meta">
          Submitted by <strong>{{ request.citizenUsername }}</strong>
          &middot; {{ formattedDate }}
          <template v-if="request.location"> &middot; {{ request.location }}</template>
        </p>
      </div>
      <span class="status-badge" :class="statusClass">{{ formattedStatus }}</span>
    </div>

    <p class="description">{{ request.description }}</p>

    <div v-if="isCaseWorker" class="actions">
      <label class="field-inline">
        <span>Update status</span>
        <select v-model="selectedStatus" :disabled="updating">
          <option v-for="status in statuses" :key="status" :value="status">
            {{ formatStatus(status) }}
          </option>
        </select>
      </label>
      <button
        class="btn btn-primary"
        :disabled="updating || selectedStatus === request.status"
        @click="applyUpdate"
      >
        {{ updating ? 'Updating…' : 'Update' }}
      </button>
    </div>
  </article>
</template>

<script setup>
import { computed, ref } from 'vue'

const props = defineProps({
  request: { type: Object, required: true },
  isCaseWorker: { type: Boolean, default: false }
})

const emit = defineEmits(['update-status'])

const statuses = ['SUBMITTED', 'IN_REVIEW', 'APPROVED', 'REJECTED', 'COMPLETED']
const selectedStatus = ref(props.request.status)
const updating = ref(false)

const formattedDate = computed(() =>
  new Date(props.request.createdAt).toLocaleDateString(undefined, {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  })
)

const formattedStatus = computed(() => formatStatus(props.request.status))

const statusClass = computed(() => `status-${props.request.status.toLowerCase()}`)

function formatStatus(status) {
  return status
    .toLowerCase()
    .split('_')
    .map((word) => word[0].toUpperCase() + word.slice(1))
    .join(' ')
}

async function applyUpdate() {
  updating.value = true
  try {
    await emit('update-status', { id: props.request.id, status: selectedStatus.value })
  } finally {
    updating.value = false
  }
}
</script>

<style scoped>
.request-card {
  margin-bottom: 14px;
}

.request-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

h3 {
  margin: 0 0 4px;
  font-size: 16px;
}

.meta {
  margin: 0;
  font-size: 12px;
  color: var(--color-muted);
}

.description {
  margin: 12px 0 0;
  font-size: 14px;
  line-height: 1.5;
}

.status-badge {
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.03em;
  padding: 4px 10px;
  border-radius: 999px;
  white-space: nowrap;
}

.status-submitted {
  background: #e0e7ff;
  color: #3730a3;
}
.status-in_review {
  background: #fef3c7;
  color: var(--color-warning);
}
.status-approved {
  background: #dcfce7;
  color: var(--color-success);
}
.status-rejected {
  background: #fee2e2;
  color: var(--color-danger);
}
.status-completed {
  background: #e5e7eb;
  color: #374151;
}

.actions {
  margin-top: 16px;
  display: flex;
  align-items: flex-end;
  gap: 12px;
  border-top: 1px solid var(--color-border);
  padding-top: 14px;
}

.field-inline {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 12px;
  font-weight: 600;
  color: var(--color-muted);
}
</style>
