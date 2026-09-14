import { defineStore } from 'pinia'
import apiClient from '@/api/axios'

const TOKEN_KEY = 'gov_portal_access_token'
const USER_KEY = 'gov_portal_user'

/**
 * Decodes the payload of a JWT without verifying its signature. Verification is the
 * backend's job (it validates every token against Keycloak's JWK set) — the frontend
 * only needs the claims to decide what UI to show, never to make an access decision.
 */
function decodeJwtPayload(token) {
  const payload = token.split('.')[1]
  const base64 = payload.replace(/-/g, '+').replace(/_/g, '/')
  const json = decodeURIComponent(
    atob(base64)
      .split('')
      .map((c) => '%' + c.charCodeAt(0).toString(16).padStart(2, '0'))
      .join('')
  )
  return JSON.parse(json)
}

function userFromToken(token) {
  const claims = decodeJwtPayload(token)
  const roles = claims.realm_access?.roles ?? []
  return {
    username: claims.preferred_username,
    roles,
    isCitizen: roles.includes('citizen'),
    isCaseWorker: roles.includes('case-worker')
  }
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    accessToken: null,
    user: null
  }),

  getters: {
    isAuthenticated: (state) => !!state.accessToken,
    isCaseWorker: (state) => !!state.user?.isCaseWorker,
    isCitizen: (state) => !!state.user?.isCitizen
  },

  actions: {
    async login(username, password) {
      const response = await apiClient.post('/auth/login', { username, password })
      const { access_token: accessToken } = response.data
      this.setSession(accessToken)
    },

    logout() {
      this.accessToken = null
      this.user = null
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(USER_KEY)
    },

    setSession(accessToken) {
      this.accessToken = accessToken
      this.user = userFromToken(accessToken)
      localStorage.setItem(TOKEN_KEY, accessToken)
      localStorage.setItem(USER_KEY, JSON.stringify(this.user))
    },

    hydrateFromStorage() {
      const token = localStorage.getItem(TOKEN_KEY)
      if (!token) return
      try {
        this.setSession(token)
      } catch {
        this.logout()
      }
    }
  }
})
