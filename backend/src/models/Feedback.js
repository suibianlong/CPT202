/**
 * Feedback Model - #4 Module
 * Records feedback files uploaded during review
 */

const { DataTypes } = require('sequelize')

module.exports = (sequelize) => {
  const Feedback = sequelize.define('Feedback', {
    feedback_id: {
      type: DataTypes.BIGINT,
      primaryKey: true,
      autoIncrement: true
    },
    review_record_id: {
      type: DataTypes.BIGINT,
      allowNull: true,
      references: {
        model: 'review_records',
        key: 'id'
      }
    },
    user_id: {
      type: DataTypes.BIGINT,
      allowNull: false,
      references: {
        model: 'users',
        key: 'user_id'
      }
    },
    file_name: {
      type: DataTypes.STRING(255),
      allowNull: false,
      validate: {
        notEmpty: true
      }
    },
    file_url: {
      type: DataTypes.STRING(500),
      allowNull: true,
      defaultValue: null
    },
    file_type: {
      type: DataTypes.STRING(50),
      allowNull: true,
      defaultValue: null
    },
    file_size: {
      type: DataTypes.BIGINT,
      allowNull: false,
      defaultValue: 0
    },
    uploaded_at: {
      type: DataTypes.DATE,
      allowNull: false,
      defaultValue: DataTypes.NOW
    }
  }, {
    tableName: 'feedback',
    timestamps: false,
    indexes: [
      { fields: ['review_record_id'] },
      { fields: ['user_id'] }
    ]
  })

  Feedback.associate = (models) => {
    Feedback.belongsTo(models.ReviewRecord, {
      foreignKey: 'review_record_id',
      as: 'reviewRecord'
    })

    Feedback.belongsTo(models.User, {
      foreignKey: 'user_id',
      as: 'uploader'
    })
  }

  return Feedback
}
