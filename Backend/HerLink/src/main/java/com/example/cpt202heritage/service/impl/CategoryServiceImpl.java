package com.example.cpt202heritage.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.cpt202heritage.entity.Category;
import com.example.cpt202heritage.mapper.CategoryMapper;
import com.example.cpt202heritage.service.CategoryService;
import com.example.cpt202heritage.vo.CategoryTagOptionVO;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;

    public CategoryServiceImpl(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    @Override
    public List<CategoryTagOptionVO> listCategoryOptions() {
        List<Category> categoryList = categoryMapper.selectActiveCategories();
        if (categoryList == null) {
            categoryList = Collections.emptyList();
        }

        List<CategoryTagOptionVO> optionVOList = new ArrayList<>();
        for (Category category : categoryList) {
            CategoryTagOptionVO optionVO = new CategoryTagOptionVO();
            optionVO.setId(category.getCategoryId());
            optionVO.setName(category.getCategoryTopic());
            optionVOList.add(optionVO);
        }

        return optionVOList;
    }
}
