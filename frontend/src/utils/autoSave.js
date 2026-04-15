/**
 * Auto-save service with 30-second debounce
 * Handles automatic saving of drafts and localStorage backup
 */
import axios from 'axios'

class AutoSaveService {
  constructor(options = {}) {
    this.resourceId = options.resourceId || 'new'
    this.onAutoSave = options.onAutoSave || (() => {})
    this.onSaveSuccess = options.onSaveSuccess || (() => {})
    this.onSaveError = options.onSaveError || (() => {})
    this.onRetry = options.onRetry || (() => {})

    this.debounceTime = 30000 // 30 seconds
    this.timer = null
    this.retryCount = 0
    this.maxRetries = 3
    this.lastSaveTime = null
    this.pendingData = null
    this.lastSavedDataHash = null  // 新增：记录上次保存的数据哈希
    this.isSaving = false          // 新增：防止并发保存
    this.isDestroyed = false       // 新增：标记服务是否已销毁
  }

  // 新增：计算数据哈希的简单方法
  getDataHash(data) {
    if (!data) return null
    try {
      // 只比较关键字段，避免函数和循环引用
      const keyData = {
        title: data.title,
        description: data.description,
        categoryId: data.categoryId,
        tags: data.tags,
        files: data.files ? data.files.map(f => f.name || f.id) : []
      }
      return JSON.stringify(keyData)
    } catch (e) {
      return null
    }
  }

  onFormChange(formData) {
    if (this.isDestroyed) return

    this.pendingData = formData

    // Clear existing timer
    if (this.timer) {
      clearTimeout(this.timer)
    }

    // Set new debounced save timer
    this.timer = setTimeout(() => {
      this.performAutoSave()
    }, this.debounceTime)
  }

  async performAutoSave() {
    // 防止在已销毁或正在保存时执行
    if (this.isDestroyed || this.isSaving) {
      console.log('Auto-save skipped: service destroyed or already saving')
      return
    }

    if (!this.pendingData) {
      console.log('Auto-save skipped: no pending data')
      return
    }

    // 检查数据是否真的变化了
    const currentDataHash = this.getDataHash(this.pendingData)
    if (currentDataHash === this.lastSavedDataHash) {
      console.log('Auto-save skipped: data unchanged since last save')
      return
    }

    this.isSaving = true
    this.onAutoSave({ data: this.pendingData })

    try {
      const response = await this.triggerAutoSave()
      this.lastSaveTime = new Date()
      this.retryCount = 0
      this.lastSavedDataHash = currentDataHash  // 记录成功保存的数据哈希
      
      // 保存成功后清除 localStorage 备份
      this.clearBackups(this.resourceId)
      
      this.onSaveSuccess({
        savedAt: this.lastSaveTime,
        resourceId: this.resourceId,
        data: response
      })
      
      console.log('Auto-save successful at', this.lastSaveTime)
    } catch (error) {
      console.error('Auto-save failed:', error)
      this.handleSaveError(error)
    } finally {
      this.isSaving = false
    }
  }

  async triggerAutoSave() {
    const API_BASE = '/api/resources'
    
    // 准备保存的数据（移除不需要发送的字段）
    const saveData = { ...this.pendingData }
    delete saveData.files  // 文件通常单独上传，不包含在自动保存中
    delete saveData.uploadedFiles

    if (this.resourceId && this.resourceId !== 'new') {
      const response = await axios.post(`${API_BASE}/${this.resourceId}/draft`, saveData)
      return response.data
    } else {
      const response = await axios.post(API_BASE, {
        ...saveData,
        status: 'Draft'
      })
      // 如果是新资源，保存成功后更新 resourceId
      if (response.data && response.data.id) {
        this.resourceId = response.data.id
      }
      return response.data
    }
  }

  handleSaveError(error) {
    // 检查是否已达到最大重试次数
    if (this.retryCount >= this.maxRetries) {
      console.error('Auto-save failed after max retries:', this.maxRetries)
      this.onSaveError({
        error: error.message || 'Unknown error',
        retryCount: this.retryCount,
        savedLocally: true,
        maxRetriesReached: true
      })
      // 重置重试计数，避免无限重试
      this.retryCount = 0
      return
    }

    this.retryCount++

    const errorInfo = {
      error: error.message || 'Unknown error',
      retryCount: this.retryCount,
      savedLocally: false
    }

    // Save to localStorage on failure
    this.saveToLocalStorage(this.pendingData, this.resourceId)
    errorInfo.savedLocally = true

    this.onSaveError(errorInfo)

    // Retry with exponential backoff，但检查是否已销毁
    if (!this.isDestroyed && this.retryCount < this.maxRetries) {
      const delay = Math.min(Math.pow(2, this.retryCount) * 1000, 30000) // 限制最大延迟30秒
      console.log(`Auto-save retry ${this.retryCount}/${this.maxRetries} in ${delay}ms`)
      
      setTimeout(() => {
        if (!this.isDestroyed && this.onRetry) {
          this.onRetry()
          // 重置 isSaving 状态以允许重试
          this.isSaving = false
          this.performAutoSave()
        }
      }, delay)
    }
  }

  saveToLocalStorage(formData, resourceId) {
    try {
      const key = `draft_${resourceId}`
      const draftData = {
        formData,
        timestamp: new Date().toISOString(),
        resourceId
      }
      localStorage.setItem(key, JSON.stringify(draftData))
      console.log('Backup saved to localStorage with key:', key)
    } catch (e) {
      console.error('Failed to save to localStorage:', e)
    }
  }

  loadFromLocalStorage(resourceId) {
    try {
      const key = `draft_${resourceId}`
      const saved = localStorage.getItem(key)
      if (saved) {
        console.log('Loaded backup from localStorage for resource:', resourceId)
        return JSON.parse(saved)
      }
      return null
    } catch (e) {
      console.error('Failed to load from localStorage:', e)
      return null
    }
  }

  clearBackups(resourceId) {
    try {
      const key = `draft_${resourceId}`
      localStorage.removeItem(key)
      console.log('Cleared localStorage backup for resource:', resourceId)
    } catch (e) {
      console.error('Failed to clear localStorage:', e)
    }
  }

  manualSave(silent = false) {
    if (this.timer) {
      clearTimeout(this.timer)
    }
    // 手动保存时也检查是否正在保存
    if (!this.isSaving) {
      return this.performAutoSave()
    }
    return Promise.resolve({ manual: true, skipped: 'already saving' })
  }

  hasUnsavedChanges() {
    if (!this.pendingData) return false
    
    const currentHash = this.getDataHash(this.pendingData)
    return currentHash !== this.lastSavedDataHash
  }

  clearChanges() {
    this.pendingData = null
    if (this.timer) {
      clearTimeout(this.timer)
    }
  }

  destroy() {
    console.log('Destroying auto-save service')
    this.isDestroyed = true
    if (this.timer) {
      clearTimeout(this.timer)
      this.timer = null
    }
    this.pendingData = null
    this.isSaving = false
    this.retryCount = 0
  }
}

function initAutoSave(options) {
  const service = new AutoSaveService(options)
  return service
}

export { AutoSaveService, initAutoSave }
export default { initAutoSave }