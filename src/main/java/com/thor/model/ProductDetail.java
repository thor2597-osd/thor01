package com.thor.model;

import cn.hutool.core.bean.BeanUtil;
import com.thor.entity.ProductDetailDO;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/*
* 商品详情
* */
@Data
public class ProductDetail {
    private long id;

    /**
     * 商品详情名称
     */
    private String name;

    /**
     * 商品详情价格
     */
    private double price;

    /**
     * 商品详情库存
     */
    private long productDetailNumber;

    /**
     * 所属商品大类
     */
    private Product product;

    /**
     * 商品图片
     */
    private String photo;

    /**
     * 所属商品大类品牌名称
     */
    private String productName;

    /**
     * 该商品详情被收藏数
     */
    private long collection;

    /**
     * 商品描述
     */
    private String description;

    /**
     * 商品下面的评论数量
     */
    private long commentNumber;

    /**
     * 发布商品的用户
     */
    private User author;

    /**
     * 商品的发布时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime gmtCreated;

    /**
     * 商品的修改时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime gmtModified;

    public ProductDetailDO toConvertAndInit(String path){
        ProductDetailDO productDetailDO = BeanUtil.copyProperties(this, ProductDetailDO.class);
        // 文件无法转化为String类型
        productDetailDO.setPhoto(path);
        return productDetailDO;
    }

    public ProductDetailDO toConvert(){
        ProductDetailDO productDetailDO = BeanUtil.toBean(this, ProductDetailDO.class);
        productDetailDO.setProductId(this.getProduct().getId());
        productDetailDO.setAuthorId(this.getAuthor().getId());
        return productDetailDO;
    }
}
