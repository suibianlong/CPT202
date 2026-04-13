/**
 * Comment Model - #4 Module
 * Records user comments on resources
 */

const { DataTypes } = require('sequelize')

module.exports = (sequelize) => {
  const Comment = sequelize.define('Comment', {
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
    user_id: {
      type: DataTypes.BIGINT,
      allowNull: false,
      references: {
        model: 'users',
        key: 'user_id'
      }
    },
    content: {
      type: DataTypes.TEXT,
      allowNull: false,
      validate: {
        notEmpty: true
      }
    },
    created_at: {
      type: DataTypes.DATE,
      allowNull: false,
      defaultValue: DataTypes.NOW
    }
  }, {
    tableName: 'comments',
    timestamps: false,
    indexes: [
      { fields: ['resource_id'] }
    ]
  })

  Comment.associate = (models) => {
    Comment.belongsTo(models.Resource, {
      foreignKey: 'resource_id',
      as: 'resource'
    })

    Comment.belongsTo(models.User, {
      foreignKey: 'user_id',
      as: 'user'
    })
  }

  return Comment
}
