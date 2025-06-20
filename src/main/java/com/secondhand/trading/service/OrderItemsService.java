package com.secondhand.trading.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.secondhand.trading.entity.OrderItemsDO;
import com.secondhand.trading.model.OrderItems;

import java.util.List;

public interface OrderItemsService extends IService<OrderItemsDO> {
  void createOrderItems(String orderId,double amount);
  void delete(String orderNo);
  List<OrderItems> selectOrderItems(String orderNo);
}
