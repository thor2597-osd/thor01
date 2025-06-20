package com.thor.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thor.entity.OrderItemsDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderItemsDAO extends BaseMapper<OrderItemsDO> {

}
