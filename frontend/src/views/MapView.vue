<template>
  <div class="page">
    <!-- Sidebar -->
    <aside class="sidebar">
      <div class="sidebar-header">
        <h1 class="logo">✈ Flight Tracker</h1>
        <span :class="['status-badge', isConnected ? 'connected' : 'disconnected']">
          {{ isConnected ? '● Live' : '○ Offline' }}
        </span>
      </div>

      <ul class="flight-list">
        <li v-if="flights.length === 0" class="empty-msg">
          No flight data yet.
        </li>
        <li
          v-for="flight in flights"
          :key="flight.flightNumber"
          class="flight-card"
          @click="focusFlight(flight)"
        >
          <div class="fn">{{ flight.flightNumber }}</div>
          <div class="details">
            <span>{{ formatCoords(flight) }}</span>
            <span>{{ flight.airSpeed }} kts</span>
          </div>
        </li>
      </ul>
    </aside>

    <!-- Map -->
    <div ref="mapEl" class="map-container">
      <!-- Spawn producer button — bottom-left corner of the map -->
      <div class="spawn-wrap">
        <button
          class="spawn-btn"
          :class="{ 'spawn-btn--loading': spawning }"
          :disabled="spawning"
          @click="handleSpawnProducer"
        >
          {{ spawning ? 'Spawning…' : '▶ Spawn Producer' }}
        </button>
        <transition name="fade">
          <span v-if="spawnMessage" :class="['spawn-msg', spawnMsgType]">
            {{ spawnMessage }}
          </span>
        </transition>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { Map as MaplibreMap, Marker, Popup } from 'maplibre-gl'
import { Client } from '@stomp/stompjs'
import { fetchFlights, getWsBrokerUrl, spawnProducer } from '../api'

// ── Refs 
const mapEl = ref(null)
const isConnected = ref(false)
const flights = ref([]) // Array for storing flight data objects from the API and WebSocket
const spawning = ref(false)
const spawnMessage = ref('')
const spawnMsgType = ref('success')

// ── Internals 
/** @type {MaplibreMap|null} */
let map = null
/** @type {Client|null} */
let stompClient = null
/** @type {Map<string, Marker>} Flight-number → MapLibre Marker */
const flightMarkers = new Map()

/** @type {Map<string, Marker[]>} Flight-number → list of trail dot Markers */
const trailMarkers = new Map()

// ── Map initialisation 

/**
 * Initialises the MapLibre map using OpenStreetMap raster tiles,
 * centred on Turkey (lon: 35.25, lat: 38.96, zoom: 5.5).
 */
function initMap() {
  map = new MaplibreMap({
    container: mapEl.value,
    style: {
      version: 8,
      sources: {
        osm: {
          type: 'raster',
          tiles: ['https://tile.openstreetmap.org/{z}/{x}/{y}.png'],
          tileSize: 256,
          attribution:
            '© <a href="https://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a> contributors'
        }
      },
      layers: [{ id: 'osm-layer', type: 'raster', source: 'osm' }]
    },
    center: [35.25, 38.96],
    zoom: 5.5
  })
}

// ── REST data loading 

/**
 * Fetches existing flight records from the REST API and renders them on the map.
 */
async function loadFlights() {
  try {
    const data = await fetchFlights()
    data.forEach(updateFlight)
  } catch (err) {
    console.error('Error loading flights:', err)
  }
}

// ── Flight data helpers 

/**
 * Adds or updates a flight entry in the flights list and its map marker.
 *
 * @param {object} flightData - Flight data object from the API or WebSocket
 */
function updateFlight(flightData) {
  const idx = flights.value.findIndex(
    (f) => f.flightNumber === flightData.flightNumber
  )
  if (idx >= 0) {
    flights.value[idx] = flightData
  } else {
    flights.value.push(flightData)
  }
  updateMarker(flightData)
}

/**
 * Places a small blue dot at the given coordinates to mark a previous flight position.
 *
 * @param {string} flightNumber - The flight number the trail belongs to
 * @param {number} lng - Longitude of the previous position
 * @param {number} lat - Latitude of the previous position
 */
function addTrailDot(flightNumber, lng, lat) {
  const el = document.createElement('div')
  el.style.cssText =
    'width:8px;height:8px;border-radius:50%;background:#2979ff;opacity:0.7;border:1px solid #fff;pointer-events:none;'
  const dot = new Marker({ element: el })
    .setLngLat([lng, lat])
    .addTo(map)
  if (!trailMarkers.has(flightNumber)) {
    trailMarkers.set(flightNumber, [])
  }
  trailMarkers.get(flightNumber).push(dot)
}

/**
 * Creates or repositions a MapLibre Marker for the given flight.
 * When an existing marker is moved, a trail dot is placed at the previous position.
 *
 * @param {object} flight - The flight data containing longitude, latitude and flightNumber
 */
function updateMarker(flight) {
  const key = flight.flightNumber
  if (flightMarkers.has(key)) {
    const existing = flightMarkers.get(key)
    const prev = existing.getLngLat()
    addTrailDot(key, prev.lng, prev.lat)
    existing.setLngLat([flight.longitude, flight.latitude])
  } else {
    const popup = new Popup({ offset: 25 }).setHTML(
      `<strong>${flight.flightNumber}</strong><br>
       Speed: ${flight.airSpeed} kts<br>
       Lat: ${flight.latitude?.toFixed(4)}<br>
       Lng: ${flight.longitude?.toFixed(4)}`
    )
    const marker = new Marker({ color: '#e53935' })
      .setLngLat([flight.longitude, flight.latitude])
      .setPopup(popup)
      .addTo(map)
    flightMarkers.set(key, marker)
  }
}

/**
 * Pans the map to centre on the selected flight and opens its popup.
 *
 * @param {object} flight - The flight to focus on
 */
function focusFlight(flight) {
  map.flyTo({ center: [flight.longitude, flight.latitude], zoom: 8, duration: 1000 })
  flightMarkers.get(flight.flightNumber)?.togglePopup()
}

/**
 * Formats a flight's coordinates as a human-readable string.
 *
 * @param {object} flight - The flight data object
 * @returns {string} Formatted coordinates string
 */
function formatCoords(flight) {
  return `${flight.latitude?.toFixed(3)}°N, ${flight.longitude?.toFixed(3)}°E`
}

// ── WebSocket 

/**
 * Initialises and activates the STOMP WebSocket client.
 * Subscribes to /topic/flights for real-time flight updates from the backend.
 */
function initWebSocket() {
  stompClient = new Client({
    brokerURL: getWsBrokerUrl(),
    reconnectDelay: 5000,
    onConnect: () => {
      isConnected.value = true
      stompClient.subscribe('/topic/flights', (message) => {
        const flightData = JSON.parse(message.body)
        updateFlight(flightData)
      })
    },
    onDisconnect: () => {
      isConnected.value = false
    },
    onStompError: (frame) => {
      console.error('STOMP error:', frame)
      isConnected.value = false
    }
  })
  stompClient.activate()
}

// ── Spawn producer 

/**
 * Calls the backend spawn endpoint and shows a transient status message.
 */
async function handleSpawnProducer() {
  spawning.value = true
  spawnMessage.value = ''
  try {
    const result = await spawnProducer()
    if (result.status === 'CREATED') {
      spawnMsgType.value = 'success'
      spawnMessage.value = `Pod created: ${result.podName}`
    } else {
      spawnMsgType.value = 'error'
      spawnMessage.value = result.message
    }
  } catch (err) {
    spawnMsgType.value = 'error'
    spawnMessage.value = 'Request failed'
  } finally {
    spawning.value = false
    setTimeout(() => { spawnMessage.value = '' }, 4000)
  }
}

// ── Lifecycle 

onMounted(() => {
  initMap()
  map.on('load', loadFlights)
  initWebSocket()
})

onUnmounted(() => {
  stompClient?.deactivate()
  map?.remove()
})
</script>

<style scoped>
.page {
  display: flex;
  height: 100vh;
  width: 100vw;
  overflow: hidden;
}

/* ── Sidebar ── */
.sidebar {
  width: 280px;
  flex-shrink: 0;
  background: #1a1a2e;
  color: #eee;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.sidebar-header {
  padding: 14px 16px;
  background: #16213e;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  border-bottom: 1px solid #0f3460;
}

.logo {
  font-size: 0.95rem;
  font-weight: 700;
  letter-spacing: 0.5px;
  white-space: nowrap;
}

.status-badge {
  font-size: 0.7rem;
  padding: 2px 8px;
  border-radius: 12px;
  white-space: nowrap;
  font-weight: 600;
}

.status-badge.connected {
  background: #1b5e20;
  color: #a5d6a7;
}

.status-badge.disconnected {
  background: #4a1212;
  color: #ef9a9a;
}

/* ── Flight list ── */
.flight-list {
  flex: 1;
  overflow-y: auto;
  list-style: none;
  padding: 8px;
}

.flight-list::-webkit-scrollbar {
  width: 4px;
}

.flight-list::-webkit-scrollbar-track {
  background: transparent;
}

.flight-list::-webkit-scrollbar-thumb {
  background: #0f3460;
  border-radius: 4px;
}

.empty-msg {
  text-align: center;
  color: #555;
  padding: 32px 16px;
  font-size: 0.85rem;
}

.flight-card {
  background: #0f3460;
  border-radius: 6px;
  padding: 10px 12px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: background 0.15s;
  border: 1px solid transparent;
}

.flight-card:hover {
  background: #1a4a7a;
  border-color: #2979ff;
}

.fn {
  font-weight: 700;
  font-size: 1rem;
  margin-bottom: 4px;
  color: #90caf9;
}

.details {
  display: flex;
  justify-content: space-between;
  font-size: 0.72rem;
  color: #aaa;
}

/* ── Map ── */
.map-container {
  flex: 1;
  min-width: 0;
  position: relative;
}

/* ── Spawn button ── */
.spawn-wrap {
  position: absolute;
  bottom: 24px;
  left: 16px;
  z-index: 10;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 6px;
}

.spawn-btn {
  background: #1565c0;
  color: #fff;
  border: none;
  border-radius: 6px;
  padding: 9px 18px;
  font-size: 0.85rem;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0,0,0,0.35);
  transition: background 0.15s, opacity 0.15s;
}

.spawn-btn:hover:not(:disabled) {
  background: #1976d2;
}

.spawn-btn--loading,
.spawn-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.spawn-msg {
  font-size: 0.78rem;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 4px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.3);
}

.spawn-msg.success {
  background: #1b5e20;
  color: #a5d6a7;
}

.spawn-msg.error {
  background: #4a1212;
  color: #ef9a9a;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
