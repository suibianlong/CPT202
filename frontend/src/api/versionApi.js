/**
 * Version API - Handles version history, comparison and rollback
 */
import axios from 'axios'

const API_BASE = '/api/resources'

/**
 * Get version history for a resource
 * GET /api/resources/:id/versions
 */
export async function getVersionHistory(resourceId) {
  const response = await axios.get(`${API_BASE}/${resourceId}/versions`)
  return response.data
}

/**
 * Get detailed version data
 * GET /api/resources/:id/versions/:versionNo
 */
export async function getVersionDetail(resourceId, versionNo) {
  const response = await axios.get(`${API_BASE}/${resourceId}/versions/${versionNo}`)
  return response.data
}

/**
 * Compare two versions
 * GET /api/resources/:id/compare?v1=X&v2=Y
 */
export async function compareVersions(resourceId, version1, version2) {
  const response = await axios.get(`${API_BASE}/${resourceId}/compare`, {
    params: { v1: version1, v2: version2 }
  })
  return response.data
}

/**
 * Rollback to a specific version
 * POST /api/resources/:id/rollback
 */
export async function rollbackVersion(resourceId, versionNo, selectedValues = null) {
  const response = await axios.post(`${API_BASE}/${resourceId}/rollback`, {
    versionNo,
    selectedValues
  })
  return response.data
}

/**
 * Get version conflict information
 * GET /api/resources/:id/versions/:versionNo/conflict
 */
export async function getVersionConflict(resourceId, versionNo) {
  const response = await axios.get(`${API_BASE}/${resourceId}/versions/${versionNo}/conflict`)
  return response.data
}

/**
 * Get all submissions for a resource
 * GET /api/resources/:id/submissions
 */
export async function getResourceSubmissions(resourceId) {
  const response = await axios.get(`${API_BASE}/${resourceId}/submissions`)
  return response.data
}

/**
 * Get review records for a version
 * GET /api/resources/:id/versions/:versionNo/reviews
 */
export async function getVersionReviews(resourceId, versionNo) {
  const response = await axios.get(`${API_BASE}/${resourceId}/versions/${versionNo}/reviews`)
  return response.data
}

export default {
  getVersionHistory,
  getVersionDetail,
  compareVersions,
  rollbackVersion,
  getVersionConflict,
  getResourceSubmissions,
  getVersionReviews
}
