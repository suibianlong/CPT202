/**
 * Contributor Module - State Management
 * Integrates with backend API - Updated for new database schema
 */

import axios from 'axios'

const API_BASE = '/api/resources'

const api = axios.create({
  baseURL: '',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

const contributorStore = {
  namespaced: true,

  state: {
    resources: [],
    currentResource: null,
    categories: [],
    tags: [],
    filterStatus: '',
    loading: false,
    error: null
  },

  mutations: {
    SET_RESOURCES(state, resources) {
      state.resources = resources
    },
    SET_CURRENT_RESOURCE(state, resource) {
      state.currentResource = resource
    },
    SET_CATEGORIES(state, categories) {
      state.categories = categories
    },
    SET_TAGS(state, tags) {
      state.tags = tags
    },
    SET_FILTER_STATUS(state, status) {
      state.filterStatus = status
    },
    SET_LOADING(state, loading) {
      state.loading = loading
    },
    SET_ERROR(state, error) {
      state.error = error
    },
    UPDATE_RESOURCE_STATUS(state, { id, status }) {
      const resource = state.resources.find(r => r.resource_id === id || r.id === id)
      if (resource) {
        resource.status = status
        resource.last_updated_time = new Date().toISOString()
      }
    },
    REMOVE_RESOURCE(state, id) {
      state.resources = state.resources.filter(r => r.resource_id !== id && r.id !== id)
    }
  },

  actions: {
    /**
     * Fetch categories list
     * GET /api/resources/categories
     */
    async fetchCategories({ commit }) {
      commit('SET_LOADING', true)
      commit('SET_ERROR', null)

      try {
        const response = await api.get(`${API_BASE}/categories`)
        commit('SET_CATEGORIES', response.data || [])
      } catch (error) {
        const message = error.response?.data?.error || 'Failed to fetch categories'
        commit('SET_ERROR', message)
        console.error('fetchCategories error:', error)
      } finally {
        commit('SET_LOADING', false)
      }
    },

    /**
     * Fetch tags list
     * GET /api/resources/tags
     */
    async fetchTags({ commit }) {
      commit('SET_LOADING', true)
      commit('SET_ERROR', null)

      try {
        const response = await api.get(`${API_BASE}/tags`)
        commit('SET_TAGS', response.data || [])
      } catch (error) {
        const message = error.response?.data?.error || 'Failed to fetch tags'
        commit('SET_ERROR', message)
        console.error('fetchTags error:', error)
      } finally {
        commit('SET_LOADING', false)
      }
    },

    /**
     * Fetch submissions list
     * GET /api/resources/my
     */
    async fetchResources({ commit }, filterStatus = '') {
      commit('SET_LOADING', true)
      commit('SET_ERROR', null)

      try {
        const params = {}
        if (filterStatus) {
          params.status = filterStatus
        }

        const response = await api.get(`${API_BASE}/my`, { params })
        commit('SET_RESOURCES', response.data || [])

      } catch (error) {
        const message = error.response?.data?.error || 'Failed to fetch submissions'
        commit('SET_ERROR', message)
        console.error('fetchResources error:', error)
        throw error
      } finally {
        commit('SET_LOADING', false)
      }
    },

    /**
     * Fetch single resource detail
     * GET /api/resources/:id
     */
    async fetchResource({ commit }, id) {
      commit('SET_LOADING', true)
      commit('SET_ERROR', null)

      try {
        const response = await api.get(`${API_BASE}/${id}`)
        commit('SET_CURRENT_RESOURCE', response.data)
        return response.data
      } catch (error) {
        const message = error.response?.data?.error || 'Failed to fetch resource'
        commit('SET_ERROR', message)
        console.error('fetchResource error:', error)
        throw error
      } finally {
        commit('SET_LOADING', false)
      }
    },

    /**
     * Create new resource (draft)
     * POST /api/resources
     */
    async createResource({ commit }, formData) {
      commit('SET_LOADING', true)
      commit('SET_ERROR', null)

      try {
        const response = await api.post(API_BASE, {
          ...formData,
          status: 'Draft'
        })

        const newResource = response.data
        commit('SET_CURRENT_RESOURCE', newResource)
        commit('SET_RESOURCES', [newResource, ...state.resources])

        return { success: true, resource: newResource }
      } catch (error) {
        const message = error.response?.data?.error || 'Failed to create resource'
        commit('SET_ERROR', message)
        console.error('createResource error:', error)
        return { success: false, error: message }
      } finally {
        commit('SET_LOADING', false)
      }
    },

    /**
     * Save draft (create or update)
     * POST /api/resources/:id/draft
     */
    async saveDraft({ commit, state }, { id, formData }) {
      commit('SET_LOADING', true)
      commit('SET_ERROR', null)

      try {
        let response

        if (id) {
          response = await api.post(`${API_BASE}/${id}/draft`, formData)
        } else {
          response = await api.post(API_BASE, {
            ...formData,
            status: 'Draft'
          })
        }

        const savedResource = response.data.resource || response.data

        if (!id) {
          commit('SET_RESOURCES', [savedResource, ...state.resources])
        } else {
          commit('UPDATE_RESOURCE_STATUS', {
            id: savedResource.resource_id || savedResource.id,
            status: 'Draft'
          })
        }

        return { success: true, resource: savedResource }
      } catch (error) {
        const message = error.response?.data?.error || 'Failed to save draft'
        commit('SET_ERROR', message)
        console.error('saveDraft error:', error)
        return { success: false, error: message }
      } finally {
        commit('SET_LOADING', false)
      }
    },

    /**
     * Update resource
     * PUT /api/resources/:id
     */
    async updateResource({ commit }, { id, formData }) {
      commit('SET_LOADING', true)
      commit('SET_ERROR', null)

      try {
        const response = await api.put(`${API_BASE}/${id}`, formData)

        const updatedResource = response.data

        commit('UPDATE_RESOURCE_STATUS', {
          id: updatedResource.resource_id || updatedResource.id,
          status: updatedResource.status
        })

        return { success: true, resource: updatedResource }
      } catch (error) {
        const message = error.response?.data?.error || 'Failed to update resource'
        commit('SET_ERROR', message)
        console.error('updateResource error:', error)
        return { success: false, error: message }
      } finally {
        commit('SET_LOADING', false)
      }
    },

    /**
     * Submit for review
     * POST /api/resources/:id/submit
     */
    async submitForReview({ commit, state }, id) {
      commit('SET_LOADING', true)
      commit('SET_ERROR', null)

      try {
        const response = await api.post(`${API_BASE}/${id}/submit`)

        commit('UPDATE_RESOURCE_STATUS', {
          id,
          status: 'Pending Review'
        })

        if (state.currentResource && (state.currentResource.resource_id === id || state.currentResource.id === id)) {
          commit('SET_CURRENT_RESOURCE', {
            ...state.currentResource,
            status: 'Pending Review',
            last_submitted_time: new Date().toISOString()
          })
        }

        return {
          success: true,
          message: 'Submitted successfully, resource is pending review',
          resource: response.data
        }
      } catch (error) {
        const message = error.response?.data?.error || 'Failed to submit for review'
        commit('SET_ERROR', message)
        console.error('submitForReview error:', error)
        return { success: false, error: message }
      } finally {
        commit('SET_LOADING', false)
      }
    },

    /**
     * Delete resource (draft only)
     * DELETE /api/resources/:id
     */
    async deleteResource({ commit }, id) {
      commit('SET_LOADING', true)
      commit('SET_ERROR', null)

      try {
        await api.delete(`${API_BASE}/${id}`)

        commit('REMOVE_RESOURCE', id)

        return { success: true }
      } catch (error) {
        const message = error.response?.data?.error || 'Failed to delete resource'
        commit('SET_ERROR', message)
        console.error('deleteResource error:', error)
        return { success: false, error: message }
      } finally {
        commit('SET_LOADING', false)
      }
    },

    /**
     * Upload file for resource
     * POST /api/resources/:id/upload
     */
    async uploadFile({ commit }, { resourceId, file }) {
      commit('SET_LOADING', true)
      commit('SET_ERROR', null)

      try {
        const formData = new FormData()
        formData.append('file', file)

        const response = await api.post(
          `${API_BASE}/${resourceId}/upload`,
          formData,
          {
            headers: {
              'Content-Type': 'multipart/form-data'
            }
          }
        )

        return { success: true, file: response.data.file }
      } catch (error) {
        const message = error.response?.data?.error || 'Failed to upload file'
        commit('SET_ERROR', message)
        console.error('uploadFile error:', error)
        return { success: false, error: message }
      } finally {
        commit('SET_LOADING', false)
      }
    },

    /**
     * Get files for resource
     * GET /api/resources/:id/files
     */
    async fetchFiles({ commit }, resourceId) {
      commit('SET_LOADING', true)
      commit('SET_ERROR', null)

      try {
        const response = await api.get(`${API_BASE}/${resourceId}/files`)
        return { success: true, files: response.data || [] }
      } catch (error) {
        const message = error.response?.data?.error || 'Failed to fetch files'
        commit('SET_ERROR', message)
        console.error('fetchFiles error:', error)
        return { success: false, error: message, files: [] }
      } finally {
        commit('SET_LOADING', false)
      }
    },

    /**
     * Delete file
     * DELETE /api/resources/:id/files/:fileId
     */
    async deleteFile({ commit }, { resourceId, fileId }) {
      commit('SET_LOADING', true)
      commit('SET_ERROR', null)

      try {
        await api.delete(`${API_BASE}/${resourceId}/files/${fileId}`)
        return { success: true }
      } catch (error) {
        const message = error.response?.data?.error || 'Failed to delete file'
        commit('SET_ERROR', message)
        console.error('deleteFile error:', error)
        return { success: false, error: message }
      } finally {
        commit('SET_LOADING', false)
      }
    },

    /**
     * Clear current resource
     */
    clearCurrentResource({ commit }) {
      commit('SET_CURRENT_RESOURCE', null)
    },

    /**
     * Clear error
     */
    clearError({ commit }) {
      commit('SET_ERROR', null)
    }
  },

  getters: {
    resourcesByStatus: (state) => (status) => {
      if (!status) return state.resources
      return state.resources.filter(r => r.status === status)
    },

    draftResources: (state) => {
      return state.resources.filter(r => r.status === 'Draft')
    },

    pendingResources: (state) => {
      return state.resources.filter(r => r.status === 'Pending Review')
    },

    rejectedResources: (state) => {
      return state.resources.filter(r => r.status === 'Rejected')
    },

    approvedResources: (state) => {
      return state.resources.filter(r => r.status === 'Approved')
    },

    statusCounts: (state) => {
      const counts = {
        all: state.resources.length,
        Draft: 0,
        'Pending Review': 0,
        Rejected: 0,
        Approved: 0
      }

      state.resources.forEach(r => {
        if (counts[r.status] !== undefined) {
          counts[r.status]++
        }
      })

      return counts
    },

    categoryOptions: (state) => {
      return state.categories.map(cat => ({
        value: cat.category_id,
        label: cat.category_topic || cat.category_name
      }))
    },

    tagOptions: (state) => {
      return state.tags.map(tag => ({
        value: tag.tag_id,
        label: tag.tag_name
      }))
    }
  }
}

export default contributorStore
