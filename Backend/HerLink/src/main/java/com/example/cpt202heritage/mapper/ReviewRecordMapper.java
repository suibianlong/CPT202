package com.example.cpt202heritage.mapper;

import com.example.cpt202heritage.entity.ReviewRecord;
import org.apache.ibatis.annotations.Mapper;

// Data access of review record
@Mapper
public interface ReviewRecordMapper {

    ReviewRecord selectLatestByResourceId(Long resourceId);
}
