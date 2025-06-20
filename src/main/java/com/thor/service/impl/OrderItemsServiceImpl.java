package com.thor.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.thor.dao.OrderItemsDAO;
import com.thor.entity.OrderItemsDO;
import com.thor.model.OrderItems;
import com.thor.model.ProductDetail;
import com.thor.service.OrderItemsService;
import com.thor.service.ProductDetailService;
import com.thor.util.RedisConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class OrderItemsServiceImpl extends ServiceImpl<OrderItemsDAO, OrderItemsDO> implements OrderItemsService {

  private final RedisTemplate<String,Object> redisTemplate;

  private final ProductDetailService productDetailService;

  @Override
  public void createOrderItems(String orderId,double amount){
    // 获取redis里面的所有订单项表数据
    List<Object> list = redisTemplate.opsForList().range(RedisConstants.ORDER_ITEMS + orderId, 0, -1);
    if (list == null || list.isEmpty()) {
      log.error("redis里面的订单项表为空了");
      return;
    }
    // 循环遍历，并将对象转换
    for (Object object : list) {
      // 先反序列化
      OrderItems orderItems = BeanUtil.copyProperties(object, OrderItems.class);
      // 然后将model转换成entity
      OrderItemsDO itemsDO = orderItems.toConvert(orderId, amount);
      // 保存到数据库即可
      save(itemsDO);
    }
  }

  @Override
  public void delete(String orderNo) {
    // 订单超时，存放在redis里面的订单项表里面的数据也要删除
    redisTemplate.delete(RedisConstants.ORDER_ITEMS + orderNo);
  }

  // 查询一个订单里面的所有订单项（即所关联的商品）
  @Override
  public List<OrderItems> selectOrderItems(String orderNo) {
    // 获取redis里面的所有订单项表数据
    List<Object> list = redisTemplate.opsForList().range(RedisConstants.ORDER_ITEMS + orderNo, 0, -1);

    List<OrderItems> orderItemsList = new ArrayList<>();
    if (list == null || list.isEmpty()) {
      // 意味着缓存里面没有数据，直接查询数据库就可以，
      List<OrderItemsDO> orderItemsDOS = query().eq("order_id", orderNo).list();
      // 将实体转换为模型
      orderItemsDOS.forEach(orderItemsDO -> {
        // 查询得到商品
        ProductDetail productDetail = productDetailService.getProductDetailsById(orderItemsDO.getProductDetailId());
        // 转换
        OrderItems orderItems = orderItemsDO.toModel(productDetail.getName(),productDetail.getPhoto(),productDetail.getPrice());
        orderItemsList.add(orderItems);
      });
      // 存入到redis里面
      redisTemplate.expire(RedisConstants.ORDER_ITEMS + orderNo,RedisConstants.ORDER_ITEMS_TIME, TimeUnit.MINUTES);

      return orderItemsList;
    }
    // 如果redis里面不为空，那么直接转换数据类型并刷新有效期时间
    redisTemplate.expire(RedisConstants.ORDER_ITEMS + orderNo,RedisConstants.ORDER_ITEMS_TIME, TimeUnit.MINUTES);
    // 遍历叠加
    list.forEach(value -> {
      orderItemsList.add(BeanUtil.toBean(value, OrderItems.class));
    });

    return orderItemsList;
  }
}
