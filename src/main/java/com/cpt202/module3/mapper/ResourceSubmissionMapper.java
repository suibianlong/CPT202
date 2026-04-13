package com.cpt202.module3.mapper;

import com.cpt202.module3.entity.ResourceSubmission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

// Data access of resource submission table
@Mapper
public interface ResourceSubmissionMapper {

    int insert(ResourceSubmission resourceSubmission);

    Integer countByResourceId(@Param("resourceId") Long resourceId);

    ResourceSubmission selectLatestByResourceId(@Param("resourceId") Long resourceId);
}