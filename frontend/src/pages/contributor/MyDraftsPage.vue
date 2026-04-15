<template>
  <div class="my-drafts">
    <div class="page-header">
      <h2>我的草稿</h2>
      <button class="btn-primary" @click="createNew">+ 新建资源</button>
    </div>

    <div v-if="loading" class="loading">加载中...</div>
    <div v-if="errorMessage" class="alert alert-error">{{ errorMessage }}</div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <input 
        type="text" 
        v-model="searchKeyword" 
        placeholder="搜索标题..."
        @input="filterDrafts"
        class="search-input"
      />
      <select v-model="sortBy" @change="sortDrafts">
        <option value="updated">最后编辑时间</option>
        <option value="created">创建时间</option>
        <option value="title">标题</option>
      </select>
    </div>

    <!-- 草稿列表 -->
    <div v-if="!loading && filteredDrafts.length > 0" class="drafts-list">
      <div v-for="draft in filteredDrafts" :key="draft.id" class="draft-card">
        <div class="draft-header">
          <h3 class="draft-title">
            {{ draft.title || '无标题草稿' }}
            <span v-if="draft.auto_save_exists" class="badge badge-auto">自动保存</span>
            <span v-if="draft.has_local_backup" class="badge badge-local">本地备份</span>
          </h3>
          <div class="draft-actions">
            <button class="btn-icon" @click="continueEditing(draft.id)" title="继续编辑">
              ✏️ 编辑
            </button>
            <button class="btn-icon danger" @click="confirmDelete(draft)" title="删除">
              🗑️ 删除
            </button>
          </div>
        </div>
        
        <div class="draft-body">
          <p class="draft-description">{{ draft.description || '暂无描述' }}</p>
          <div class="draft-meta">
            <span class="meta-item">
              📅 最后编辑: {{ formatDateTime(draft.last_updated_time) }}
            </span>
            <span class="meta-item" v-if="draft.last_auto_save_time">
              💾 自动保存: {{ formatDateTime(draft.last_auto_save_time) }}
            </span>
            <span class="meta-item" v-if="draft.category">
              📁 {{ draft.category }}
            </span>
          </div>
          <div class="draft-progress" v-if="draft.completion_percentage">
            <div class="progress-bar">
              <div class="progress-fill" :style="{ width: draft.completion_percentage + '%' }"></div>
            </div>
            <span class="progress-text">完成度: {{ draft.completion_percentage }}%</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-if="!loading && filteredDrafts.length === 0" class="empty-state">
      <div class="empty-icon">📝</div>
      <h3>暂无草稿</h3>
      <p>点击上方按钮创建新的资源草稿</p>
    </div>

    <!-- 删除确认弹窗 -->
    <div v-if="showDeleteConfirm" class="modal-overlay" @click.self="cancelDelete">
      <div class="modal">
        <h3>确认删除</h3>
        <p>确定要删除草稿 "{{ deleteTarget?.title || '无标题' }}" 吗？此操作不可撤销。</p>
        <div class="modal-actions">
          <button @click="cancelDelete" class="btn-secondary">取消</button>
          <button @click="executeDelete" class="btn-danger" :disabled="deleting">
            {{ deleting ? '删除中...' : '确认删除' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

const API_BASE = '/api/resources'

export default {
  name: 'MyDraftsPage',
  data() {
    return {
      drafts: [],
      filteredDrafts: [],
      loading: false,
      errorMessage: '',
      searchKeyword: '',
      sortBy: 'updated',
      showDeleteConfirm: false,
      deleteTarget: null,
      deleting: false
    }
  },
  methods: {
    async fetchDrafts() {
      this.loading = true
      this.errorMessage = ''
      
      try {
        // 获取状态为 Draft 的资源
        const response = await axios.get(`${API_BASE}/my`, { 
          params: { status: 'Draft' }
        })
        
        let drafts = response.data || []
        
        // 检查本地存储中的草稿
        drafts = drafts.map(draft => {
          const localBackup = this.getLocalBackup(draft.id)
          const autoSaveExists = this.checkAutoSaveExists(draft.id)
          
          return {
            ...draft,
            has_local_backup: !!localBackup,
            auto_save_exists: autoSaveExists,
            last_auto_save_time: localBackup?.timestamp,
            completion_percentage: this.calculateCompletion(draft)
          }
        })
        
        this.drafts = drafts
        this.filterDrafts()
      } catch (error) {
        this.errorMessage = error.response?.data?.error || '加载草稿失败'
      } finally {
        this.loading = false
      }
    },
    
    getLocalBackup(resourceId) {
      try {
        const key = `draft_${resourceId}`
        const saved = localStorage.getItem(key)
        return saved ? JSON.parse(saved) : null
      } catch (e) {
        return null
      }
    },
    
    checkAutoSaveExists(resourceId) {
      // 检查是否有自动保存的记录
      const autoSaveKey = `auto_save_${resourceId}`
      return !!localStorage.getItem(autoSaveKey)
    },
    
    calculateCompletion(draft) {
      let completed = 0
      let total = 0
      
      // 计算完成度
      const fields = ['title', 'description', 'copyright_declaration']
      fields.forEach(field => {
        total++
        if (draft[field] && draft[field].trim()) completed++
      })
      
      // 文件完成度
      total++
      if (draft.file_count > 0) completed++
      
      return Math.round((completed / total) * 100)
    },
    
    filterDrafts() {
      let filtered = [...this.drafts]
      
      // 搜索过滤
      if (this.searchKeyword) {
        const keyword = this.searchKeyword.toLowerCase()
        filtered = filtered.filter(draft => 
          draft.title?.toLowerCase().includes(keyword) ||
          draft.description?.toLowerCase().includes(keyword)
        )
      }
      
      this.filteredDrafts = filtered
      this.sortDrafts()
    },
    
    sortDrafts() {
      const sortFunctions = {
        updated: (a, b) => new Date(b.last_updated_time) - new Date(a.last_updated_time),
        created: (a, b) => new Date(b.created_time) - new Date(a.created_time),
        title: (a, b) => (a.title || '').localeCompare(b.title || '')
      }
      
      this.filteredDrafts.sort(sortFunctions[this.sortBy] || sortFunctions.updated)
    },
    
    continueEditing(id) {
      this.$router.push(`/contributor/edit/${id}`)
    },
    
    createNew() {
      this.$router.push('/contributor/create')
    },
    
    confirmDelete(draft) {
      this.deleteTarget = draft
      this.showDeleteConfirm = true
    },
    
    cancelDelete() {
      this.showDeleteConfirm = false
      this.deleteTarget = null
    },
    
    async executeDelete() {
      if (!this.deleteTarget) return
      this.deleting = true
      
      try {
        const id = this.deleteTarget.id
        await axios.delete(`${API_BASE}/${id}`)
        
        // 清理本地存储
        localStorage.removeItem(`draft_${id}`)
        localStorage.removeItem(`auto_save_${id}`)
        
        this.drafts = this.drafts.filter(d => d.id !== id)
        this.filterDrafts()
        this.showDeleteConfirm = false
        this.deleteTarget = null
      } catch (error) {
        this.errorMessage = error.response?.data?.error || '删除失败'
      } finally {
        this.deleting = false
      }
    },
    
    formatDateTime(dateStr) {
      if (!dateStr) return '-'
      const date = new Date(dateStr)
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    }
  },
  mounted() {
    this.fetchDrafts()
  }
}
</script>

<style scoped>
.my-drafts {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-header h2 {
  font-size: 24px;
  font-weight: 600;
  color: #1a1a1a;
}

.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
}

.search-input {
  flex: 1;
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
}

.filter-bar select {
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  background: white;
}

.drafts-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.draft-card {
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 16px;
  transition: box-shadow 0.2s;
}

.draft-card:hover {
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.draft-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.draft-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
}

.badge {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 500;
  margin-left: 8px;
}

.badge-auto {
  background: #e3f2fd;
  color: #1976d2;
}

.badge-local {
  background: #fff3e0;
  color: #f57c00;
}

.draft-actions {
  display: flex;
  gap: 8px;
}

.btn-icon {
  background: none;
  border: none;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background 0.2s;
}

.btn-icon:hover {
  background: #f5f5f5;
}

.btn-icon.danger:hover {
  background: #ffebee;
  color: #c62828;
}

.draft-body {
  margin-top: 8px;
}

.draft-description {
  color: #666;
  font-size: 14px;
  line-height: 1.5;
  margin-bottom: 12px;
}

.draft-meta {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}

.meta-item {
  font-size: 12px;
  color: #999;
}

.draft-progress {
  margin-top: 12px;
}

.progress-bar {
  height: 4px;
  background: #e0e0e0;
  border-radius: 2px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: #4caf50;
  transition: width 0.3s;
}

.progress-text {
  font-size: 11px;
  color: #666;
  margin-top: 4px;
  display: inline-block;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  background: #fafafa;
  border-radius: 8px;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.empty-state h3 {
  font-size: 18px;
  color: #666;
  margin-bottom: 8px;
}

.empty-state p {
  color: #999;
}

.loading {
  text-align: center;
  padding: 40px;
  color: #666;
}

.alert {
  padding: 12px 16px;
  border-radius: 4px;
  margin-bottom: 20px;
}

.alert-error {
  background: #ffebee;
  color: #c62828;
  border: 1px solid #ffcdd2;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal {
  background: white;
  padding: 24px;
  border-radius: 8px;
  max-width: 400px;
  width: 90%;
}

.modal h3 {
  margin-bottom: 12px;
}

.modal p {
  margin-bottom: 20px;
  color: #666;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.btn-primary {
  background: #1a1a1a;
  color: white;
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.btn-secondary {
  background: #f0f0f0;
  color: #333;
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.btn-danger {
  background: #c62828;
  color: white;
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.btn-danger:hover {
  background: #b71c1c;
}
</style>