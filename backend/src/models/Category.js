/**
 * Category Model - #4 Module
 * Defines resource category structure
 * Updated: category_type + category_topic composite unique key
 */

const { DataTypes } = require('sequelize')

const CATEGORY_STATUS = {
  ACTIVE: 'ACTIVE',
  INACTIVE: 'INACTIVE'
}

module.exports = (sequelize) => {
  const Category = sequelize.define('Category', {
    category_id: {
      type: DataTypes.BIGINT,
      primaryKey: true,
      autoIncrement: true
    },
    category_type: {
      type: DataTypes.STRING(20),
      allowNull: false,
      validate: {
        notEmpty: true
      }
    },
    category_topic: {
      type: DataTypes.STRING(20),
      allowNull: false,
      validate: {
        notEmpty: true
      }
    },
    description: {
      type: DataTypes.TEXT,
      allowNull: true,
      defaultValue: null
    },
    status: {
      type: DataTypes.ENUM(...Object.values(CATEGORY_STATUS)),
      allowNull: false,
      defaultValue: CATEGORY_STATUS.ACTIVE
    },
    usage_count: {
      type: DataTypes.INTEGER,
      allowNull: false,
      defaultValue: 0
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
    }
  }, {
    tableName: 'categories',
    timestamps: false,
    indexes: [
      { unique: true, fields: ['category_type', 'category_topic'] }
    ]
  })

  Category.associate = (models) => {
    Category.belongsToMany(models.Resource, {
      through: models.ResourceCategory,
      foreignKey: 'category_id',
      otherKey: 'resource_id',
      as: 'resources'
    })
  }

  Category.STATUS = CATEGORY_STATUS

  return Category
}
