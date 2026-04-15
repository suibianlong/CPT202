const { sequelize } = require('../config/database');
const { DataTypes } = require('sequelize');

// 定义 Resource 模型
const Resource = sequelize.define('Resource', {
  id: {
    type: DataTypes.INTEGER,
    primaryKey: true,
    autoIncrement: true
  },
  resource_id: {
    type: DataTypes.INTEGER,
    field: 'resource_id'
  },
  title: {
    type: DataTypes.STRING,
    allowNull: true
  },
  description: {
    type: DataTypes.TEXT,
    allowNull: true
  },
  status: {
    type: DataTypes.STRING,
    defaultValue: 'Draft'
  },
  user_id: {
    type: DataTypes.INTEGER,
    allowNull: true
  },
  created_time: {
    type: DataTypes.DATE,
    defaultValue: DataTypes.NOW
  },
  last_updated_time: {
    type: DataTypes.DATE,
    defaultValue: DataTypes.NOW
  },
  submitted_time: {
    type: DataTypes.DATE,
    allowNull: true
  }
}, {
  tableName: 'resources',
  timestamps: false
});

// 定义 Category 模型
const Category = sequelize.define('Category', {
  id: {
    type: DataTypes.INTEGER,
    primaryKey: true,
    autoIncrement: true
  },
  category_topic: {
    type: DataTypes.STRING,
    allowNull: false
  }
}, {
  tableName: 'categories',
  timestamps: false
});

// 定义 User 模型（简化版）
const User = sequelize.define('User', {
  id: {
    type: DataTypes.INTEGER,
    primaryKey: true,
    autoIncrement: true
  },
  username: {
    type: DataTypes.STRING,
    allowNull: true
  },
  email: {
    type: DataTypes.STRING,
    allowNull: true
  },
  role: {
    type: DataTypes.STRING,
    defaultValue: 'contributor'
  },
  status: {
    type: DataTypes.STRING,
    defaultValue: 'approved'
  }
}, {
  tableName: 'users',
  timestamps: false
});

// 同步数据库（创建表）
sequelize.sync({ alter: true }).then(() => {
  console.log('✅ Database synchronized');
}).catch(err => {
  console.error('❌ Database sync error:', err.message);
});

module.exports = {
  sequelize,
  Resource,
  Category,
  User
};