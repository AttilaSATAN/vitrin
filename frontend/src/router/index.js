import { createRouter, createWebHistory } from 'vue-router'
import MapView from '../views/MapView.vue'

/**
 * Application router — maps the root path to the MapView.
 */
const routes = [
  {
    path: '/',
    name: 'map',
    component: MapView
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
