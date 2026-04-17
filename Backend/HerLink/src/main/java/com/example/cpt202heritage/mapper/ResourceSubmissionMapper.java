package com.example.cpt202heritage.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.cpt202heritage.entity.ResourceSubmission;

// Data access of resource submission table
@Mapper
public interface ResourceSubmissionMapper {

    int insert(ResourceSubmission resourceSubmission);

    Integer countByResourceId(@Param("resourceId") Long resourceId);

    ResourceSubmission selectLatestByResourceId(@Param("resourceId") Long resourceId);
}