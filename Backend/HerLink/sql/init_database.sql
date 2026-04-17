-- 关闭外键检查，避免建表顺序报错
SET FOREIGN_KEY_CHECKS = 0;

-- 创建数据库（如果不存在）
DROP DATABASE IF EXISTS heritageResourcePlatform;
CREATE DATABASE heritageResourcePlatform CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE heritageResourcePlatform;

-- ----------------------------
-- 1. User 用户表
-- ----------------------------
CREATE TABLE `user` (
  `userId` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(100) NOT NULL UNIQUE COMMENT '用户名',
  `email` VARCHAR(255) NOT NULL UNIQUE COMMENT '邮箱',
  `passwordHash` VARCHAR(255) NOT NULL COMMENT '密码哈希',
  `role` ENUM('user','reviewer') NOT NULL COMMENT '角色',
  `isContributor` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否为贡献者 0=否 1=是',
  `bio` TEXT NULL COMMENT '个人简介',
  `createdAt` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `lastUpdatedAt` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ----------------------------
-- 2. Category 分类表
-- ----------------------------
CREATE TABLE `category` (
  `categoryId` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类主键',
  `categoryTopic` VARCHAR(20) NOT NULL COMMENT 'Object/places等',
  `status` ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/INACTIVE',
  `usageCount` INT NOT NULL DEFAULT 0 COMMENT '使用次数',
  `createdAt` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `lastUpdatedAt` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后管理时间',
  PRIMARY KEY (`categoryId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源分类表';

-- ----------------------------
-- 3. Tag 标签表
-- ----------------------------
CREATE TABLE `tag` (
  `tagId` BIGINT NOT NULL AUTO_INCREMENT COMMENT '标签主键',
  `tagName` VARCHAR(100) NOT NULL UNIQUE COMMENT '标签名',
  `status` ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/INACTIVE',
  `usageCount` INT NOT NULL DEFAULT 0 COMMENT '使用次数',
  `createdAt` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `lastUpdatedAt` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`tagId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签表';

-- ----------------------------
-- 4. Resource 资源主表
-- ----------------------------
CREATE TABLE `resource` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '资源主键',
  `contributorId` BIGINT NOT NULL COMMENT '贡献者ID',
  `title` VARCHAR(255) NOT NULL COMMENT '标题',
  `description` TEXT NOT NULL COMMENT '描述',
  `copyright` VARCHAR(500) NOT NULL COMMENT '版权声明',
  `categoryId` BIGINT NOT NULL COMMENT '分类ID',
  `place` VARCHAR(255) NULL COMMENT 'IP/归属地',
  `previewImage` VARCHAR(500) NULL COMMENT '预览图URL',
  `mediaUrl` VARCHAR(500) NULL COMMENT '资源内容URL',
  `status` ENUM('Draft','Pending Review','Approved','Rejected', 'Archived') NOT NULL COMMENT '状态 Draft/Pending Review/Approved/Rejected/Archived',
  `resourceType` ENUM('Video','Picture','Audio','Document') NOT NULL COMMENT '资源类型',
  `reviewedAt` TIMESTAMP NULL COMMENT '审核通过时间',
  `createdAt` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updatedAt` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `archivedAt` TIMESTAMP NULL COMMENT '归档时间',
  PRIMARY KEY (`id`),
  KEY `idx_contributor` (`contributorId`),
  CONSTRAINT `fk_resource_contributor`
  FOREIGN KEY (`contributorId`) REFERENCES `user` (`userId`)
  ON DELETE RESTRICT ON UPDATE CASCADE,

  KEY `idx_category` (`categoryId`),
  CONSTRAINT `fk_resource_category`    -- 外键名
  FOREIGN KEY (`categoryId`) REFERENCES `category` (`categoryId`)
  ON DELETE RESTRICT ON UPDATE CASCADE,
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源主表';

CREATE TABLE `resourceArchive` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '资源主键',
  `contributorId` BIGINT NOT NULL COMMENT '贡献者ID',
  `title` VARCHAR(255) NOT NULL COMMENT '标题',
  `description` TEXT NOT NULL COMMENT '描述',
  `copyright` VARCHAR(500) NOT NULL COMMENT '版权声明',
  `categoryId` BIGINT NOT NULL COMMENT '分类ID',
  `place` VARCHAR(255) NULL COMMENT 'IP/归属地',
  `previewImage` VARCHAR(500) NULL COMMENT '预览图URL',
  `mediaUrl` VARCHAR(500) NULL COMMENT '资源内容URL',
  `status` ENUM('Draft','Pending_Review','Approved','Rejected', 'Archived') NOT NULL COMMENT '状态 Draft/Pending Review/Approved/Rejected/Archived',
  `resourceType` ENUM('Video','Picture','Audio','Document') NOT NULL COMMENT '资源类型',
  `reviewedAt` TIMESTAMP NULL COMMENT '审核通过时间',
  `createdAt` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updatedAt` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `archivedAt` TIMESTAMP NULL COMMENT '归档时间',
  PRIMARY KEY (`id`),
  KEY `idx_contributor` (`contributorId`),
  CONSTRAINT `fk_resourceArchive_contributor`
  FOREIGN KEY (`contributorId`) REFERENCES `user` (`userId`)
  ON DELETE RESTRICT ON UPDATE CASCADE,

  KEY `idx_category` (`categoryId`),
  CONSTRAINT `fk_resourceArchive_category`    -- 外键名
  FOREIGN KEY (`categoryId`) REFERENCES `category` (`categoryId`)
  ON DELETE RESTRICT ON UPDATE CASCADE,
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='归档后的资源主表';

-- ----------------------------
-- 5. resource_tag 资源-标签 关联表
-- ----------------------------
CREATE TABLE `resourceTag` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '关联ID',
  `resourceId` BIGINT NOT NULL COMMENT '资源ID',
  `tagId` BIGINT NOT NULL COMMENT '标签ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_resource_tag` (`resourceId`,`tagId`),
  
   -- 外键关联 resource 表
  CONSTRAINT `fk_resourceTag_resource`
  FOREIGN KEY (`resourceId`) REFERENCES `resource` (`id`)
  ON DELETE CASCADE ON UPDATE CASCADE,

  -- 外键关联 tag 表
  CONSTRAINT `fk_resourceTag_tag`
  FOREIGN KEY (`tagId`) REFERENCES `tag` (`tagId`)
  ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源标签关联表';

-- ----------------------------
-- 6. resource_submission 提交版本表
-- ----------------------------
CREATE TABLE `resourceSubmission` (
  `submissionId` BIGINT NOT NULL AUTO_INCREMENT COMMENT '提交版本主键',
  `resourceId` BIGINT NOT NULL COMMENT '资源ID',
  `versionNo` INT NOT NULL COMMENT '版本号 从1开始',
  `submittedBy` BIGINT NOT NULL COMMENT '提交人ID',
  `submittedAt` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  `submissionNote` TEXT NULL COMMENT '提交说明',
  `statusSnapshot` ENUM('Draft','Pending Review','Approved','Rejected', 'Archived') NOT NULL COMMENT '提交时状态',
  `createdAt` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`submissionId`),
  KEY `idx_resource` (`resourceId`),
  CONSTRAINT `fk_resourceSubmission_resource`
  FOREIGN KEY (`resourceId`) REFERENCES `resource` (`id`)
  ON DELETE CASCADE ON UPDATE CASCADE,

  CONSTRAINT `fk_resourceSubmission_user`
  FOREIGN KEY (`submittedBy`) REFERENCES `user` (`userId`)
  ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源提交版本表';

-- ----------------------------
-- 7. Review_Record 审核记录表
-- ----------------------------
CREATE TABLE `reviewRecord` (
  `reviewRecordId` BIGINT NOT NULL AUTO_INCREMENT COMMENT '审核记录ID',
  `resourceId` BIGINT NOT NULL COMMENT '资源ID',
  `submissionId` BIGINT NOT NULL COMMENT '提交版本ID',
  `versionNo` INT NOT NULL COMMENT '版本号',
  `reviewerId` BIGINT NOT NULL COMMENT '审核人ID',
  `actionDescription` VARCHAR(20) NOT NULL COMMENT '具体行为描述',
  `status` ENUM('Approved','Rejected') NOT NULL COMMENT 'APPROVED/REJECTED',
  `feedbackComment` TEXT NULL COMMENT '审核意见',
  `reviewedAt` TIMESTAMP NULL COMMENT '审核时间',
  `createdAt` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  PRIMARY KEY (`reviewRecordId`),
  KEY `idx_resource` (`resourceId`),
  CONSTRAINT `fk_reviewRecord_resource`
  FOREIGN KEY (`resourceId`) REFERENCES `resource` (`id`)
  ON DELETE CASCADE ON UPDATE CASCADE,

  KEY `idx_submission` (`submissionId`),
  CONSTRAINT `fk_reviewRecord_resourceSubmission`
  FOREIGN KEY (`submissionId`) REFERENCES `resourceSubmission` (`submissionId`)
  ON DELETE CASCADE ON UPDATE CASCADE,

  CONSTRAINT `fk_reviewRecord_user`
  FOREIGN KEY (`reviewerId`) REFERENCES `user` (`userId`)
  ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审核记录表';

-- ----------------------------
-- 8. ContributorApplication 贡献者申请表
-- ----------------------------
CREATE TABLE `contributorApplication` (
  `applicationId` BIGINT NOT NULL AUTO_INCREMENT COMMENT '申请记录主键',
  `userId` BIGINT NOT NULL COMMENT '申请人ID',
  `applicationReason` TEXT NOT NULL COMMENT '申请理由',
  `approvalStatus` ENUM('PENDING','APPROVED','REJECTED','ARCHIVED') NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/REJECTED/ARCHIVED',
  `submittedAt` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  `reviewerId` BIGINT NULL COMMENT '审批人ID',
  `reviewedAt` TIMESTAMP NULL COMMENT '审批时间',
  `reviewComment` TEXT NULL COMMENT '审批意见',
  `updatedAt` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`applicationId`),
  KEY `idx_user` (`userId`),
  CONSTRAINT `fk_contributorApplication_user`
  FOREIGN KEY (`userId`) REFERENCES `user` (`userId`)
  ON DELETE CASCADE ON UPDATE CASCADE,

  CONSTRAINT `fk_contributorApplication_reviewer`
  FOREIGN KEY (`reviewerId`) REFERENCES `user` (`userId`)
  ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='贡献者申请表';

CREATE TABLE `contributorApplicationArchive` (
  `applicationId` BIGINT NOT NULL AUTO_INCREMENT COMMENT '申请记录主键',
  `userId` BIGINT NOT NULL COMMENT '申请人ID',
  `applicationReason` TEXT NOT NULL COMMENT '申请理由',
  `approvalStatus` ENUM('PENDING','APPROVED','REJECTED','ARCHIVED') NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/REJECTED/ARCHIVED',
  `submittedAt` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  `reviewerId` BIGINT NULL COMMENT '审批人ID',
  `reviewedAt` TIMESTAMP NULL COMMENT '审批时间',
  `reviewComment` TEXT NULL COMMENT '审批意见',
  `updatedAt` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`applicationId`),
  KEY `idx_user` (`userId`),
  CONSTRAINT `fk_contributorApplicationArchive_user`
  FOREIGN KEY (`userId`) REFERENCES `user` (`userId`)
  ON DELETE CASCADE ON UPDATE CASCADE,

  CONSTRAINT `fk_contributorApplicationArchive_reviewer`
  FOREIGN KEY (`reviewerId`) REFERENCES `user` (`userId`)
  ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='归档的贡献者申请表';

-- ----------------------------
-- 9. Comment 评论表
-- ----------------------------
CREATE TABLE `comment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评论主键',
  `resourceId` BIGINT NOT NULL COMMENT '资源ID',
  `userId` BIGINT NOT NULL COMMENT '评论用户ID',
  `content` TEXT NOT NULL COMMENT '评论内容',
  `createdAt` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_resource` (`resourceId`),
  CONSTRAINT `fk_comment_resource`
  FOREIGN KEY (`resourceId`) REFERENCES `resource` (`id`)
  ON DELETE CASCADE ON UPDATE CASCADE,

  KEY `idx_user` (`userId`),
  CONSTRAINT `fk_comment_user`
  FOREIGN KEY (`userId`) REFERENCES `user` (`userId`)
  ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- ----------------------------
-- 10. Feedback 反馈表
-- ----------------------------
CREATE TABLE `feedback` (
  `feedbackId` BIGINT NOT NULL AUTO_INCREMENT COMMENT '反馈ID',
  `fileNum` INT NOT NULL DEFAULT 0 CHECK (`fileNum` <= 3) COMMENT '附件数量',
  `uploadedAt` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  `userId` BIGINT NOT NULL COMMENT '上传用户ID',
  PRIMARY KEY (`feedbackId`),
  KEY `idx_user` (`userId`),
  CONSTRAINT `fk_feedback_user`
  FOREIGN KEY (`userId`) REFERENCES `user` (`userId`)
  ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='反馈表';

-- ----------------------------
-- 11. Attached_File 附件表
-- ----------------------------
CREATE TABLE `attachedFile` (
  `fileId` BIGINT NOT NULL AUTO_INCREMENT COMMENT '附件主键',
  `feedbackId` BIGINT NOT NULL COMMENT '反馈ID',
  `originalFilename` VARCHAR(500) NOT NULL COMMENT '原文件名',
  `storedFilename` VARCHAR(50) NOT NULL COMMENT '存储文件名',
  `filePath` VARCHAR(500) NOT NULL COMMENT '文件路径/URL',
  `fileType` ENUM('JPG','PNG','PDF','TXT') NOT NULL COMMENT '文件类型',
  `fileSize` BIGINT NOT NULL CHECK (`fileSize` <= 10485760) COMMENT '文件大小',
  `uploadedAt` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  PRIMARY KEY (`fileId`),
  KEY `idx_feedback` (`feedbackId`),
  CONSTRAINT `fk_attachedFile_feedback`
  FOREIGN KEY (`feedbackId`) REFERENCES `feedback` (`feedbackId`)
  ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='附件表';

-- 重新开启外键检查
SET FOREIGN_KEY_CHECKS = 1;