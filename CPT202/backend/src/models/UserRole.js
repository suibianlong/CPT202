/**
 * UserRole Model - #4 Module
 * Defines user-role relationship structure
 */

const { DataTypes } = require('sequelize')

module.exports = (sequelize) => {
  const UserRole = sequelize.define('UserRole', {
    id: {
      type: DataTypes.BIGINT,
      primaryKey: true,
      autoIncrement: true
    },
    user_id: {
      type: DataTypes.BIGINT,
      allowNull: false,
      references: {
        model: 'users',
        key: 'user_id'
      }
    },
    role_id: {
      type: DataTypes.INTEGER,
      allowNull: false,
      references: {
        model: 'roles',
        key: 'role_id'
      }
    }
  }, {
    tableName: 'user_role',
    timestamps: false
  })

  UserRole.associate = (models) => {
    UserRole.belongsTo(models.User, {
      foreignKey: 'user_id',
      as: 'user'
    })

    UserRole.belongsTo(models.Role, {
      foreignKey: 'role_id',
      as: 'role'
    })
  }

  return UserRole
}
