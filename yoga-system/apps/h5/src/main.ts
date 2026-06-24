import { createVueApp } from '@dcloudio/uni-app'
import { createPinia } from 'pinia'
import App from './App.vue'

export function createApp() {
  const app = createVueApp(App)
  app.use(createPinia())
  return { app }
}
