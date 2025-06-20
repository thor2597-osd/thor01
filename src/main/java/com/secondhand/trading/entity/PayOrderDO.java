package com.secondhand.trading.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.secondhand.trading.model.OrderItems;
import com.secondhand.trading.model.PayOrder;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("tb_pay_order")
public class PayOrderDO {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;             // 发起购买的用户id
    private String orderNo;          // 商户订单号
    private Double amount;           // 订单金额
    private String qrCode;           // 支付二维码链接
    private Integer status;          // 支付状态：0-待支付 1-支付成功 2-支付失败
    private String tradeNo;          // 支付宝交易号

    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime payTime;   // 支付时间
    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;// 创建时间

    public PayOrder toModel(List<OrderItems> orderItems){
        PayOrder payOrder = new PayOrder();
        payOrder.setOrderNo(this.getOrderNo());
        payOrder.setAmount(this.getAmount());
        payOrder.setUserId(this.getUserId());
        payOrder.setCreateTime(this.getCreateTime());
        payOrder.setOrderItems(orderItems);
        return payOrder;
    }
}