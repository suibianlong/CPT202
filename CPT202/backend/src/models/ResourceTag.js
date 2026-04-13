/**
 * ResourceTag Model - #4 Module
 * Defines resource-tag relationship
 */

const { DataTypes } = require('sequelize')

module.exports = (sequelize) => {
  const ResourceTag = sequelize.define('ResourceTag', {
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
    tag_id: {
      type: DataTypes.BIGINT,
      allowNull: false,
      references: {
        model: 'tags',
        key: 'tag_id'
      }
    }
  }, {
    tableName: 'resource_tag',
    timestamps: false
  })

  ResourceTag.associate = (models) => {
    ResourceTag.belongsTo(models.Resource, {
      foreignKey: 'resource_id',
      as: 'resource'
    })

    ResourceTag.belongsTo(models.Tag, {
      foreignKey: 'tag_id',
      as: 'tag'
    })
  }

  return ResourceTag
}
