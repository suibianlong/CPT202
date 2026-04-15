const { Sequelize } = require('sequelize');
const dotenv = require('dotenv');

dotenv.config();

// 临时配置 - 使用 SQLite 进行测试（不需要安装 MySQL）
const sequelize = new Sequelize({
  dialect: 'sqlite',
  storage: './database.sqlite',
  logging: false
});

// 如果使用 MySQL，取消下面的注释并注释上面的配置
// const sequelize = new Sequelize(
//   process.env.DB_NAME || 'cpt202_db',
//   process.env.DB_USER || 'root',
//   process.env.DB_PASSWORD || '',
//   {
//     host: process.env.DB_HOST || 'localhost',
//     dialect: 'mysql',
//     logging: false
//   }
// );

const testConnection = async () => {
  try {
    await sequelize.authenticate();
    console.log('✅ Database connection established successfully.');
  } catch (error) {
    console.error('❌ Unable to connect to database:', error.message);
  }
};

module.exports = {
  sequelize,
  testConnection
};