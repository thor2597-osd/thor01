package com.secondhand.trading.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.secondhand.trading.model.OrderItems;
import lombok.Data;

@Data
@TableName("tb_order_items")
public class OrderItemsDO {
  @TableId(type = IdType.AUTO)
  private Long id; // 主键
  private Long orderId; // 订单id
  private Long productDetailId; // 商品Id
  private Long quantity; // 商品数量
  private Double totalPrice; // 商品总价格

  public OrderItems toModel(String name, String avatar, double price){
    OrderItems orderItems = new OrderItems();
    orderItems.setQuantity(this.getQuantity());
    orderItems.setProductDetailId(this.getProductDetailId());
    orderItems.setTotalPrice(this.getTotalPrice());
    orderItems.setName(name);
    orderItems.setAvatar(avatar);
    orderItems.setPrice(price);
    return orderItems;
  }
}
