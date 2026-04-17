package com.example.cpt202heritage.mapper;

import com.example.cpt202heritage.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

// Data access of category table
@Mapper
public interface CategoryMapper {

    List<Category> selectActiveCategories();
}