package com.cpt202.module3.service.impl;

import com.cpt202.module3.entity.Tag;
import com.cpt202.module3.mapper.TagMapper;
import com.cpt202.module3.service.TagService;
import com.cpt202.module3.vo.CategoryTagOptionVO;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class TagServiceImpl implements TagService {

    private final TagMapper tagMapper;

    public TagServiceImpl(TagMapper tagMapper) {
        this.tagMapper = tagMapper;
    }

    @Override
    public List<CategoryTagOptionVO> listTagOptions() {
        List<Tag> tagList = tagMapper.selectActiveTags();
        if (tagList == null) {
            tagList = Collections.emptyList();
        }

        List<CategoryTagOptionVO> optionVOList = new ArrayList<>();
        for (Tag tag : tagList) {
            CategoryTagOptionVO optionVO = new CategoryTagOptionVO();
            optionVO.setId(tag.getTagId());
            optionVO.setName(tag.getTagName());
            optionVOList.add(optionVO);
        }

        return optionVOList;
    }
}
