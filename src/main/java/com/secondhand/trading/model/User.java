package com.secondhand.trading.model;

import lombok.Data;

import java.math.BigDecimal;

/*
 * 用户
 * */
@Data
public class User {
    /**
     * 用户的id
     */
    private long id;

    /**
     * 用户的昵称
     */
    private String nickName;

    /**
     * 收藏的商品数量
     */
    private long collection;

    /**
     * 用户账号
     */
    private String userName;

    /**
     * 用户的密码
     */
    private String passWord;

    /**
     * 用户电话号码
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 购买数量
     */
    private long purchaseQuantity;

    /**
     * 浏览数量
     */
    private long viewCount;

    /**
     * 发布商品的数量
     */
    private long listingCount;

    /**
     * 用户的收货地址
     */
    private String address;
}
