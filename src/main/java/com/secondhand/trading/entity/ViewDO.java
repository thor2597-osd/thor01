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
@TableName("tb_view")
public class ViewDO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 浏览记录的主键Id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户主键Id
     */
    private Long userId;

    /**
     * 商品详情主键Id
     */
    private Long productDetailId;

    public static ViewDO init(long userId, long productDetailId){
        // 实例化对象
        ViewDO viewDO = new ViewDO();
        // 传值
        viewDO.setProductDetailId(productDetailId);
        viewDO.setUserId(userId);
        // 返回结果
        return viewDO;
    }
}
