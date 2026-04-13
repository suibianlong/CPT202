package com.cpt202.module3.mapper;

import com.cpt202.module3.entity.Tag;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

// Data access of tag table
@Mapper
public interface TagMapper {

    List<Tag> selectActiveTags();
}