package com.cpt202.module3.mapper;

import com.cpt202.module3.entity.ResourceTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

// Data access of resource_tag table
@Mapper
public interface ResourceTagMapper {

    int insert(ResourceTag resourceTag);

    int deleteByResourceId(@Param("resourceId") Long resourceId);

    List<Long> selectTagIdsByResourceId(@Param("resourceId") Long resourceId);
}