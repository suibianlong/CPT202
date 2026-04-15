// Resource API Controller
import axios from 'axios'

const API_BASE = '/api/resources'

const resourceApi = {
  // 获取所有分类
  getCategories: async () => {
    const response = await axios.get(`${API_BASE}/categories`)
    return response.data
  },

  // 获取所有标签
  getTags: async () => {
    const response = await axios.get(`${API_BASE}/tags`)
    return response.data
  },

  // 获取用户的资源列表
  getMyResources: async (status = '') => {
    const params = status ? { status } : {}
    const response = await axios.get(`${API_BASE}/my`, { params })
    return response.data
  },

  // 获取单个资源详情
  getResourceById: async (id) => {
    const response = await axios.get(`${API_BASE}/${id}`)
    return response.data
  },

  // 创建资源
  createResource: async (data) => {
    const response = await axios.post(API_BASE, data)
    return response.data
  },

  // 更新资源
  updateResource: async (id, data) => {
    const response = await axios.put(`${API_BASE}/${id}`, data)
    return response.data
  },

  // 保存草稿
  saveDraft: async (id, data) => {
    const response = await axios.post(`${API_BASE}/${id}/draft`, data)
    return response.data
  },

  // 提交审核
  submitForReview: async (id) => {
    const response = await axios.post(`${API_BASE}/${id}/submit`)
    return response.data
  },

  // 删除资源
  deleteResource: async (id) => {
    await axios.delete(`${API_BASE}/${id}`)
  },

  // 上传文件
  uploadFile: async (resourceId, file) => {
    const formData = new FormData()
    formData.append('file', file)
    const response = await axios.post(`${API_BASE}/${resourceId}/upload`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    return response.data
  },

  // 获取文件列表
  getFiles: async (resourceId) => {
    const response = await axios.get(`${API_BASE}/${resourceId}/files`)
    return response.data
  },

  // 删除文件
  deleteFile: async (resourceId, fileId) => {
    await axios.delete(`${API_BASE}/${resourceId}/files/${fileId}`)
  }
}

export default resourceApi