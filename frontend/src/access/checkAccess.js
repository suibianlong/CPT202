import ACCESS_ENUM from './accessEnum.js'

export const getAccessLevel = (user) => {
  if (!user) {
    return ACCESS_ENUM.NOT_LOGIN
  }
  
  if (user.isAdmin) {
    return ACCESS_ENUM.ADMIN
  }
  
  if (user.isReviewer) {
    return ACCESS_ENUM.REVIEWER
  }
  
  if (user.isContributor) {
    return ACCESS_ENUM.CONTRIBUTOR
  }
  
  return ACCESS_ENUM.USER
}

export const hasAccess = (user, requiredAccess) => {
  const userLevel = getAccessLevel(user)
  return userLevel >= requiredAccess
}

export default { getAccessLevel, hasAccess }