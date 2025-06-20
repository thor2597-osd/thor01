package com.secondhand.trading.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

import com.secondhand.trading.model.User;
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
@TableName("tb_user")
public class UserDO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户的主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

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

    public void register(){
        this.setNickName("新人用户");
        this.setAvatar("default-avator.png");
        this.setCollection(0);
        this.setPurchaseQuantity(0);
        this.setListingCount(0);
        this.setViewCount(0);
    }

    public User toUser(){
        User user = new User();
        user.setId(this.id);
        user.setUserName(this.userName);
        user.setPassWord(this.passWord);
        user.setNickName(this.nickName);
        user.setAvatar(this.avatar);
        user.setPhone(this.phone);
        user.setEmail(this.email);
        user.setCollection(this.collection);
        user.setListingCount(this.listingCount);
        user.setPurchaseQuantity(this.purchaseQuantity);
        user.setViewCount(this.viewCount);
        user.setAddress(this.getAddress());
        return user;
    }

}
