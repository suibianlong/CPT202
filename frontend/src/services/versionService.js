// frontend/src/services/versionService.js
import axios from 'axios'

const API_BASE = '/api'

class VersionService {
  constructor(resourceId) {
    this.resourceId = resourceId
  }

  async saveVersion(formData, changeType) {
    try {
      const response = await axios.post(`${API_BASE}/resources/${this.resourceId}/versions`, {
        snapshot: {
          title: formData.title,
          description: formData.description,
          category_id: formData.category_id,
          place: formData.place,
          copyright_declaration: formData.copyright_declaration,
          usage_declaration: formData.usage_declaration,
          tag_names: formData.tag_names,
          file_count: formData.file_count || 0
        },
        change_type: changeType,
        change_summary: this.generateChangeSummary(formData)
      })
      return response.data.version
    } catch (error) {
      console.error('Save version failed:', error)
      return null
    }
  }

  async getVersionHistory() {
    try {
      const response = await axios.get(`${API_BASE}/resources/${this.resourceId}/versions`)
      return response.data
    } catch (error) {
      console.error('Get version history failed:', error)
      return []
    }
  }

  async getVersion(versionNumber) {
    try {
      const response = await axios.get(`${API_BASE}/resources/${this.resourceId}/versions/${versionNumber}`)
      return response.data
    } catch (error) {
      console.error('Get version failed:', error)
      return null
    }
  }

  async rollback(versionNumber) {
    try {
      const response = await axios.post(`${API_BASE}/resources/${this.resourceId}/rollback/${versionNumber}`)
      return response.data
    } catch (error) {
      console.error('Rollback failed:', error)
      return { success: false, error: error.message }
    }
  }

  generateChangeSummary(formData) {
    const changes = []
    if (formData.title) changes.push('标题')
    if (formData.description) changes.push('描述')
    if (formData.category_id) changes.push('分类')
    if (formData.place) changes.push('地点')
    if (formData.tag_names?.length) changes.push(`标签(${formData.tag_names.length})`)
    
    return changes.length > 0 ? `更新了: ${changes.join('、')}` : '内容更新'
  }

  cleanupOldVersions() {
    // 后端自动处理，前端不需要
  }
}

export default VersionService