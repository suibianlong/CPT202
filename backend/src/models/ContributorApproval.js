/**
 * ContributorApproval Model - #4 Module
 * Records contributor application/approval information
 */

const { DataTypes } = require('sequelize')

const APPROVAL_STATUS = {
  PENDING: 'pending',
  APPROVED: 'approved',
  REJECTED: 'rejected'
}

module.exports = (sequelize) => {
  const ContributorApproval = sequelize.define('ContributorApproval', {
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
    approval_status: {
      type: DataTypes.ENUM(...Object.values(APPROVAL_STATUS)),
      allowNull: false,
      defaultValue: APPROVAL_STATUS.PENDING
    },
    approved_at: {
      type: DataTypes.DATE,
      allowNull: true,
      defaultValue: null
    },
    review_note: {
      type: DataTypes.TEXT,
      allowNull: true,
      defaultValue: null
    },
    created_time: {
      type: DataTypes.DATE,
      allowNull: false,
      defaultValue: DataTypes.NOW
    }
  }, {
    tableName: 'contributor_approvals',
    timestamps: false,
    indexes: [
      { fields: ['user_id'] },
      { fields: ['approval_status'] }
    ]
  })

  ContributorApproval.associate = (models) => {
    ContributorApproval.belongsTo(models.User, {
      foreignKey: 'user_id',
      as: 'user'
    })
  }

  ContributorApproval.STATUS = APPROVAL_STATUS

  return ContributorApproval
}
