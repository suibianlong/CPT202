/**
 * Tag Model - #4 Module
 * Defines tag structure
 * Updated: added status and usage_count fields
 */

const { DataTypes } = require('sequelize')

const TAG_STATUS = {
  ACTIVE: 'ACTIVE',
  INACTIVE: 'INACTIVE'
}

module.exports = (sequelize) => {
  const Tag = sequelize.define('Tag', {
    tag_id: {
      type: DataTypes.BIGINT,
      primaryKey: true,
      autoIncrement: true
    },
    tag_name: {
      type: DataTypes.STRING(100),
      allowNull: false,
      unique: true,
      validate: {
        notEmpty: true,
        len: [1, 100]
      }
    },
    status: {
      type: DataTypes.ENUM(...Object.values(TAG_STATUS)),
      allowNull: false,
      defaultValue: TAG_STATUS.ACTIVE
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
    tableName: 'tags',
    timestamps: false,
    indexes: [
      { unique: true, fields: ['tag_name'] }
    ]
  })

  Tag.associate = (models) => {
    Tag.belongsToMany(models.Resource, {
      through: models.ResourceTag,
      foreignKey: 'tag_id',
      otherKey: 'resource_id',
      as: 'resources'
    })
  }

  Tag.STATUS = TAG_STATUS

  return Tag
}
