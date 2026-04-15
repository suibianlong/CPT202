// 临时存储（后续替换为数据库）
let versionStore = {}

class Version {
  static async save(resourceId, versionData) {
    if (!versionStore[resourceId]) {
      versionStore[resourceId] = []
    }
    
    const newVersion = {
      id: Date.now(),
      version_number: versionStore[resourceId].length + 1,
      resource_id: resourceId,
      snapshot: versionData.snapshot,
      change_type: versionData.change_type,
      change_summary: versionData.change_summary,
      created_by: versionData.created_by || 1,
      created_at: new Date().toISOString()
    }
    
    versionStore[resourceId].push(newVersion)
    
    // 只保留最近50个版本
    if (versionStore[resourceId].length > 50) {
      versionStore[resourceId] = versionStore[resourceId].slice(-50)
    }
    
    return newVersion
  }
  
  static async getVersions(resourceId) {
    return versionStore[resourceId] || []
  }
  
  static async getVersion(resourceId, versionNumber) {
    const versions = versionStore[resourceId] || []
    return versions.find(v => v.version_number === versionNumber)
  }
  
  static async deleteVersions(resourceId) {
    delete versionStore[resourceId]
  }
}

module.exports = Version