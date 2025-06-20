package com.secondhand.trading.model;

import lombok.Data;

/*
* 商品
* */
@Data
public class Product {
    private long id;

    /**
     * 商品分类的名称
     */
    private String brand;

    /**
     * 里面的商品详情数量
     */
    private long productDetailNumber;
}
