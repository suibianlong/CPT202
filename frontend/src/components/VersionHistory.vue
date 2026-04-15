<template>
  <div class="version-history">
    <div class="version-header">
      <h3>版本历史</h3>
      <button class="close-btn" @click="$emit('close')">×</button>
    </div>
    
    <div class="version-timeline" v-if="versions.length > 0">
      <div 
        v-for="version in versions" 
        :key="version.version_number"
        class="version-item"
        :class="{ active: selectedVersion === version.version_number }"
        @click="selectVersion(version.version_number)"
      >
        <div class="version-badge">
          <span class="version-number">v{{ version.version_number }}</span>
          <span class="version-type" :class="getTypeClass(version.change_type)">
            {{ getTypeLabel(version.change_type) }}
          </span>
        </div>
        <div class="version-info">
          <div class="version-summary">{{ version.change_summary }}</div>
          <div class="version-time">{{ formatTime(version.created_at) }}</div>
        </div>
      </div>
    </div>
    
    <div v-else class="empty-versions">暂无版本记录</div>
    
    <div v-if="selectedVersion && compareData && Object.keys(compareData).length > 0" class="version-compare">
      <h4>版本对比 (v{{ selectedVersion }} vs 当前)</h4>
      <div class="compare-content">
        <div v-for="(diff, field) in compareData" :key="field" class="diff-item">
          <div class="diff-field">{{ getFieldLabel(field) }}</div>
          <div class="diff-old"><strong>旧值:</strong> <span class="deleted">{{ diff.old || '(空)' }}</span></div>
          <div class="diff-new"><strong>新值:</strong> <span class="added">{{ diff.new || '(空)' }}</span></div>
        </div>
      </div>
      <div class="compare-actions">
        <button class="btn-primary" @click="confirmRollback" :disabled="rollingBack">
          {{ rollingBack ? '回滚中...' : '回滚到此版本' }}
        </button>
      </div>
    </div>
    
    <div v-if="showRollbackConfirm" class="modal-overlay" @click.self="showRollbackConfirm = false">
      <div class="modal">
        <h3>确认回滚</h3>
        <p>确定要回滚到版本 v{{ selectedVersion }} 吗？</p>
        <p class="warning">这将创建一个新的版本记录，当前内容将被覆盖。</p>
        <div class="modal-actions">
          <button @click="showRollbackConfirm = false" class="btn-secondary">取消</button>
          <button @click="executeRollback" class="btn-primary">确认回滚</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import VersionService from '../services/versionService';

export default {
  name: 'VersionHistory',
  props: {
    resourceId: { type: [Number, String], required: true },
    currentFormData: { type: Object, required: true }
  },
  data() {
    return {
      versions: [],
      selectedVersion: null,
      compareData: null,
      rollingBack: false,
      showRollbackConfirm: false,
      versionService: null
    };
  },
  async mounted() {
    this.versionService = new VersionService(this.resourceId);
    await this.loadVersionHistory();
  },
  methods: {
    async loadVersionHistory() {
      this.versions = await this.versionService.getVersionHistory();
      if (this.versions.length > 0) {
        this.selectedVersion = this.versions[0].version_number;
        await this.compareVersion();
      }
    },
    async selectVersion(versionNumber) {
      this.selectedVersion = versionNumber;
      await this.compareVersion();
    },
    async compareVersion() {
      const selectedVer = await this.versionService.getVersion(this.selectedVersion);
      if (!selectedVer) return;
      
      // 手动解析版本数据
      const oldData = typeof selectedVer.snapshot === 'string' 
        ? JSON.parse(selectedVer.snapshot) 
        : selectedVer.snapshot;
      
      const newData = {
        title: this.currentFormData.title || '',
        description: this.currentFormData.description || '',
        category_id: this.currentFormData.category_id || '',
        place: this.currentFormData.place || '',
        copyright_declaration: this.currentFormData.copyright_declaration || '',
        usage_declaration: this.currentFormData.usage_declaration || '',
        tag_names: this.currentFormData.tag_names || []
      };
      
      const differences = {};
      const allFields = new Set([...Object.keys(oldData), ...Object.keys(newData)]);
      
      for (const field of allFields) {
        const oldValue = oldData[field] !== undefined && oldData[field] !== null ? String(oldData[field]) : '';
        const newValue = newData[field] !== undefined && newData[field] !== null ? String(newData[field]) : '';
        
        if (oldValue !== newValue) {
          differences[field] = { old: oldValue, new: newValue };
        }
      }
      
      this.compareData = differences;
    },
    confirmRollback() {
      this.showRollbackConfirm = true;
    },
    async executeRollback() {
      this.rollingBack = true;
      const result = await this.versionService.rollback(this.selectedVersion);
      if (result.success) {
        this.$emit('rollback', result.formData);
        this.showRollbackConfirm = false;
        await this.loadVersionHistory();
      } else {
        this.$emit('error', result.error);
      }
      this.rollingBack = false;
    },
    getTypeLabel(type) {
      const labels = { create: '创建', edit: '编辑', submit: '提交', revision: '修订', rollback: '回滚' };
      return labels[type] || '更新';
    },
    getTypeClass(type) {
      const classes = { create: 'type-create', submit: 'type-submit', revision: 'type-revision', rollback: 'type-rollback' };
      return classes[type] || '';
    },
    getFieldLabel(field) {
      const labels = { 
        title: '标题', 
        description: '描述', 
        category_id: '分类', 
        place: '地点', 
        copyright_declaration: '版权声明', 
        usage_declaration: '使用声明', 
        tag_names: '标签' 
      };
      return labels[field] || field;
    },
    formatTime(dateStr) {
      if (!dateStr) return '';
      const date = new Date(dateStr);
      return date.toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' });
    }
  }
};
</script>

<style scoped>
.version-history {
  position: fixed;
  right: 0;
  top: 0;
  width: 500px;
  height: 100vh;
  background: white;
  box-shadow: -2px 0 8px rgba(0,0,0,0.1);
  display: flex;
  flex-direction: column;
  z-index: 1000;
}
.version-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #e0e0e0;
}
.version-header h3 { margin: 0; font-size: 18px; }
.close-btn { background: none; border: none; font-size: 24px; cursor: pointer; color: #999; }
.version-timeline { flex: 1; overflow-y: auto; padding: 16px; }
.version-item {
  display: flex;
  gap: 12px;
  padding: 12px;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  margin-bottom: 12px;
  cursor: pointer;
  transition: all 0.2s;
}
.version-item:hover { background: #f5f5f5; }
.version-item.active { background: #e3f2fd; border-color: #2196f3; }
.version-badge { display: flex; flex-direction: column; align-items: center; gap: 4px; min-width: 60px; }
.version-number { font-weight: 600; font-size: 14px; }
.version-type { font-size: 10px; padding: 2px 6px; border-radius: 3px; }
.type-create { background: #4caf50; color: white; }
.type-submit { background: #ff9800; color: white; }
.type-revision { background: #2196f3; color: white; }
.type-rollback { background: #9c27b0; color: white; }
.version-info { flex: 1; }
.version-summary { font-size: 14px; margin-bottom: 4px; }
.version-time { font-size: 11px; color: #999; }
.empty-versions { text-align: center; padding: 40px; color: #999; }
.version-compare {
  border-top: 1px solid #e0e0e0;
  padding: 16px;
  max-height: 50%;
  overflow-y: auto;
}
.version-compare h4 { margin: 0 0 12px 0; font-size: 14px; }
.diff-item { margin-bottom: 12px; padding: 8px; background: #f9f9f9; border-radius: 4px; }
.diff-field { font-weight: 600; font-size: 12px; color: #666; margin-bottom: 8px; }
.diff-old, .diff-new { font-size: 12px; margin-bottom: 4px; }
.deleted { background: #ffebee; color: #c62828; padding: 2px 4px; border-radius: 3px; display: inline-block; }
.added { background: #e8f5e9; color: #2e7d32; padding: 2px 4px; border-radius: 3px; display: inline-block; }
.compare-actions { margin-top: 16px; text-align: center; }
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
  z-index: 1100;
}
.modal {
  background: white;
  padding: 24px;
  border-radius: 8px;
  max-width: 400px;
}
.warning { color: #f57c00; font-size: 12px; margin-top: 8px; }
.modal-actions { display: flex; justify-content: flex-end; gap: 8px; margin-top: 20px; }
.btn-primary { background: #1a1a1a; color: white; padding: 8px 16px; border: none; border-radius: 4px; cursor: pointer; }
.btn-secondary { background: #f0f0f0; color: #333; padding: 8px 16px; border: none; border-radius: 4px; cursor: pointer; }
</style>