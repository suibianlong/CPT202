/**
 * Authentication Middleware
 */

// 临时版本（等你有真实的认证系统后再替换）
const isLoggedIn = async (req, res, next) => {
  try {
    // 临时跳过认证，让你可以先测试
    // 实际项目中应该验证 JWT token
    req.user = {
      id: 1,
      role: 'contributor',
      status: 'approved'
    };
    next();
  } catch (error) {
    res.status(401).json({ error: 'Authentication required' });
  }
};

module.exports = {
  isLoggedIn
};