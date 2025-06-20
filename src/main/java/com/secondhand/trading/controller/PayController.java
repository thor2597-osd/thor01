package com.secondhand.trading.controller;

import cn.hutool.extra.qrcode.QrCodeUtil;
import com.alibaba.fastjson2.JSONObject;
import com.alipay.easysdk.factory.Factory;
import com.alipay.easysdk.kernel.Config;
import com.alipay.easysdk.payment.common.models.AlipayTradeQueryResponse;
import com.alipay.easysdk.payment.facetoface.models.AlipayTradePrecreateResponse;
import com.secondhand.trading.entity.PayOrderDO;
import com.secondhand.trading.model.*;
import com.secondhand.trading.service.OrderItemsService;
import com.secondhand.trading.service.PayOrderService;
import com.secondhand.trading.service.ProductDetailService;
import com.secondhand.trading.util.RedisConstants;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/alipay")
@Slf4j
@RequiredArgsConstructor
public class PayController {

  private final Config config;

  private final PayOrderService payOrderService;

  private final ProductDetailService productDetailService;

  private final OrderItemsService orderItemsService;

  private final RedisTemplate<String,Object> redisTemplate;

  @PostMapping("/pay")
  public String pay(@RequestParam("orderNo") String orderNo,@RequestParam("amount") String amount) throws Exception {
    Factory.setOptions(config);
    log.info(orderNo + "金额" + amount);
    // 调用支付宝接口
    AlipayTradePrecreateResponse response = Factory.Payment.FaceToFace().preCreate(
        "购物订单", orderNo,amount
    );
    // 解析结果
    String httpBody = response.getHttpBody();
    // 转JSON对象
    JSONObject jsonObject = JSONObject.parseObject(httpBody);
    String qrUrl = jsonObject
        .getJSONObject("alipay_trade_precreate_response")
        .get("qr_code")
        .toString();
    // 生成二维码
    QrCodeUtil.generate(qrUrl, 300, 300, new File("D://pay.jpg"));
    // 将二维码字符串值传到订单中
    payOrderService.setQrUrl(orderNo,qrUrl);
    // 将二维码字符串值传给前端
    return qrUrl;
  }

  // 支付成功后会立刻调用该接口
  @PostMapping("/notify")
  public Result notify(HttpServletRequest request) {
    log.info("收到支付成功通知");
    // 打印所有请求参数
    request.getParameterMap().forEach((key, value) -> {
      log.info("参数名: {}, 参数值: {}", key, String.join(", ", value));
    });
    // 流水号，即订单号
    String out_trade_no = request.getParameter("out_trade_no");
    log.info("流水号: {}", out_trade_no);
    // 支付宝的流水订单号
    String amount = request.getParameter("invoice_amount");
    String trade_no = request.getParameter("trade_no");
    // 支付成功会立刻生成订单。
    payOrderService.createCompleteOrder(out_trade_no,trade_no);
    // 并且会生成订单项表
    orderItemsService.createOrderItems(trade_no, Double.parseDouble(amount));
    // 对于库存为0的商品会对其进行下架。
    productDetailService.takeDown();
    return Result.success("支付成功");
  }

  // 订单超时，删除订单并对数据进行回滚
  @PostMapping("/orderTimeOut")
  @ResponseBody
  public Result orderTimeOut(@RequestParam("orderNo")String orderNo,@RequestParam("userId")long userId){
    // 获取map集合即可，方便数据回滚
    Map<Long, Cart> map = payOrderService.delete(orderNo,userId);
    // 数据回滚
    productDetailService.rollBack(map);
    // redis里面的订单项表也要删除
    orderItemsService.delete(orderNo);

    return Result.success("取消订单成功");
  }

  // 查询相关订单
  @GetMapping("/query/{orderNo}")
  public String query(@PathVariable String orderNo) throws Exception {
    Factory.setOptions(config);
    AlipayTradeQueryResponse response = Factory.Payment.Common().query(orderNo);
    return response.getHttpBody();
  }

  // 查询一个用户的所有订单
  @PostMapping("/queryOrder")
  @ResponseBody
  public Result queryOrder(@RequestParam("userId")long userId){
    // 先判断用户Id
    if (userId <= 0) {
      return Result.fail("用户不存在");
    }
    String name = RedisConstants.ORDER + userId;
    // 先查询redis里面是否有相关订单数据
    List<Object> order = redisTemplate.opsForList().range(name, 0, -1);
    if (order == null || order.isEmpty()) {
      // map为空则查询数据库
      List<PayOrderDO> payOrderDOS = payOrderService.query().eq("user_id", userId).list();
      // 设置一个list集合用户存储该用户的所有订单
      List<PayOrder> payOrders = new ArrayList<>();
      // 将数据存放到redis里面
      payOrderDOS.forEach(payOrderDO -> {
        // 根据这些订单号再查询
        List<OrderItems> orderItems = orderItemsService.selectOrderItems(payOrderDO.getOrderNo());
        // 先进行转换
        PayOrder payOrder = payOrderDO.toModel(orderItems);
        payOrders.add(payOrder);
      });
      // 将查询的订单存放到redis即可
      redisTemplate.opsForList().rightPush(name,payOrders);
      // 设置有效期时间
      redisTemplate.expire(name,RedisConstants.ORDER_TIME, TimeUnit.MINUTES);
    }
    // 如果redis里面有数据，那么直接刷新有效期时间即可
    redisTemplate.expire(name,RedisConstants.ORDER_TIME,TimeUnit.MINUTES);

    return Result.success("查询成功",order);
  }
}