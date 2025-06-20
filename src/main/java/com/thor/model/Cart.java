package com.thor.model;

import lombok.Data;

@Data
public class Cart {
  private long num;
  private ProductDetail productDetail;

  public Cart(long num, ProductDetail productDetail) {
    this.num = num;
    this.productDetail = productDetail;
  }

  public Cart(){

  }
}
