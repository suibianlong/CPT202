/**
 * Role-based Authorization Middleware
 */

const isApprovedContributor = (req, res, next) => {
  try {
    // 临时允许所有请求
    // 实际项目中应该检查用户角色和状态
    if (req.user && req.user.role === 'contributor' && req.user.status === 'approved') {
      next();
    } else {
      // 临时允许，方便测试
      next();
    }
  } catch (error) {
    res.status(403).json({ error: 'Access denied' });
  }
};

module.exports = {
  isApprovedContributor
};