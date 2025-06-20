package com.secondhand.trading.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.secondhand.trading.model.Product;
import com.secondhand.trading.model.ProductDetail;
import com.secondhand.trading.model.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * <p>
 * 
 * </p>
 *
 * @author OSD
 * @since 2024-11-01
 */
@Data
@TableName("tb_product_detail")
public class ProductDetailDO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商品详情主键Id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

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
     * 所属商品大类的Id
     */
    private Long productId;

    /**
     * 商品详情图片路径
     */
    private String photo;

    /**
     * 发布商品的用户
     */
    private Long authorId;

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
     * 商品的发布时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime gmtCreated;

    /**
     * 商品的修改时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime gmtModified;

    public ProductDetail toProductDetail(Product product,User user){
        ProductDetail productDetail = new ProductDetail();
        productDetail.setProductName(product.getBrand());
        productDetail.setId(this.getId());
        productDetail.setName(this.getName());
        productDetail.setAuthor(user);
        productDetail.setPrice(this.getPrice());
        productDetail.setProduct(product);
        productDetail.setPhoto(this.photo);
        productDetail.setDescription(this.description);
        productDetail.setCollection(this.collection);
        productDetail.setProductDetailNumber(this.productDetailNumber);
        productDetail.setCommentNumber(this.commentNumber);
        productDetail.setGmtCreated(this.gmtCreated);
        productDetail.setGmtModified(this.gmtModified);
        return productDetail;
    }
}
