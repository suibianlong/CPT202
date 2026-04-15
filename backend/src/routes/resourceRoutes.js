const express = require('express')
const router = express.Router()
const multer = require('multer')
const path = require('path')
const fs = require('fs')
const resourceController = require('../controllers/resourceController')
const versionController = require('../controllers/versionController')

// 确保上传目录存在
const uploadDir = path.join(__dirname, '../../uploads/temp')
if (!fs.existsSync(uploadDir)) {
  fs.mkdirSync(uploadDir, { recursive: true })
}

// 配置文件上传
const storage = multer.diskStorage({
  destination: (req, file, cb) => {
    cb(null, uploadDir)
  },
  filename: (req, file, cb) => {
    const uniqueSuffix = Date.now() + '-' + Math.round(Math.random() * 1E9)
    cb(null, uniqueSuffix + '-' + file.originalname)
  }
})

const upload = multer({ 
  storage: storage,
  limits: { fileSize: 50 * 1024 * 1024 }
})

// ========== 调试路由（放在最前面）==========
router.get('/debug/store', (req, res) => {
  // 直接访问 resourceController 中的变量
  res.json({ 
    message: 'Debug endpoint - check backend console for store',
    note: 'Check server terminal for uploadedFilesStore content'
  });
});

// ========== 测试路由 ==========
router.get('/test', (req, res) => {
  res.json({ message: 'API is working!', timestamp: new Date().toISOString() })
})

// ========== 静态路由（必须在动态路由之前）==========
router.get('/my', resourceController.getMyResources)
router.get('/categories', resourceController.getCategories)
router.get('/tags', resourceController.getTags)

// ========== 动态路由（带参数的路由）==========
router.get('/:id', resourceController.getResourceById)
router.put('/:id', resourceController.updateResource)
router.delete('/:id', resourceController.deleteResource)
router.post('/:id/draft', resourceController.saveDraft)
router.post('/:id/submit', resourceController.submitForReview)

// ========== 文件路由 ==========
router.post('/:id/upload', upload.single('file'), resourceController.uploadFile)
router.get('/:id/files', resourceController.getFiles)
router.delete('/:id/files/:fileId', resourceController.deleteFile)

// ========== 版本历史路由 ==========
router.post('/:id/versions', versionController.saveVersion)
router.get('/:id/versions', versionController.getVersions)
router.get('/:id/versions/:versionNumber', versionController.getVersion)
router.post('/:id/rollback/:versionNumber', versionController.rollbackToVersion)

// ========== 创建资源 ==========
router.post('/', resourceController.createResource)

module.exports = router