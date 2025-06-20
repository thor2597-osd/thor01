package com.secondhand.trading.model;

import com.secondhand.trading.entity.ListingDO;
import lombok.Data;

/*
 * 发布
 * */
@Data
public class Listing {
    private long id;

    /**
     * 用户
     */
    private User user;


    /**
     * 商品详情
     */
    private ProductDetail productDetail;

    /**
     * 商品发布的状态
     */
    private int status;

    /**
     * 商品的品牌名
     */
    private String brand;

    public ListingDO toConvert() {
        ListingDO listingDO = new ListingDO();
        // 获取用户Id
        listingDO.setUserId(this.getUser().getId());
        // 获取商品详情Id
        listingDO.setProductDetailId(this.getProductDetail().getId());
        // 设置状态
        listingDO.setStatus(this.getStatus());
        // 设置品牌名
        listingDO.setBrand(this.getBrand());

        return listingDO;
    }
}
