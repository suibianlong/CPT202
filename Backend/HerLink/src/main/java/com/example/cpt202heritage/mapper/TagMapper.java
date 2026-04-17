package com.example.cpt202heritage.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.example.cpt202heritage.entity.Tag;

// Data access of tag table
@Mapper
public interface TagMapper {

    List<Tag> selectActiveTags();
}