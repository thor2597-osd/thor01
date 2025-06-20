package com.thor.dao;

import com.thor.entity.UserDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author OSD
 * @since 2024-11-01
 */
@Mapper
public interface UserDAO extends BaseMapper<UserDO> {
    void UserCollectionPlus(@Param("id") long userId); // 移除用户收藏
    void UserCollectionMinus(@Param("id") long userId); // 增加用户收藏
    void updateUserView(@Param("id") long userId); // 清空用户浏览记录
    void UserViewPlus(@Param("id") long userId); // 增加用户浏览记录
    void UserListingPlus(@Param("id") long userId); // 用户下架商品
    void UserListingMinus(@Param("id") long userId); // 用户发布商品
    void UserAddOrder(@Param("id") long userId); // 用户增加订单
}
