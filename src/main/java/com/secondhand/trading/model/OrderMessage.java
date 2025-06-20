package com.secondhand.trading.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
public class OrderMessage implements Serializable {
  // 订单的总价格
  private double price;
  // 购买者的ID
  private long userId;
  // 存放订单项
  private List<OrderItems> list;
  // 存放回滚数据
  private Map<Long, Cart> map;
  public OrderMessage(double price, long userId, List<OrderItems> list, Map<Long, Cart> map) {
    this.price = price;
    this.userId = userId;
    this.list = list;
    this.map = map;
  }
}
