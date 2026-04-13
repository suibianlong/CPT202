package com.cpt202.module3.mapper;

import com.cpt202.module3.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

// Data access of category table
@Mapper
public interface CategoryMapper {

    List<Category> selectActiveCategories();
}