package com.cpt202.module3.mapper;

import com.cpt202.module3.entity.AppUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AppUserMapper {

    int insert(AppUser user);

    AppUser selectById(@Param("userId") Long userId);

    AppUser selectByEmail(@Param("email") String email);

    int updateBasicInfo(AppUser user);
}
