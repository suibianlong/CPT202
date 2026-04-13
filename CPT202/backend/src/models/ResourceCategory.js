/**
 * ResourceCategory Model - #4 Module
 * Defines resource-category relationship
 */

const { DataTypes } = require('sequelize')

module.exports = (sequelize) => {
  const ResourceCategory = sequelize.define('ResourceCategory', {
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
    category_id: {
      type: DataTypes.BIGINT,
      allowNull: false,
      references: {
        model: 'categories',
        key: 'category_id'
      }
    }
  }, {
    tableName: 'resource_category',
    timestamps: false
  })

  ResourceCategory.associate = (models) => {
    ResourceCategory.belongsTo(models.Resource, {
      foreignKey: 'resource_id',
      as: 'resource'
    })

    ResourceCategory.belongsTo(models.Category, {
      foreignKey: 'category_id',
      as: 'category'
    })
  }

  return ResourceCategory
}
