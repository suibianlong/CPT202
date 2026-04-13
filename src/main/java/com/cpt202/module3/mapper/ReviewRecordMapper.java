package com.cpt202.module3.mapper;

import com.cpt202.module3.entity.ReviewRecord;
import org.apache.ibatis.annotations.Mapper;

// Data access of review record
@Mapper
public interface ReviewRecordMapper {

    ReviewRecord selectLatestByResourceId(Long resourceId);
}
