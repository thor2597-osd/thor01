package com.thor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

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
@TableName("tb_listing")
public class ListingDO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 发布商品的主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户Id
     */
    private Long userId;

    /**
     * 商品详情Id
     */
    private Long productDetailId;

    /**
     * 商品发布的状态
     */
    private Integer status;

    /**
     * 商品的品牌名
     */
    private String brand;
}
