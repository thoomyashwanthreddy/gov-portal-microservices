import { defineStore } from 'pinia'
import apiClient from '@/api/axios'

export const useRequestsStore = defineStore('requests', {
  state: () => ({
    requests: [],
    loading: false,
    error: null
  }),

  actions: {
    async fetchRequests() {
      this.loading = true
      this.error = null
      try {
        const response = await apiClient.get('/requests')
        this.requests = response.data
      } catch (err) {
        this.error = err.response?.data?.message || 'Failed to load service requests.'
      } finally {
        this.loading = false
      }
    },

    async submitRequest({ category, description, location }) {
      const response = await apiClient.post('/requests', { category, description, location })
      this.requests.unshift(response.data)
      return response.data
    },

    async updateStatus(id, status) {
      const response = await apiClient.patch(`/requests/${id}`, { status })
      const index = this.requests.findIndex((r) => r.id === id)
      if (index !== -1) {
        this.requests[index] = response.data
      }
      return response.data
    }
  }
})
