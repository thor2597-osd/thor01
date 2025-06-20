package com.secondhand.trading.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

import com.secondhand.trading.model.Product;
import lombok.Data;

/**
 * <p>
 * 
 * </p>
 *
 * @author LJX
 * @since 2024-11-01
 */
@Data
@TableName("tb_product")
public class ProductDO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商品主键Id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 商品分类的名称
     */
    private String brand;

    /**
     * 里面的商品详情数量
     */
    private long productDetailNumber;

    public Product toConvert(){
        Product product = new Product();
        product.setId(this.getId());
        product.setProductDetailNumber(this.getProductDetailNumber());
        product.setBrand(this.getBrand());
        return product;
    }
}
