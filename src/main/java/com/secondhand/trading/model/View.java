package com.secondhand.trading.model;

import lombok.Data;

/*
* 浏览
* */
@Data
public class View {
    private long id;

    /**
     * 用户
     */
    private User user;

    /**
     * 商品详情
     */
    private ProductDetail productDetail;
}
