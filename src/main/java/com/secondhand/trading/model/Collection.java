package com.secondhand.trading.model;

import lombok.Data;

/*
* 收藏
* */
@Data
public class Collection {
    private long id;

    /**
     * 收藏商品的用户
     */
    private User user;

    /**
     * 收藏的商品详情
     */
    private ProductDetail productDetail;
}
