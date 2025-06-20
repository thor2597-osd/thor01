package com.thor.model;

import com.thor.entity.OrderItemsDO;
import lombok.Data;

import java.io.Serializable;

@Data
public class OrderItems implements Serializable {
  // 商品总数量
  private long quantity;
  // 商品总价格
  private double totalPrice;
  // 商品图片
  private String avatar;
  // 商品单价
  private double price;
  // 商品名称
  private String name;
  // 商品Id
  private long productDetailId;

  public void init(ProductDetail productDetail,double amount,long num) {
    this.setName(productDetail.getName());
    this.setPrice(productDetail.getPrice());
    this.setAvatar(productDetail.getPhoto());
    this.setProductDetailId(productDetail.getId());
    this.setTotalPrice(amount);
    this.setQuantity(num);
  }

  public OrderItemsDO toConvert(String orderId,double amount){
    OrderItemsDO orderItemsDO = new OrderItemsDO();
    orderItemsDO.setOrderId(Long.valueOf(orderId));
    orderItemsDO.setQuantity(this.getQuantity());
    orderItemsDO.setTotalPrice(amount);
    orderItemsDO.setProductDetailId(this.getProductDetailId());
    return orderItemsDO;
  }
}
