/**
 * Resource Controller - #4 Responsible
 * Handles contributor-related resource operations
 */

const path = require('path')
const fs = require('fs')
const { Resource, Tag, ResourceTag, AttachedFile, Category, ResourceCategory, sequelize } = require('../models')

// File upload directory
const UPLOAD_DIR = path.join(__dirname, '../../uploads')

/**
 * Create new resource (draft)
 * POST /api/resources
 */
exports.createResource = async (req, res) => {
  const t = await sequelize.transaction()

  try {
    const {
      title,
      place,
      description,
      copyright_declaration,
      usage_declaration,
      category_ids,
      tag_names
    } = req.body

    const resource = await Resource.create({
      contributor_id: req.user.user_id,
      title,
      place,
      description,
      copyright_declaration,
      usage_declaration,
      status: 'Draft',
      created_time: new Date(),
      last_updated_time: new Date()
    }, { transaction: t })

    if (category_ids && category_ids.length > 0) {
      const categoryRelations = category_ids.map(catId => ({
        resource_id: resource.resource_id,
        category_id: catId
      }))
      await ResourceCategory.bulkCreate(categoryRelations, { transaction: t })
    }

    if (tag_names && tag_names.length > 0) {
      for (const tagName of tag_names) {
        const [tag] = await Tag.findOrCreate({
          where: { tag_name: tagName.trim() },
          defaults: { created_time: new Date() },
          transaction: t
        })
        await ResourceTag.create({
          resource_id: resource.resource_id,
          tag_id: tag.tag_id
        }, { transaction: t })
      }
    }

    await t.commit()

    const createdResource = await Resource.findByPk(resource.resource_id, {
      include: [
        { model: Category, as: 'categories' },
        { model: Tag, as: 'tags' }
      ]
    })

    res.status(201).json(createdResource)
  } catch (error) {
    await t.rollback()
    console.error('Create resource error:', error)
    res.status(500).json({ error: error.message })
  }
}

/**
 * Get all resources by contributor
 * GET /api/resources/my
 */
exports.getMyResources = async (req, res) => {
  try {
    const { status } = req.query
    const query = {
      where: { contributor_id: req.user.user_id },
      include: [
        { model: Category, as: 'categories' },
        { model: Tag, as: 'tags' }
      ],
      order: [['last_updated_time', 'DESC']]
    }

    if (status) {
      query.where.status = status
    }

    const resources = await Resource.findAll(query)
    res.json(resources)
  } catch (error) {
    console.error('Get my resources error:', error)
    res.status(500).json({ error: error.message })
  }
}

/**
 * Get single resource by ID
 * GET /api/resources/:id
 */
exports.getResourceById = async (req, res) => {
  try {
    const resource = await Resource.findByPk(req.params.id, {
      include: [
        { model: Category, as: 'categories' },
        { model: Tag, as: 'tags' },
        { model: AttachedFile, as: 'attachedFiles' }
      ]
    })

    if (!resource) {
      return res.status(404).json({ error: 'Resource not found' })
    }

    if (resource.contributor_id !== req.user.user_id && req.user.role !== 'admin') {
      return res.status(403).json({ error: 'Access denied' })
    }

    res.json(resource)
  } catch (error) {
    console.error('Get resource by id error:', error)
    res.status(500).json({ error: error.message })
  }
}

/**
 * Update resource (draft or rejected only)
 * PUT /api/resources/:id
 */
exports.updateResource = async (req, res) => {
  const t = await sequelize.transaction()

  try {
    const resource = await Resource.findByPk(req.params.id)

    if (!resource) {
      await t.rollback()
      return res.status(404).json({ error: 'Resource not found' })
    }

    if (resource.contributor_id !== req.user.user_id) {
      await t.rollback()
      return res.status(403).json({ error: 'Access denied' })
    }

    if (!['Draft', 'Rejected'].includes(resource.status)) {
      await t.rollback()
      return res.status(400).json({ error: 'Cannot edit in this status' })
    }

    const {
      title,
      place,
      description,
      copyright_declaration,
      usage_declaration,
      category_ids,
      tag_names
    } = req.body

    await resource.update({
      title: title || resource.title,
      place: place !== undefined ? place : resource.place,
      description: description || resource.description,
      copyright_declaration: copyright_declaration || resource.copyright_declaration,
      usage_declaration: usage_declaration !== undefined ? usage_declaration : resource.usage_declaration,
      last_updated_time: new Date()
    }, { transaction: t })

    if (category_ids !== undefined) {
      await ResourceCategory.destroy({
        where: { resource_id: resource.resource_id },
        transaction: t
      })

      if (category_ids && category_ids.length > 0) {
        const categoryRelations = category_ids.map(catId => ({
          resource_id: resource.resource_id,
          category_id: catId
        }))
        await ResourceCategory.bulkCreate(categoryRelations, { transaction: t })
      }
    }

    if (tag_names !== undefined) {
      await ResourceTag.destroy({
        where: { resource_id: resource.resource_id },
        transaction: t
      })

      if (tag_names && tag_names.length > 0) {
        for (const tagName of tag_names) {
          const [tag] = await Tag.findOrCreate({
            where: { tag_name: tagName.trim() },
            defaults: { created_time: new Date() },
            transaction: t
          })
          await ResourceTag.create({
            resource_id: resource.resource_id,
            tag_id: tag.tag_id
          }, { transaction: t })
        }
      }
    }

    await t.commit()

    const updatedResource = await Resource.findByPk(resource.resource_id, {
      include: [
        { model: Category, as: 'categories' },
        { model: Tag, as: 'tags' }
      ]
    })

    res.json(updatedResource)
  } catch (error) {
    await t.rollback()
    console.error('Update resource error:', error)
    res.status(500).json({ error: error.message })
  }
}

/**
 * Submit for review
 * POST /api/resources/:id/submit
 * Requirement: Must have at least 1 attached file before submitting
 */
exports.submitForReview = async (req, res) => {
  try {
    const resource = await Resource.findByPk(req.params.id)

    if (!resource) {
      return res.status(404).json({ error: 'Resource not found' })
    }

    if (!['Draft', 'Rejected'].includes(resource.status)) {
      return res.status(400).json({ error: 'Cannot submit in this status' })
    }

    if (!resource.title || !resource.description || !resource.copyright_declaration) {
      return res.status(400).json({ error: 'Please fill in all required fields' })
    }

    // Check if at least 1 file has been uploaded
    const fileCount = await AttachedFile.count({
      where: { resource_id: resource.resource_id }
    })

    if (fileCount === 0) {
      return res.status(400).json({
        error: 'Please upload at least 1 file before submitting',
        require_files: true
      })
    }

    await resource.update({
      status: 'Pending Review',
      last_submitted_time: new Date(),
      last_updated_time: new Date()
    })

    res.json({
      message: 'Submitted successfully, waiting for review',
      resource
    })
  } catch (error) {
    console.error('Submit for review error:', error)
    res.status(500).json({ error: error.message })
  }
}

/**
 * Save draft
 * POST /api/resources/:id/draft
 */
exports.saveDraft = async (req, res) => {
  const t = await sequelize.transaction()

  try {
    const resource = await Resource.findByPk(req.params.id)

    if (!resource) {
      const {
        title,
        place,
        description,
        copyright_declaration,
        usage_declaration,
        category_ids,
        tag_names
      } = req.body

      const newResource = await Resource.create({
        contributor_id: req.user.user_id,
        title,
        place,
        description,
        copyright_declaration,
        usage_declaration,
        status: 'Draft',
        created_time: new Date(),
        last_updated_time: new Date()
      }, { transaction: t })

      if (category_ids && category_ids.length > 0) {
        const categoryRelations = category_ids.map(catId => ({
          resource_id: newResource.resource_id,
          category_id: catId
        }))
        await ResourceCategory.bulkCreate(categoryRelations, { transaction: t })
      }

      if (tag_names && tag_names.length > 0) {
        for (const tagName of tag_names) {
          const [tag] = await Tag.findOrCreate({
            where: { tag_name: tagName.trim() },
            defaults: { created_time: new Date() },
            transaction: t
          })
          await ResourceTag.create({
            resource_id: newResource.resource_id,
            tag_id: tag.tag_id
          }, { transaction: t })
        }
      }

      await t.commit()

      const createdResource = await Resource.findByPk(newResource.resource_id, {
        include: [
          { model: Category, as: 'categories' },
          { model: Tag, as: 'tags' }
        ]
      })

      return res.json({ message: 'Draft saved successfully', resource: createdResource })
    }

    const {
      title,
      place,
      description,
      copyright_declaration,
      usage_declaration,
      category_ids,
      tag_names
    } = req.body

    await resource.update({
      title: title !== undefined ? title : resource.title,
      place: place !== undefined ? place : resource.place,
      description: description !== undefined ? description : resource.description,
      copyright_declaration: copyright_declaration !== undefined ? copyright_declaration : resource.copyright_declaration,
      usage_declaration: usage_declaration !== undefined ? usage_declaration : resource.usage_declaration,
      last_updated_time: new Date()
    }, { transaction: t })

    if (category_ids !== undefined) {
      await ResourceCategory.destroy({
        where: { resource_id: resource.resource_id },
        transaction: t
      })

      if (category_ids && category_ids.length > 0) {
        const categoryRelations = category_ids.map(catId => ({
          resource_id: resource.resource_id,
          category_id: catId
        }))
        await ResourceCategory.bulkCreate(categoryRelations, { transaction: t })
      }
    }

    if (tag_names !== undefined) {
      await ResourceTag.destroy({
        where: { resource_id: resource.resource_id },
        transaction: t
      })

      if (tag_names && tag_names.length > 0) {
        for (const tagName of tag_names) {
          const [tag] = await Tag.findOrCreate({
            where: { tag_name: tagName.trim() },
            defaults: { created_time: new Date() },
            transaction: t
          })
          await ResourceTag.create({
            resource_id: resource.resource_id,
            tag_id: tag.tag_id
          }, { transaction: t })
        }
      }
    }

    await t.commit()

    const updatedResource = await Resource.findByPk(resource.resource_id, {
      include: [
        { model: Category, as: 'categories' },
        { model: Tag, as: 'tags' }
      ]
    })

    res.json({ message: 'Draft saved successfully', resource: updatedResource })
  } catch (error) {
    await t.rollback()
    console.error('Save draft error:', error)
    res.status(500).json({ error: error.message })
  }
}

/**
 * Delete resource (draft only)
 * DELETE /api/resources/:id
 */
exports.deleteResource = async (req, res) => {
  const t = await sequelize.transaction()

  try {
    const resource = await Resource.findByPk(req.params.id)

    if (!resource) {
      await t.rollback()
      return res.status(404).json({ error: 'Resource not found' })
    }

    if (resource.contributor_id !== req.user.user_id) {
      await t.rollback()
      return res.status(403).json({ error: 'Access denied' })
    }

    if (resource.status !== 'Draft') {
      await t.rollback()
      return res.status(400).json({ error: 'Can only delete draft resources' })
    }

    await ResourceTag.destroy({
      where: { resource_id: resource.resource_id },
      transaction: t
    })

    await ResourceCategory.destroy({
      where: { resource_id: resource.resource_id },
      transaction: t
    })

    await AttachedFile.destroy({
      where: { resource_id: resource.resource_id },
      transaction: t
    })

    await Resource.destroy({
      where: { resource_id: resource.resource_id },
      transaction: t
    })

    await t.commit()
    res.json({ message: 'Deleted successfully' })
  } catch (error) {
    await t.rollback()
    console.error('Delete resource error:', error)
    res.status(500).json({ error: error.message })
  }
}

/**
 * Get all categories
 * GET /api/resources/categories
 */
exports.getCategories = async (req, res) => {
  try {
    const categories = await Category.findAll({
      order: [['category_name', 'ASC']]
    })
    res.json(categories)
  } catch (error) {
    console.error('Get categories error:', error)
    res.status(500).json({ error: error.message })
  }
}

/**
 * Get all tags
 * GET /api/resources/tags
 */
exports.getTags = async (req, res) => {
  try {
    const tags = await Tag.findAll({
      order: [['tag_name', 'ASC']]
    })
    res.json(tags)
  } catch (error) {
    console.error('Get tags error:', error)
    res.status(500).json({ error: error.message })
  }
}

/**
 * Upload file for resource
 * POST /api/resources/:id/upload
 * Requires multipart/form-data with 'file' field
 */
exports.uploadFile = async (req, res) => {
  try {
    const resourceId = req.params.id

    const resource = await Resource.findByPk(resourceId)

    if (!resource) {
      return res.status(404).json({ error: 'Resource not found' })
    }

    if (resource.contributor_id !== req.user.user_id) {
      return res.status(403).json({ error: 'Access denied' })
    }

    if (!['Draft', 'Rejected'].includes(resource.status)) {
      return res.status(400).json({ error: 'Cannot upload files in this status' })
    }

    if (!req.file) {
      return res.status(400).json({ error: 'No file uploaded' })
    }

    const originalFilename = req.file.originalname
    const storedFilename = `${Date.now()}-${Math.random().toString(36).substr(2, 9)}${path.extname(req.file.originalname)}`
    const filePath = path.join(UPLOAD_DIR, resourceId.toString(), storedFilename)

    // Create directory if not exists
    const resourceUploadDir = path.join(UPLOAD_DIR, resourceId.toString())
    if (!fs.existsSync(resourceUploadDir)) {
      fs.mkdirSync(resourceUploadDir, { recursive: true })
    }

    // Move file to storage location
    fs.renameSync(req.file.path, filePath)

    // Save file record to database
    const attachedFile = await AttachedFile.create({
      resource_id: resourceId,
      original_filename: originalFilename,
      stored_filename: storedFilename,
      file_path: filePath,
      file_type: req.file.mimetype,
      file_size: req.file.size,
      uploaded_time: new Date()
    })

    // Update resource last_updated_time
    await resource.update({ last_updated_time: new Date() })

    res.status(201).json({
      message: 'File uploaded successfully',
      file: attachedFile
    })
  } catch (error) {
    console.error('Upload file error:', error)
    res.status(500).json({ error: error.message })
  }
}

/**
 * Get files for resource
 * GET /api/resources/:id/files
 */
exports.getFiles = async (req, res) => {
  try {
    const resourceId = req.params.id

    const resource = await Resource.findByPk(resourceId)

    if (!resource) {
      return res.status(404).json({ error: 'Resource not found' })
    }

    if (resource.contributor_id !== req.user.user_id) {
      return res.status(403).json({ error: 'Access denied' })
    }

    const files = await AttachedFile.findAll({
      where: { resource_id: resourceId },
      order: [['uploaded_time', 'DESC']]
    })

    res.json(files)
  } catch (error) {
    console.error('Get files error:', error)
    res.status(500).json({ error: error.message })
  }
}

/**
 * Delete file
 * DELETE /api/resources/:id/files/:fileId
 */
exports.deleteFile = async (req, res) => {
  try {
    const { id: resourceId, fileId } = req.params

    const resource = await Resource.findByPk(resourceId)

    if (!resource) {
      return res.status(404).json({ error: 'Resource not found' })
    }

    if (resource.contributor_id !== req.user.user_id) {
      return res.status(403).json({ error: 'Access denied' })
    }

    if (!['Draft', 'Rejected'].includes(resource.status)) {
      return res.status(400).json({ error: 'Cannot delete files in this status' })
    }

    const file = await AttachedFile.findByPk(fileId)

    if (!file || file.resource_id !== parseInt(resourceId)) {
      return res.status(404).json({ error: 'File not found' })
    }

    // Delete physical file
    if (fs.existsSync(file.file_path)) {
      fs.unlinkSync(file.file_path)
    }

    // Delete database record
    await file.destroy()

    // Update resource last_updated_time
    await resource.update({ last_updated_time: new Date() })

    res.json({ message: 'File deleted successfully' })
  } catch (error) {
    console.error('Delete file error:', error)
    res.status(500).json({ error: error.message })
  }
}
