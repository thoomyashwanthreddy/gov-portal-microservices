<template>
  <div>
    <h1>Submit a Service Request</h1>

    <form class="card form" @submit.prevent="handleSubmit">
      <label class="field">
        <span>Category</span>
        <select v-model="category" required>
          <option value="" disabled>Select a category</option>
          <option>Pothole</option>
          <option>Streetlight Outage</option>
          <option>Trash / Recycling</option>
          <option>Permit Renewal</option>
          <option>Noise Complaint</option>
          <option>Other</option>
        </select>
      </label>

      <label class="field">
        <span>Location</span>
        <input v-model="location" type="text" placeholder="e.g. 500 Main St" />
      </label>

      <label class="field">
        <span>Description</span>
        <textarea v-model="description" rows="5" required maxlength="2000"></textarea>
      </label>

      <p v-if="error" class="error">{{ error }}</p>

      <div class="form-actions">
        <button class="btn btn-secondary" type="button" @click="router.push('/')">Cancel</button>
        <button class="btn btn-primary" type="submit" :disabled="submitting">
          {{ submitting ? 'Submitting…' : 'Submit Request' }}
        </button>
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useRequestsStore } from '@/stores/requests'

const category = ref('')
const location = ref('')
const description = ref('')
const submitting = ref(false)
const error = ref('')

const requestsStore = useRequestsStore()
const router = useRouter()

async function handleSubmit() {
  submitting.value = true
  error.value = ''
  try {
    await requestsStore.submitRequest({
      category: category.value,
      description: description.value,
      location: location.value
    })
    router.push('/')
  } catch (err) {
    error.value = err.response?.data?.message || 'Could not submit the request. Please try again.'
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
h1 {
  font-size: 20px;
  margin: 0 0 20px;
}

.form {
  display: flex;
  flex-direction: column;
  gap: 16px;
  max-width: 480px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.error {
  color: var(--color-danger);
  font-size: 13px;
  margin: 0;
}
</style>
