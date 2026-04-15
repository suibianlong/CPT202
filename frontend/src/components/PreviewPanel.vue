<template>
  <div class="preview-panel" :class="{ 'fullscreen': isFullscreen }">
    <div class="preview-header">
      <h3>实时预览</h3>
      <div class="preview-actions">
        <button class="preview-btn" @click="toggleFullscreen">
          {{ isFullscreen ? '✕ 退出' : '🖥️ 全屏' }}
        </button>
        <button class="preview-btn" @click="refreshPreview">
          🔄 刷新
        </button>
      </div>
    </div>
    
    <div class="preview-content">
      <div class="preview-section">
        <div class="preview-label">标题</div>
        <div class="preview-value title">{{ formData.title || '未填写' }}</div>
      </div>
      
      <div class="preview-section" v-if="formData.category_id">
        <div class="preview-label">分类</div>
        <div class="preview-value">
          <span class="category-badge">{{ getCategoryName() }}</span>
        </div>
      </div>
      
      <div class="preview-section" v-if="formData.place">
        <div class="preview-label">地点</div>
        <div class="preview-value">📍 {{ formData.place }}</div>
      </div>
      
      <div class="preview-section">
  <div class="preview-label">描述</div>
  <div class="preview-value description" v-html="renderedDescription || '未填写'"></div>
</div>
      
      <div class="preview-section" v-if="formData.tag_names && formData.tag_names.length">
        <div class="preview-label">标签</div>
        <div class="preview-value">
          <span v-for="tag in formData.tag_names" :key="tag" class="tag">#{{ tag }}</span>
        </div>
      </div>
      
      <div class="preview-section" v-if="files && files.length">
        <div class="preview-label">附件 ({{ files.length }}个文件)</div>
        <div class="preview-value">
          <div v-for="file in files" :key="file.file_id" class="file-preview">
            <span class="file-icon">📄</span>
            <span>{{ file.original_filename }}</span>
          </div>
        </div>
      </div>
      
      <div class="preview-section" v-if="formData.copyright_declaration">
        <div class="preview-label">版权声明</div>
        <div class="preview-value copyright">© {{ formData.copyright_declaration }}</div>
      </div>
      
      <div class="preview-completion" v-if="completionPercentage < 100">
        <div class="completion-bar">
          <div class="completion-fill" :style="{ width: completionPercentage + '%' }"></div>
        </div>
        <span class="completion-text">完成度: {{ completionPercentage }}%</span>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'PreviewPanel',
  props: {
    formData: { type: Object, required: true },
    files: { type: Array, default: () => [] },
    categories: { type: Array, default: () => [] }
  },
  data() {
    return {
      isFullscreen: false,
      refreshTimer: null,
      renderedDescription: ''
    };
  },
  computed: {
    completionPercentage() {
      let completed = 0;
      let total = 0;
      const fields = ['title', 'description', 'copyright_declaration'];
      fields.forEach(field => {
        total++;
        if (this.formData[field] && this.formData[field].trim()) completed++;
      });
      total++;
      if (this.files && this.files.length > 0) completed++;
      return Math.round((completed / total) * 100);
    }
  },
  watch: {
    formData: {
      handler() {
        clearTimeout(this.refreshTimer);
        this.refreshTimer = setTimeout(() => {
          this.renderDescription();
        }, 500);
      },
      deep: true
    }
  },
  mounted() {
    this.renderDescription();
  },
  beforeUnmount() {
    clearTimeout(this.refreshTimer);
  },
  methods: {
    renderDescription() {
      let text = this.formData.description || '';
      text = text.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');
      text = text.replace(/\*(.*?)\*/g, '<em>$1</em>');
      text = text.replace(/\[(.*?)\]\((.*?)\)/g, '<a href="$2" target="_blank">$1</a>');
      text = text.replace(/`(.*?)`/g, '<code>$1</code>');
      text = text.replace(/\n/g, '<br>');
      this.renderedDescription = text;
    },
    getCategoryName() {
      const category = this.categories.find(c => c.id == this.formData.category_id);
      return category ? (category.category_topic || category.category_name || category.name) : '未知分类';
    },
    toggleFullscreen() {
      this.isFullscreen = !this.isFullscreen;
      this.$emit('fullscreen-change', this.isFullscreen);
    },
    refreshPreview() {
      this.renderDescription();
      this.$emit('refresh');
    }
  }
};
</script>

<style scoped>
.preview-panel {
  background: #fafafa;
  border-left: 1px solid #e0e0e0;
  width: 100%;
  height: 100%;
  overflow-y: auto;
}

.preview-panel.fullscreen {
  position: fixed;
  top: 0;
  right: 0;
  width: 50%;
  height: 100vh;
  z-index: 1000;
  box-shadow: -2px 0 8px rgba(0,0,0,0.1);
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: white;
  border-bottom: 1px solid #e0e0e0;
  position: sticky;
  top: 0;
  z-index: 10;
}

.preview-header h3 { margin: 0; font-size: 16px; }
.preview-actions { display: flex; gap: 8px; }
.preview-btn { background: none; border: 1px solid #ddd; padding: 4px 8px; border-radius: 4px; cursor: pointer; font-size: 12px; }
.preview-content { padding: 20px; }
.preview-section { margin-bottom: 20px; padding-bottom: 16px; border-bottom: 1px solid #eee; }
.preview-label { font-size: 11px; font-weight: 600; color: #999; margin-bottom: 8px; text-transform: uppercase; }
.preview-value { font-size: 14px; color: #333; line-height: 1.6; }
.preview-value.title { font-size: 18px; font-weight: 600; }
.category-badge { background: #e3f2fd; color: #1565c0; padding: 4px 12px; border-radius: 16px; font-size: 13px; display: inline-block; }
.tag { background: #f0f0f0; color: #666; padding: 2px 8px; border-radius: 12px; font-size: 11px; margin-right: 6px; display: inline-block; }
.file-preview { padding: 4px 0; font-size: 13px; }
.completion-bar { height: 4px; background: #e0e0e0; border-radius: 2px; margin-bottom: 8px; }
.completion-fill { height: 100%; background: #4caf50; transition: width 0.3s; border-radius: 2px; }
.completion-text { font-size: 11px; color: #666; }
</style>