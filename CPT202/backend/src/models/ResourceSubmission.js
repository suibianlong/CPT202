/**
 * ResourceSubmission Model - #4 Module
 * Records each submission version of a resource
 */

const { DataTypes } = require('sequelize')

const STATUS_SNAPSHOT = {
  DRAFT: 'Draft',
  PENDING_REVIEW: 'Pending Review',
  REJECTED: 'Rejected',
  APPROVED: 'Approved',
  ARCHIVED: 'Archived'
}

module.exports = (sequelize) => {
  const ResourceSubmission = sequelize.define('ResourceSubmission', {
    submission_id: {
      type: DataTypes.BIGINT,
      primaryKey: true,
      autoIncrement: true
    },
    resource_id: {
      type: DataTypes.BIGINT,
      allowNull: false,
      references: {
        model: 'resources',
        key: 'resource_id'
      }
    },
    version_no: {
      type: DataTypes.INTEGER,
      allowNull: false,
      defaultValue: 1
    },
    submitted_by: {
      type: DataTypes.BIGINT,
      allowNull: false,
      references: {
        model: 'users',
        key: 'user_id'
      }
    },
    submitted_at: {
      type: DataTypes.DATE,
      allowNull: false,
      defaultValue: DataTypes.NOW
    },
    submission_note: {
      type: DataTypes.TEXT,
      allowNull: true,
      defaultValue: null
    },
    status_snapshot: {
      type: DataTypes.ENUM(...Object.values(STATUS_SNAPSHOT)),
      allowNull: false,
      defaultValue: STATUS_SNAPSHOT.PENDING_REVIEW
    },
    created_at: {
      type: DataTypes.DATE,
      allowNull: false,
      defaultValue: DataTypes.NOW
    }
  }, {
    tableName: 'resource_submission',
    timestamps: false,
    indexes: [
      { fields: ['resource_id'] },
      { fields: ['submitted_by'] },
      { unique: true, fields: ['resource_id', 'version_no'] }
    ]
  })

  ResourceSubmission.associate = (models) => {
    ResourceSubmission.belongsTo(models.Resource, {
      foreignKey: 'resource_id',
      as: 'resource'
    })

    ResourceSubmission.belongsTo(models.User, {
      foreignKey: 'submitted_by',
      as: 'submitter'
    })

    ResourceSubmission.hasMany(models.ReviewRecord, {
      foreignKey: 'submission_id',
      as: 'reviewRecords'
    })
  }

  ResourceSubmission.STATUS_SNAPSHOT = STATUS_SNAPSHOT

  return ResourceSubmission
}
