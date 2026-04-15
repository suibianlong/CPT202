// 临时存储（后续替换为数据库）
let versionStore = {};

const saveVersion = async (req, res) => {
  try {
    const { id } = req.params;
    const { snapshot, change_type, change_summary } = req.body;
    
    if (!versionStore[id]) {
      versionStore[id] = [];
    }
    
    const newVersion = {
      id: Date.now(),
      version_number: versionStore[id].length + 1,
      resource_id: id,
      snapshot: JSON.stringify(snapshot),
      change_type: change_type || 'edit',
      change_summary: change_summary || '内容更新',
      created_by: req.user?.id || 1,
      created_at: new Date().toISOString()
    };
    
    versionStore[id].push(newVersion);
    
    if (versionStore[id].length > 50) {
      versionStore[id] = versionStore[id].slice(-50);
    }
    
    res.json({ success: true, version: newVersion });
  } catch (error) {
    console.error('Save version error:', error);
    res.status(500).json({ error: error.message });
  }
};

const getVersions = async (req, res) => {
  try {
    const { id } = req.params;
    const versions = versionStore[id] || [];
    res.json(versions);
  } catch (error) {
    console.error('Get versions error:', error);
    res.status(500).json({ error: error.message });
  }
};

const getVersion = async (req, res) => {
  try {
    const { id, versionNumber } = req.params;
    const versions = versionStore[id] || [];
    const version = versions.find(v => v.version_number === parseInt(versionNumber));
    
    if (!version) {
      return res.status(404).json({ error: 'Version not found' });
    }
    
    res.json(version);
  } catch (error) {
    console.error('Get version error:', error);
    res.status(500).json({ error: error.message });
  }
};

const rollbackToVersion = async (req, res) => {
  try {
    const { id, versionNumber } = req.params;
    const versions = versionStore[id] || [];
    const targetVersion = versions.find(v => v.version_number === parseInt(versionNumber));
    
    if (!targetVersion) {
      return res.status(404).json({ error: 'Version not found' });
    }
    
    const snapshot = JSON.parse(targetVersion.snapshot);
    
    const rollbackVersion = {
      id: Date.now(),
      version_number: versions.length + 1,
      resource_id: id,
      snapshot: JSON.stringify(snapshot),
      change_type: 'rollback',
      change_summary: `回滚到版本 ${versionNumber}`,
      created_by: req.user?.id || 1,
      created_at: new Date().toISOString()
    };
    
    versionStore[id].push(rollbackVersion);
    
    res.json({ 
      success: true, 
      formData: snapshot,
      version: rollbackVersion,
      message: `已回滚到版本 ${versionNumber}`
    });
  } catch (error) {
    console.error('Rollback error:', error);
    res.status(500).json({ error: error.message });
  }
};

module.exports = {
  saveVersion,
  getVersions,
  getVersion,
  rollbackToVersion
};