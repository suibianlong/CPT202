/**
 * Model Index - #4 Module
 * Export all models for easy importing
 */

const { sequelize } = require('../config/database')

const UserModel = require('./User')
const RoleModel = require('./Role')
const UserRoleModel = require('./UserRole')
const ContributorApprovalModel = require('./ContributorApproval')
const CategoryModel = require('./Category')
const TagModel = require('./Tag')
const ResourceModel = require('./Resource')
const ResourceCategoryModel = require('./ResourceCategory')
const ResourceTagModel = require('./ResourceTag')
const AttachedFileModel = require('./AttachedFile')
const ResourceSubmissionModel = require('./ResourceSubmission')
const ReviewRecordModel = require('./ReviewRecord')
const FeedbackModel = require('./Feedback')
const CommentModel = require('./Comment')

const User = UserModel(sequelize)
const Role = RoleModel(sequelize)
const UserRole = UserRoleModel(sequelize)
const ContributorApproval = ContributorApprovalModel(sequelize)
const Category = CategoryModel(sequelize)
const Tag = TagModel(sequelize)
const Resource = ResourceModel(sequelize)
const ResourceCategory = ResourceCategoryModel(sequelize)
const ResourceTag = ResourceTagModel(sequelize)
const AttachedFile = AttachedFileModel(sequelize)
const ResourceSubmission = ResourceSubmissionModel(sequelize)
const ReviewRecord = ReviewRecordModel(sequelize)
const Feedback = FeedbackModel(sequelize)
const Comment = CommentModel(sequelize)

User.associate({ Role, UserRole, ContributorApproval, Resource })
Role.associate({ User, UserRole })
UserRole.associate({ User, Role })
ContributorApproval.associate({ User })
Category.associate({ Resource })
Tag.associate({ Resource })
Resource.associate({ User, Category, Tag, AttachedFile, ResourceSubmission, ReviewRecord })
ResourceCategory.associate({ Resource, Category })
ResourceTag.associate({ Resource, Tag })
AttachedFile.associate({ Resource })
ResourceSubmission.associate({ Resource, User, ReviewRecord })
ReviewRecord.associate({ Resource, ResourceSubmission, User, Feedback })
Feedback.associate({ ReviewRecord, User })
Comment.associate({ Resource, User })

module.exports = {
  sequelize,
  User,
  Role,
  UserRole,
  ContributorApproval,
  Category,
  Tag,
  Resource,
  ResourceCategory,
  ResourceTag,
  AttachedFile,
  ResourceSubmission,
  ReviewRecord,
  Feedback,
  Comment
}
