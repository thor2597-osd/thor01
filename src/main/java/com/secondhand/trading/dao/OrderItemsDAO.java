package com.secondhand.trading.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.secondhand.trading.entity.OrderItemsDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderItemsDAO extends BaseMapper<OrderItemsDO> {

}
