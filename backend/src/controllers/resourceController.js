let tempResources = [];
let nextId = 1;
let uploadedFilesStore = {};  
const { Resource, Category } = require('../models');

// 临时数据存储（绕过数据库问题）


// 初始化一些示例数据
tempResources.push({
  id: 1,
  resource_id: 1,
  title: '示例草稿',
  description: '这是一个测试草稿',
  status: 'Draft',
  user_id: 1,
  created_time: new Date().toISOString(),
  last_updated_time: new Date().toISOString(),
  categories: [{ id: 1, category_topic: 'Culture' }]
});

const createResource = async (req, res) => {
  try {
    console.log('Creating resource:', req.body);
    const resource = {
      id: nextId++,
      resource_id: nextId,
      ...req.body,
      user_id: req.user?.id || 1,
      status: 'Draft',
      created_time: new Date().toISOString(),
      last_updated_time: new Date().toISOString()
    };
    tempResources.push(resource);
    console.log('Resource created:', resource);
    res.status(201).json(resource);
  } catch (error) {
    console.error('Create error:', error);
    res.status(500).json({ error: error.message, stack: error.stack });
  }
};

const getMyResources = async (req, res) => {
  try {
    console.log('Getting resources for user');
    const { status } = req.query;
    let userResources = tempResources.filter(r => r.user_id === (req.user?.id || 1));
    
    if (status) {
      userResources = userResources.filter(r => r.status === status);
    }
    
    console.log(`Found ${userResources.length} resources`);
    res.json(userResources);
  } catch (error) {
    console.error('Get resources error:', error);
    res.status(500).json({ error: error.message });
  }
};

// 修复 getResourceById - 确保返回完整的资源信息（包括文件）


const updateResource = async (req, res) => {
  try {
    const index = tempResources.findIndex(r => r.id === parseInt(req.params.id));
    if (index === -1) {
      return res.status(404).json({ error: 'Resource not found' });
    }
    tempResources[index] = { ...tempResources[index], ...req.body, last_updated_time: new Date().toISOString() };
    res.json(tempResources[index]);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
};

// 修复 submitForReview - 状态改为前端期望的格式
const submitForReview = async (req, res) => {
  try {
    const resourceId = parseInt(req.params.id);
    const resource = tempResources.find(r => r.id === resourceId || r.resource_id === resourceId);
    
    if (!resource) {
      return res.status(404).json({ error: 'Resource not found' });
    }
    
    const files = uploadedFilesStore[resourceId] || [];
    if (files.length === 0) {
      return res.status(400).json({ 
        error: 'Please upload at least one file before submitting',
        require_files: true 
      });
    }
    
    // 记录重提信息
    const isResubmit = resource.status === 'Rejected';
    if (isResubmit) {
      resource.resubmit_count = (resource.resubmit_count || 0) + 1;
      resource.last_resubmit_time = new Date().toISOString();
      
      if (!resource.rejection_history) {
        resource.rejection_history = [];
      }
      resource.rejection_history.push({
        resubmit_count: resource.resubmit_count,
        resubmit_time: resource.last_resubmit_time,
        previous_status: resource.status
      });
    }
    
    resource.status = 'Pending Review';
    resource.last_submitted_time = new Date().toISOString();
    resource.last_updated_time = new Date().toISOString();
    
    console.log('Resource submitted:', resource);
    res.json({ 
      message: 'Resource submitted for review', 
      resource: resource,
      is_resubmit: isResubmit,
      resubmit_count: resource.resubmit_count
    });
  } catch (error) {
    console.error('Submit error:', error);
    res.status(500).json({ error: error.message });
  }
};

// 修改 getResourceById，返回重提信息
const getResourceById = async (req, res) => {
  try {
    const resourceId = parseInt(req.params.id);
    const resource = tempResources.find(r => r.id === resourceId || r.resource_id === resourceId);
    
    if (!resource) {
      return res.status(404).json({ error: 'Resource not found' });
    }
    
    const files = uploadedFilesStore[resourceId] || [];
    
    const resourceWithFiles = {
      ...resource,
      files: files,
      file_count: files.length,
      resubmit_count: resource.resubmit_count || 0,
      last_resubmit_time: resource.last_resubmit_time || null,
      rejection_history: resource.rejection_history || []
    };
    
    console.log(`Returning resource ${resourceId} with ${files.length} files`);
    res.json(resourceWithFiles);
  } catch (error) {
    console.error('Get resource error:', error);
    res.status(500).json({ error: error.message });
  }
};

const saveDraft = async (req, res) => {
  try {
    console.log('=== Save Draft Request ===');
    console.log('Resource ID:', req.params.id);
    console.log('Request body:', req.body);
    
    const resourceId = parseInt(req.params.id);
    
    // 查找资源（支持 id 和 resource_id）
    let resource = tempResources.find(r => r.id === resourceId);
    if (!resource) {
      resource = tempResources.find(r => r.resource_id === resourceId);
    }
    
    if (!resource) {
      console.log('Resource not found. Available resources:', 
        tempResources.map(r => ({ id: r.id, resource_id: r.resource_id, title: r.title })));
      return res.status(404).json({ error: 'Resource not found' });
    }
    
    // 更新资源字段
    if (req.body.title !== undefined) resource.title = req.body.title;
    if (req.body.description !== undefined) resource.description = req.body.description;
    if (req.body.category_ids !== undefined) resource.category_ids = req.body.category_ids;
    if (req.body.place !== undefined) resource.place = req.body.place;
    if (req.body.copyright_declaration !== undefined) resource.copyright_declaration = req.body.copyright_declaration;
    if (req.body.usage_declaration !== undefined) resource.usage_declaration = req.body.usage_declaration;
    if (req.body.tag_names !== undefined) resource.tag_names = req.body.tag_names;
    
    resource.last_updated_time = new Date().toISOString();
    
    console.log('Draft saved successfully:', resource);
    res.json({ 
      message: 'Draft saved successfully', 
      resource: resource 
    });
  } catch (error) {
    console.error('Save draft error:', error);
    res.status(500).json({ error: error.message });
  }
};
const uploadFile = async (req, res) => {
  try {
    console.log('=== Upload Request ===');
    const resourceId = req.params.id;
    console.log('Resource ID:', resourceId);
    console.log('File:', req.file ? req.file.originalname : 'No file');
    
    if (!req.file) {
      return res.status(400).json({ error: '请选择要上传的文件' });
    }
    
    // 创建文件信息
    const fileInfo = {
      file_id: Date.now(),
      resource_id: parseInt(resourceId),
      original_filename: req.file.originalname,
      filename: req.file.filename,
      file_size: req.file.size,
      mime_type: req.file.mimetype,
      file_path: req.file.path,
      uploaded_at: new Date().toISOString()
    };
    
    // 存储文件
    if (!uploadedFilesStore[resourceId]) {
      uploadedFilesStore[resourceId] = [];
    }
    uploadedFilesStore[resourceId].push(fileInfo);
    
    console.log(`✅ File saved for resource ${resourceId}`);
    console.log(`📁 Total files: ${uploadedFilesStore[resourceId].length}`);
    console.log(`💾 All stored files:`, JSON.stringify(uploadedFilesStore, null, 2));
    
    res.json({ 
      success: true,
      message: 'File uploaded successfully', 
      file: fileInfo
    });
    
  } catch (error) {
    console.error('Upload error:', error);
    res.status(500).json({ error: error.message });
  }
};

const getFiles = async (req, res) => {
  try {
    const resourceId = req.params.id;
    console.log(`Getting files for resource: ${resourceId}`);
    console.log(`Current store:`, JSON.stringify(uploadedFilesStore, null, 2));
    
    const files = uploadedFilesStore[resourceId] || [];
    console.log(`Found ${files.length} files`);
    
    // 返回标准格式
    const formattedFiles = files.map(file => ({
      file_id: file.file_id,
      original_filename: file.original_filename,
      file_size: file.file_size,
      uploaded_at: file.uploaded_at,
      filename: file.filename,
      mime_type: file.mime_type
    }));
    
    res.json(formattedFiles);
  } catch (error) {
    console.error('Get files error:', error);
    res.status(500).json({ error: error.message });
  }
};


const deleteFile = async (req, res) => {
  try {
    const resourceId = req.params.id;
    const fileId = parseInt(req.params.fileId);
    const fs = require('fs');
    
    console.log(`Deleting file ${fileId} from resource ${resourceId}`);
    
    if (!uploadedFilesStore[resourceId]) {
      return res.status(404).json({ error: 'File not found' });
    }
    
    const fileIndex = uploadedFilesStore[resourceId].findIndex(f => f.file_id === fileId);
    if (fileIndex === -1) {
      return res.status(404).json({ error: 'File not found' });
    }
    
    const file = uploadedFilesStore[resourceId][fileIndex];
    
    // 删除物理文件
    if (file.file_path && fs.existsSync(file.file_path)) {
      fs.unlinkSync(file.file_path);
      console.log('Physical file deleted:', file.file_path);
    }
    
    // 从数组中删除
    uploadedFilesStore[resourceId].splice(fileIndex, 1);
    
    res.json({ message: 'File deleted successfully' });
  } catch (error) {
    console.error('Delete file error:', error);
    res.status(500).json({ error: error.message });
  }
};

const deleteResource = async (req, res) => {
  try {
    const resourceId = parseInt(req.params.id);
    const index = tempResources.findIndex(r => r.id === resourceId);
    
    if (index === -1) {
      return res.status(404).json({ error: 'Resource not found' });
    }
    
    // 删除关联的文件
    if (uploadedFilesStore[resourceId]) {
      const fs = require('fs');
      uploadedFilesStore[resourceId].forEach(file => {
        if (file.file_path && fs.existsSync(file.file_path)) {
          fs.unlinkSync(file.file_path);
        }
      });
      delete uploadedFilesStore[resourceId];
    }
    
    tempResources.splice(index, 1);
    res.json({ message: 'Resource deleted successfully' });
  } catch (error) {
    console.error('Delete resource error:', error);
    res.status(500).json({ error: error.message });
  }
};

const getCategories = async (req, res) => {
  try {
    const categories = [
      { id: 1, category_topic: 'Culture', category_name: '文化' },
      { id: 2, category_topic: 'History', category_name: '历史' },
      { id: 3, category_topic: 'Art', category_name: '艺术' },
      { id: 4, category_topic: 'Science', category_name: '科学' },
      { id: 5, category_topic: 'Technology', category_name: '技术' }
    ];
    console.log('Sending categories:', categories);
    res.json(categories);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
};

const getTags = async (req, res) => {
  res.json([]);
};

module.exports = {
  createResource,
  getMyResources,
  getResourceById,
  updateResource,
  submitForReview,
  saveDraft,  
  uploadFile,
  getFiles,
  deleteFile,
  deleteResource,
  getCategories,
  getTags
};
// 调试端点 - 查看所有存储的文件
const debugStore = (req, res) => {
  console.log('=== DEBUG STORE ===');
  console.log('uploadedFilesStore:', JSON.stringify(uploadedFilesStore, null, 2));
  res.json({
    store: uploadedFilesStore,
    keys: Object.keys(uploadedFilesStore),
    resource4Files: uploadedFilesStore['4'] || []
  });
};