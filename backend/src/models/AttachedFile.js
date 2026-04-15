/**
 * AttachedFile Model - #4 Module
 * Defines resource attached file structure
 * Updated: original_filename length 500, uploaded_at renamed from uploaded_time
 */

const { DataTypes } = require('sequelize')

module.exports = (sequelize) => {
  const AttachedFile = sequelize.define('AttachedFile', {
    file_id: {
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
    original_filename: {
      type: DataTypes.STRING(500),
      allowNull: false,
      validate: {
        notEmpty: true
      }
    },
    stored_filename: {
      type: DataTypes.STRING(255),
      allowNull: false,
      unique: true
    },
    file_path: {
      type: DataTypes.STRING(500),
      allowNull: false,
      validate: {
        notEmpty: true
      }
    },
    file_type: {
      type: DataTypes.STRING(50),
      allowNull: true,
      defaultValue: null
    },
    file_size: {
      type: DataTypes.BIGINT,
      allowNull: false,
      defaultValue: 0,
      validate: {
        min: 0
      }
    },
    uploaded_at: {
      type: DataTypes.DATE,
      allowNull: false,
      defaultValue: DataTypes.NOW
    }
  }, {
    tableName: 'attached_file',
    timestamps: false,
    indexes: [
      { unique: true, fields: ['stored_filename'] },
      { fields: ['resource_id'] }
    ]
  })

  AttachedFile.associate = (models) => {
    AttachedFile.belongsTo(models.Resource, {
      foreignKey: 'resource_id',
      as: 'resource'
    })
  }

  return AttachedFile
}
