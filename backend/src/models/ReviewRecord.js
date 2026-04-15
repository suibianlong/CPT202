/**
 * ReviewRecord Model - #4 Module
 * Records each review history of a resource submission
 */

const { DataTypes } = require('sequelize')

const ACTION = {
  APPROVE: 'APPROVE',
  REJECT: 'REJECT'
}

const REVIEW_STATUS = {
  APPROVED: 'APPROVED',
  REJECTED: 'REJECTED'
}

module.exports = (sequelize) => {
  const ReviewRecord = sequelize.define('ReviewRecord', {
    id: {
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
    submission_id: {
      type: DataTypes.BIGINT,
      allowNull: false,
      references: {
        model: 'resource_submission',
        key: 'submission_id'
      }
    },
    version_no: {
      type: DataTypes.INTEGER,
      allowNull: false,
      defaultValue: 1
    },
    reviewer_id: {
      type: DataTypes.BIGINT,
      allowNull: false,
      references: {
        model: 'users',
        key: 'user_id'
      }
    },
    action_description: {
      type: DataTypes.ENUM(...Object.values(ACTION)),
      allowNull: false
    },
    status: {
      type: DataTypes.ENUM(...Object.values(REVIEW_STATUS)),
      allowNull: false
    },
    feedback_comment: {
      type: DataTypes.TEXT,
      allowNull: true,
      defaultValue: null
    },
    reviewed_at: {
      type: DataTypes.DATE,
      allowNull: false,
      defaultValue: DataTypes.NOW
    },
    created_at: {
      type: DataTypes.DATE,
      allowNull: false,
      defaultValue: DataTypes.NOW
    }
  }, {
    tableName: 'review_records',
    timestamps: false,
    indexes: [
      { fields: ['resource_id'] },
      { fields: ['reviewer_id'] }
    ]
  })

  ReviewRecord.associate = (models) => {
    ReviewRecord.belongsTo(models.Resource, {
      foreignKey: 'resource_id',
      as: 'resource'
    })

    ReviewRecord.belongsTo(models.ResourceSubmission, {
      foreignKey: 'submission_id',
      as: 'submission'
    })

    ReviewRecord.belongsTo(models.User, {
      foreignKey: 'reviewer_id',
      as: 'reviewer'
    })

    ReviewRecord.hasMany(models.Feedback, {
      foreignKey: 'review_record_id',
      as: 'feedbacks'
    })
  }

  ReviewRecord.ACTION = ACTION
  ReviewRecord.STATUS = REVIEW_STATUS

  return ReviewRecord
}
