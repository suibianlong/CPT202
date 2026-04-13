/**
 * Resource Model - #4 Responsible
 * Defines the Resource entity structure based on database schema
 */

const { DataTypes } = require('sequelize')

const STATUS = {
  DRAFT: 'Draft',
  PENDING_REVIEW: 'Pending Review',
  REJECTED: 'Rejected',
  APPROVED: 'Approved',
  ARCHIVED: 'Archived'
}

const TYPE = {
  VIDEO: 'video',
  IMAGE: 'image',
  DOCUMENT: 'document',
  AUDIO: 'audio'
}

module.exports = (sequelize) => {
  const Resource = sequelize.define('Resource', {
    resource_id: {
      type: DataTypes.BIGINT,
      primaryKey: true,
      autoIncrement: true
    },
    contributor_id: {
      type: DataTypes.BIGINT,
      allowNull: false,
      references: {
        model: 'users',
        key: 'user_id'
      }
    },
    title: {
      type: DataTypes.STRING(255),
      allowNull: false,
      validate: {
        notEmpty: true,
        len: [1, 255]
      }
    },
    description: {
      type: DataTypes.TEXT,
      allowNull: false,
      validate: {
        notEmpty: true
      }
    },
    place: {
      type: DataTypes.STRING(255),
      allowNull: true,
      defaultValue: null
    },
    preview_image: {
      type: DataTypes.STRING(500),
      allowNull: true,
      defaultValue: null
    },
    type: {
      type: DataTypes.ENUM(...Object.values(TYPE)),
      allowNull: true,
      defaultValue: null
    },
    status: {
      type: DataTypes.ENUM(...Object.values(STATUS)),
      allowNull: false,
      defaultValue: STATUS.DRAFT
    },
    approved_at: {
      type: DataTypes.DATE,
      allowNull: true,
      defaultValue: null
    },
    archived_at: {
      type: DataTypes.DATE,
      allowNull: true,
      defaultValue: null
    },
    created_time: {
      type: DataTypes.DATE,
      allowNull: false,
      defaultValue: DataTypes.NOW
    },
    last_updated_time: {
      type: DataTypes.DATE,
      allowNull: false,
      defaultValue: DataTypes.NOW
    },
    last_submitted_time: {
      type: DataTypes.DATE,
      allowNull: true,
      defaultValue: null
    },
    last_published_time: {
      type: DataTypes.DATE,
      allowNull: true,
      defaultValue: null
    }
  }, {
    tableName: 'resources',
    timestamps: false,
    indexes: [
      { fields: ['title'] },
      { fields: ['status'] },
      { fields: ['contributor_id', 'status'] },
      { fields: ['created_time'] }
    ]
  })

  Resource.associate = (models) => {
    Resource.belongsTo(models.User, {
      foreignKey: 'contributor_id',
      as: 'contributor'
    })

    Resource.belongsToMany(models.Category, {
      through: models.ResourceCategory,
      foreignKey: 'resource_id',
      otherKey: 'category_id',
      as: 'categories'
    })

    Resource.belongsToMany(models.Tag, {
      through: models.ResourceTag,
      foreignKey: 'resource_id',
      otherKey: 'tag_id',
      as: 'tags'
    })

    Resource.hasMany(models.AttachedFile, {
      foreignKey: 'resource_id',
      as: 'attachedFiles'
    })

    Resource.hasMany(models.ResourceSubmission, {
      foreignKey: 'resource_id',
      as: 'submissions'
    })

    Resource.hasMany(models.ReviewRecord, {
      foreignKey: 'resource_id',
      as: 'reviewRecords'
    })
  }

  Resource.STATUS = STATUS
  Resource.TYPE = TYPE

  return Resource
}
