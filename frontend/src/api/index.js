/**
 * Base URL for all REST API calls.
 * Reads VITE_API_URL from the environment (set in .env / .env.compose).
 * Falls back to an empty string so relative paths work when the value is unset.
 *
 * @type {string}
 */
export const API_BASE = import.meta.env.VITE_API_URL || ''

/**
 * Base URL for WebSocket (STOMP) connections.
 * Derived from API_BASE by replacing the http(s) scheme with ws(s).
 * Falls back to the current page host so a reverse-proxy setup works out of the box.
 *
 * @type {string}
 */
export const WS_BASE = API_BASE
  ? API_BASE.replace(/^http/, 'ws')
  : `${location.protocol === 'https:' ? 'wss:' : 'ws:'}//${location.host}:8080`

const AUTH_HEADER = 'Basic ' + btoa('admin:password')

/**
 * Fetches all current flight records from the REST API.
 *
 * @returns {Promise<object[]>} Array of flight data objects from the backend.
 * @throws {Error} When the HTTP response is not OK.
 */
export async function fetchFlights() {
  const response = await fetch(`${API_BASE}/api/flights`, {
    headers: { Authorization: AUTH_HEADER }
  })
  if (!response.ok) {
    throw new Error(`Failed to load flights: ${response.statusText}`)
  }
  return response.json()
}

/**
 * Returns the full WebSocket broker URL for STOMP connections.
 *
 * @returns {string} The WebSocket URL including the /ws-native path.
 */
export function getWsBrokerUrl() {
  return `${WS_BASE}/ws-native`
}
