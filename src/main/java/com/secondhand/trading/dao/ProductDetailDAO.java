package com.secondhand.trading.dao;

import com.secondhand.trading.entity.ProductDetailDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author LJX
 * @since 2024-11-01
 */
@Mapper
public interface ProductDetailDAO extends BaseMapper<ProductDetailDO> {
  List<ProductDetailDO> selectByName(@Param("name") String name);

  List<ProductDetailDO> selectByAuthorId(@Param("authorId") long authorId);

  long count();

  void updateCommentAndCollection(@Param("id") long productDetailId, @Param("comment") long comment, @Param("collection") long collection);

  List<ProductDetailDO> pagination(@Param("pageSize") long pageSize, @Param("offset") long offset); // 第几页和每页多少数据

  void reduceStock(@Param("num") long quantity,@Param("id") long productDetailId);

  void incrementNum(@Param("num") long num,@Param("id") long id);
}
