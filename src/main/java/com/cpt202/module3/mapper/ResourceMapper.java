package com.cpt202.module3.mapper;

import com.cpt202.module3.entity.Resource;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

// Data access of resource table
@Mapper
public interface ResourceMapper {

    int insert(Resource resource);

    Resource selectById(@Param("id") Long id);

    Resource selectByIdForUpdate(@Param("id") Long id);

    int updateById(Resource resource);

    List<Resource> selectMyResources(@Param("contributorId") Long contributorId,
                                     @Param("keyword") String keyword,
                                     @Param("status") String status,
                                     @Param("categoryId") Long categoryId);
}
