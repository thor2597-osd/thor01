package com.secondhand.trading.entity;

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
 * @author LJX
 * @since 2024-11-01
 */
@Data
@TableName("tb_collection")
public class CollectionDO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 收藏的商品的主键Id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 收藏商品的用户id
     */
    private Long userId;

    /**
     * 收藏的商品详情id
     */
    private Long productDetailId;

    public void init(long userId, long productDetailId) {
        this.setUserId(userId);
        this.setProductDetailId(productDetailId);
    }
}
