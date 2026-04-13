/**
 * Resource Routes - #4 Responsible
 * Contributor-side resource API endpoints
 */

const express = require('express')
const multer = require('multer')
const path = require('path')
const router = express.Router()
const resourceController = require('../controllers/resourceController')
const authMiddleware = require('../middleware/auth')
const roleMiddleware = require('../middleware/role')

// Configure multer for file upload
const storage = multer.diskStorage({
  destination: (req, file, cb) => {
    cb(null, path.join(__dirname, '../../uploads/temp'))
  },
  filename: (req, file, cb) => {
    const uniqueSuffix = Date.now() + '-' + Math.round(Math.random() * 1E9)
    cb(null, uniqueSuffix + '-' + file.originalname)
  }
})

const upload = multer({
  storage,
  limits: {
    fileSize: 50 * 1024 * 1024 // 50MB limit
  },
  fileFilter: (req, file, cb) => {
    // Allow common file types
    const allowedTypes = [
      'image/jpeg',
      'image/png',
      'image/gif',
      'image/webp',
      'application/pdf',
      'application/msword',
      'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
      'application/vnd.ms-excel',
      'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
      'text/plain',
      'video/mp4',
      'audio/mpeg'
    ]

    if (allowedTypes.includes(file.mimetype)) {
      cb(null, true)
    } else {
      cb(new Error('File type not allowed'), false)
    }
  }
})

// All routes require authentication
router.use(authMiddleware.isLoggedIn)

// Only approved contributors can operate
router.use(roleMiddleware.isApprovedContributor)

/**
 * @route   POST /api/resources
 * @desc    Create new resource (draft)
 * @access  Private (Approved Contributor)
 */
router.post('/', resourceController.createResource)

/**
 * @route   GET /api/resources/my
 * @desc    Get current user's submissions
 * @access  Private (Approved Contributor)
 */
router.get('/my', resourceController.getMyResources)

/**
 * @route   GET /api/resources/categories
 * @desc    Get all categories
 * @access  Private
 */
router.get('/categories', resourceController.getCategories)

/**
 * @route   GET /api/resources/tags
 * @desc    Get all tags
 * @access  Private
 */
router.get('/tags', resourceController.getTags)

/**
 * @route   GET /api/resources/:id
 * @desc    Get resource detail
 * @access  Private (Contributor or Admin)
 */
router.get('/:id', resourceController.getResourceById)

/**
 * @route   PUT /api/resources/:id
 * @desc    Update resource
 * @access  Private (Contributor - draft/rejected only)
 */
router.put('/:id', resourceController.updateResource)

/**
 * @route   POST /api/resources/:id/submit
 * @desc    Submit for review (requires at least 1 file)
 * @access  Private (Approved Contributor)
 */
router.post('/:id/submit', resourceController.submitForReview)

/**
 * @route   POST /api/resources/:id/draft
 * @desc    Save draft
 * @access  Private (Approved Contributor)
 */
router.post('/:id/draft', resourceController.saveDraft)

/**
 * @route   POST /api/resources/:id/upload
 * @desc    Upload file for resource
 * @access  Private (Approved Contributor - draft/rejected only)
 */
router.post('/:id/upload', upload.single('file'), resourceController.uploadFile)

/**
 * @route   GET /api/resources/:id/files
 * @desc    Get files for resource
 * @access  Private (Contributor or Admin)
 */
router.get('/:id/files', resourceController.getFiles)

/**
 * @route   DELETE /api/resources/:id/files/:fileId
 * @desc    Delete file
 * @access  Private (Contributor - draft/rejected only)
 */
router.delete('/:id/files/:fileId', resourceController.deleteFile)

/**
 * @route   DELETE /api/resources/:id
 * @desc    Delete resource
 * @access  Private (Contributor - draft only)
 */
router.delete('/:id', resourceController.deleteResource)

module.exports = router
