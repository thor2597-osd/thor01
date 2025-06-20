package com.secondhand.trading.dao;

import com.secondhand.trading.entity.CommentDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.secondhand.trading.model.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author OSD
 * @since 2024-11-01
 */
@Mapper
public interface CommentDAO extends BaseMapper<CommentDO> {
    int batchAdd(@Param("list") List<CommentDO> userDOs);

    List<CommentDO> findAll();

    int insert(CommentDO commentDO);

    List<Comment> findByRefId(@Param("refId") String refId);

    List<CommentDO> findByUserIds(@Param("userIds") List<Long> ids);
}
