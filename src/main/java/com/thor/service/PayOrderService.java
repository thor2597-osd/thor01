package com.thor.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.thor.entity.PayOrderDO;
import com.thor.model.Cart;
import com.thor.model.OrderItems;

import java.util.List;
import java.util.Map;

public interface PayOrderService extends IService<PayOrderDO> {
  void createOrder(double price, long userId, List<OrderItems> orderItems, Map<Long, Cart> map);
  void setQrUrl(String orderNo,String qrUrl);
  Map<Long, Cart> delete(String orderNo,long userId);
  void createCompleteOrder(String orderNo, String out_trade_no);
}
