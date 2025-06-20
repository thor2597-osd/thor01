package com.thor.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.thor.entity.OrderItemsDO;
import com.thor.model.OrderItems;

import java.util.List;

public interface OrderItemsService extends IService<OrderItemsDO> {
  void createOrderItems(String orderId,double amount);
  void delete(String orderNo);
  List<OrderItems> selectOrderItems(String orderNo);
}
