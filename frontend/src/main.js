import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import { useAuthStore } from './stores/auth'

const app = createApp(App)

app.use(createPinia())
app.use(router)

// Rehydrate the session (if a token was persisted) before the first route resolves.
const authStore = useAuthStore()
authStore.hydrateFromStorage()

app.mount('#app')
