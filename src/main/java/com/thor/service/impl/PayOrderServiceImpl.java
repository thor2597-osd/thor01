package com.thor.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.thor.dao.PayOrderDAO;
import com.thor.dao.UserDAO;
import com.thor.entity.PayOrderDO;
import com.thor.model.Cart;
import com.thor.model.OrderItems;
import com.thor.model.PayOrder;
import com.thor.service.PayOrderService;
import com.thor.util.RedisConstants;
import lombok.AllArgsConstructor;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
@AllArgsConstructor
public class PayOrderServiceImpl extends ServiceImpl<PayOrderDAO, PayOrderDO> implements PayOrderService {

  private final RedissonClient redissonClient;

  private final RedisTemplate<String,Object> redisTemplate;

  private final UserDAO userDAO;

  // 订单初始化
  @Override
  public void createOrder(double price, long userId, List<OrderItems> orderItems, Map<Long, Cart> map) {
    // 创建订单模型对象
    PayOrder payOrder = new PayOrder();
    // 格式化格式为年月日
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    // 获取当前时间
    String now = LocalDate.now().format(dateTimeFormatter);
    // 通过redis的自增获取序号
    RAtomicLong atomicLong = redissonClient.getAtomicLong(now);
    // 每天重新增值
    atomicLong.expireAsync(1, TimeUnit.DAYS);
    long number = atomicLong.incrementAndGet();
    //拼装订单号
    String orderId = now + number;
    // 设置总价格，用户ID，里面的订单项表，订单号
    payOrder.init(price,userId,orderItems,orderId);
    // 将订单存到数据库里面
    save(payOrder.toConvert());
    // 将订单里面的商品操作存到redis里面，如果订单超时那么进行回滚，键是订单号
    // 下面的键之所以是订单号是因为这里只是个订单，不管支付成功还是未支付都是要删除的，如果键是用户名那是为了迎合查询而为
    redisTemplate.opsForHash().putAll(RedisConstants.ORDER + orderId,map);
    // 前端订单有效期是15分钟，这里20分钟，留有充足时间方便操作
    redisTemplate.expire(RedisConstants.ORDER + orderId,RedisConstants.ORDER_TIME,TimeUnit.MINUTES);
    // 生成订单附项，存放到redis里面
    redisTemplate.opsForList().rightPushAll(RedisConstants.ORDER_ITEMS + orderId,orderItems);
    // 设置redis缓存有效期时间
    redisTemplate.expire(RedisConstants.ORDER_ITEMS + orderId,RedisConstants.ORDER_ITEMS_TIME,TimeUnit.MINUTES);
    // 返回订单
  }

  @Override
  public void setQrUrl(String orderNo,String qrUrl) {
    PayOrderDO order = query().eq("order_no", orderNo).one();
    // 设置二维码字符串值
    order.setQrCode(qrUrl);
    log.debug(orderNo);
  }

  // 删除订单并返回回滚数据
  @Override
  public Map<Long, Cart> delete(String orderNo,long userId) {
    // 该用户的订单项表也要进行删除
    redisTemplate.delete(RedisConstants.ORDER_ITEMS + userId);
    // 根据订单号查询redis，等会会自动删除的
    Map<Object, Object> map = redisTemplate.opsForHash().entries(RedisConstants.ORDER + orderNo);
    // 转换为 Map<Long, ProductDetail>
    Map<Long, Cart> cartMap = new ConcurrentHashMap<>();
    // 循环遍历进行类型转换
    map.forEach((productDetailId,cart) -> {
      // Hutool的beanUtil不适合基本类型的转换。
      Long value = Long.parseLong(productDetailId.toString());
      Cart copyProperties = BeanUtil.copyProperties(cart, Cart.class);
      cartMap.put(value,copyProperties);
    });
    // 建立流查询
    QueryWrapper<PayOrderDO> wrapper = new QueryWrapper<>();
    wrapper.eq("order_no",orderNo);
    // 删除订单
    remove(wrapper);

    return cartMap;
  }

  // 生成完整订单
  @Override
  public void createCompleteOrder(String orderNo, String trade_no) {
    // 根据订单号查询订单
    PayOrderDO payOrderDO = query().eq("order_no", orderNo).one();
    // 设置订单状态
    payOrderDO.setStatus(1);
    // 设置订单流水号
    payOrderDO.setTradeNo(trade_no);
    // 设置完整支付时间
    payOrderDO.setPayTime(LocalDateTime.now());
    // 保存订单即可
    save(payOrderDO);
    // 用户的订单数量也要增加
    userDAO.UserAddOrder(payOrderDO.getUserId());
  }
}
