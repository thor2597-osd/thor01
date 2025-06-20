package com.secondhand.trading.model;

import com.secondhand.trading.entity.PayOrderDO;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PayOrder {
    private String orderNo;          // 商户订单号
    private double amount;           // 支付总金额
    private long userId;             // 设置买家
    // 订单里面购买的所有商品
    private List<OrderItems> orderItems;
    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;// 创建时间
    // 模型转实体
    public PayOrderDO toConvert(){
        PayOrderDO payOrderDO = new PayOrderDO();

        payOrderDO.setCreateTime(this.getCreateTime());
        payOrderDO.setAmount(this.getAmount());
        payOrderDO.setOrderNo(this.getOrderNo());
        payOrderDO.setUserId(this.getUserId());
        // 订单状态是待支付
        payOrderDO.setStatus(0);

        return payOrderDO;
    }
    // 订单初始化
    public void init(double amount, long userId, List<OrderItems> orderItems, String orderNo){
        this.setUserId(userId);
        this.setOrderNo(orderNo);
        this.setOrderItems(orderItems);
        this.setAmount(amount);
        this.setCreateTime(LocalDateTime.now());
    }
}