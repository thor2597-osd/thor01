package com.secondhand.trading.dao;

import com.secondhand.trading.entity.ProductDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.secondhand.trading.model.Product;
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
public interface ProductDAO extends BaseMapper<ProductDO> {
  ProductDO get(@Param("id") long productId);
  int addProductNumByBrand(@Param("brand") String brand);
  int decProductNumById(@Param("id") long productId);
}
