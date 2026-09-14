import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useRequestsStore } from '@/stores/requests'
import apiClient from '@/api/axios'

vi.mock('@/api/axios', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    patch: vi.fn()
  }
}))

describe('requests store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('fetchRequests populates requests on success', async () => {
    const mockData = [{ id: '1', category: 'Pothole', status: 'SUBMITTED' }]
    apiClient.get.mockResolvedValueOnce({ data: mockData })

    const store = useRequestsStore()
    await store.fetchRequests()

    expect(store.requests).toEqual(mockData)
    expect(store.loading).toBe(false)
    expect(store.error).toBeNull()
  })

  it('fetchRequests sets an error message on failure', async () => {
    apiClient.get.mockRejectedValueOnce({ response: { data: { message: 'boom' } } })

    const store = useRequestsStore()
    await store.fetchRequests()

    expect(store.error).toBe('boom')
    expect(store.requests).toEqual([])
  })

  it('submitRequest prepends the new request to the list', async () => {
    const created = { id: '2', category: 'Streetlight Outage', status: 'SUBMITTED' }
    apiClient.post.mockResolvedValueOnce({ data: created })

    const store = useRequestsStore()
    store.requests = [{ id: '1', category: 'Pothole', status: 'SUBMITTED' }]
    await store.submitRequest({ category: 'Streetlight Outage', description: 'desc', location: '' })

    expect(store.requests[0]).toEqual(created)
    expect(store.requests).toHaveLength(2)
  })

  it('updateStatus replaces the matching request in place', async () => {
    const updated = { id: '1', category: 'Pothole', status: 'APPROVED' }
    apiClient.patch.mockResolvedValueOnce({ data: updated })

    const store = useRequestsStore()
    store.requests = [{ id: '1', category: 'Pothole', status: 'SUBMITTED' }]
    await store.updateStatus('1', 'APPROVED')

    expect(store.requests[0]).toEqual(updated)
  })
})
