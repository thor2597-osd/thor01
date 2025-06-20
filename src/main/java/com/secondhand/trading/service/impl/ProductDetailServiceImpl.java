package com.secondhand.trading.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.secondhand.trading.dao.CommentDAO;
import com.secondhand.trading.dao.ProductDAO;
import com.secondhand.trading.dao.UserDAO;
import com.secondhand.trading.entity.*;
import com.secondhand.trading.dao.ProductDetailDAO;
import com.secondhand.trading.model.*;
import com.secondhand.trading.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.secondhand.trading.util.MQConstants;
import com.secondhand.trading.util.RedisConstants;
import lombok.AllArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author LJX
 * @since 2024-11-01
 */
@Service
@AllArgsConstructor
public class ProductDetailServiceImpl extends ServiceImpl<ProductDetailDAO, ProductDetailDO> implements ProductDetailService {

  private final ProductDetailDAO productDetailDAO;

  private final ProductDAO productDAO;

  private final ProductService productService;

  private final UserDAO userDAO;

  private final CommentDAO commentDAO;

  private final RedisTemplate<String, Object> redisTemplate;

  private final UserService userService;

  private final PayOrderService payOrderService;

  private final RabbitTemplate rabbitTemplate;


  // 初始化并保存商品
  @Override
  public long initAndSave(ProductDetail productDetail, List<String> path) {
    // 先把里面有的数据进行转换，有描述，库存，名称，图片路径，价格
    ProductDetailDO productDetailDO = BeanUtil.copyProperties(productDetail, ProductDetailDO.class);
    // 设置图片路径
    productDetailDO.setPhoto(path.toString());
    productDetail.setPhoto(path.toString());
    // 下面的数据刚发布当然都为空
    productDetailDO.setCollection(0);
    productDetailDO.setCommentNumber(0);
    // 设置商品详情库存
    productDetailDO.setProductDetailNumber(productDetail.getProductDetailNumber());
    // 设置商品用户ID
    productDetailDO.setAuthorId(productDetail.getAuthor().getId());
    // 设置时间
    productDetailDO.setGmtCreated(LocalDateTime.now());
    productDetailDO.setGmtModified(LocalDateTime.now());
    productDetail.setGmtCreated(LocalDateTime.now());
    productDetail.setGmtModified(LocalDateTime.now());

    // 设置商品大类id
    // 商品所属大类也要进行添加
    Product product = productService.addProductDetail(productDetail.getProductName());
    // 给返回的商品详情添加商品大类d对象
    productDetail.setProduct(product);
    productDetailDO.setProductId(product.getId());
    // 先存储到redis缓存，防止击穿
    // 存储商品详情
    productDetailDAO.insert(productDetailDO);
    // 将商品详情序列化成字符串并存储到redis
    productDetail.setId(productDetailDO.getId());
    redisTemplate.opsForValue().set(RedisConstants.PRODUCT_DETAIL + productDetailDO.getId(), productDetail);
    // 返回主键
    return productDetailDO.getId();
  }

  // 更新收藏数量和评论数量
  @Override
  public void updateCommentToProductDetail(long productDetailId, long comment, long collection) {
    // 1.先删除缓存
    redisTemplate.delete(RedisConstants.PRODUCT_DETAIL + productDetailId);
    // 更改数据库中的数据
    productDetailDAO.updateCommentAndCollection(productDetailId, comment, collection);
  }

  // 获取指定的商品详情
  @Override
  public ProductDetail getProductDetailsById(long id) {
    // 先查询redis里面有没有该数据
    String name = RedisConstants.PRODUCT_DETAIL + id;
    Object object = redisTemplate.opsForValue().get(name);
    if (object != null) {
      // 反序列化成对象
      ProductDetail productDetail = BeanUtil.toBean(object, ProductDetail.class);
      // 刷新商品详情有效期时间
      redisTemplate.expire(name, RedisConstants.PRODUCT_DETAIL_TIME, TimeUnit.MINUTES);
      // 那就说明商品缓存状态的库存也没有过期
      redisTemplate.expire(RedisConstants.PRODUCT_DETAIL_STOCK + id, RedisConstants.PRODUCT_DETAIL_STOCK_TIME, TimeUnit.MINUTES);

      return productDetail;
    }
    // 查询数据库
    ProductDetailDO detailDO = query().eq("id", id).one();
    if (detailDO == null) {
      log.error("空");
      return null;
    }
    ProductDO productDO = productDAO.selectById(detailDO.getProductId());
    // 将实体类转换为模型类
    ProductDetail detail = BeanUtil.copyProperties(detailDO, ProductDetail.class);
    detail.setProduct(productDO.toConvert());
    // 设置商品详情的品牌
    detail.setProductName(productDO.getBrand());
    // 用户也要存储到商品详情里面
    UserDO userDO = userDAO.selectById(detailDO.getAuthorId());
    detail.setAuthor(BeanUtil.copyProperties(userDO, User.class));
    // 不为空则添加在redis里面
    redisTemplate.opsForValue().set(name, detail);
    // 商品库存也要存放到缓存里面（先查看库存缓存是否已经存在）
    if (redisTemplate.opsForValue().get(RedisConstants.PRODUCT_DETAIL_STOCK + id) == null) {
      redisTemplate.opsForValue().set(RedisConstants.PRODUCT_DETAIL_STOCK + id, detail.getProductDetailNumber());
    }
    // 还要设置有效期时间
    redisTemplate.expire(name, RedisConstants.PRODUCT_DETAIL_TIME, TimeUnit.MINUTES);
    // 商品库存也要设置有效期时间
    redisTemplate.expire(RedisConstants.PRODUCT_DETAIL_STOCK + id, RedisConstants.PRODUCT_DETAIL_STOCK_TIME, TimeUnit.MINUTES);

    return detail;
  }

  // 下架商品
  @Override
  public Result delete(long userId, long productDetailId) {
    if (productDetailId <= 0 || userId <= 0) {
      return Result.fail("商品不存在或已被删除");
    }
    // 先删除缓存，再删除数据库
    // 先删除评论
    redisTemplate.delete(RedisConstants.COMMENT + productDetailId);
    // 删除缓存里的商品
    redisTemplate.delete(RedisConstants.PRODUCT_DETAIL + productDetailId);
    // 查询数据库中的数据
    ProductDetailDO productDetailDO = productDetailDAO.selectById(productDetailId);
    // 删除数据库中的商品
    productDetailDAO.deleteById(productDetailId);
    // 删除数据库中的该商品详情下的所有评论
    QueryWrapper<CommentDO> wrapper = new QueryWrapper<>();
    wrapper.eq("ref_id", productDetailId);
    commentDAO.delete(wrapper);
    // 用户里面的商品数量也要减少
    userDAO.UserListingMinus(userId);
    // 商品大类下面的商品数量也需要删除
    productService.decProductDetail(productDetailDO.getProductId());
    // 同时，我的发布里面的商品也要删除

    return Result.success("删除成功");
  }

  // 购物车结算
  @Override
  public Result purchase(long userId, List<CartDO> carts) {
    // 先查看redis里面是否有用户信息
    UserDO user = userService.getUserById(userId);
    if (user == null) {
      return Result.fail("没有该用户");
    }
    // 存储总价格
    double price = 0.00;
    // 用于存放订单的附项
    List<OrderItems> list = new ArrayList<>();
    // 存放回滚数据
    Map<Long, Cart> map = new HashMap<>();
    // 检查并扣减redis库存
    for (CartDO cartDO : carts) {
      String name = RedisConstants.PRODUCT_DETAIL_STOCK + cartDO.getProductDetailId();
      Long decrement = redisTemplate.opsForValue().decrement(name, cartDO.getNum());
      // 判断库存是否充足
      if (decrement == null || decrement < 0) {
        // 库存不足，直接回滚即可
        redisTemplate.opsForValue().increment(name, cartDO.getNum());
        if (!list.isEmpty()) {
          // 已经购买的商品库存也要回滚
          list.forEach(orderItems -> {
            String stockKey = RedisConstants.PRODUCT_DETAIL_STOCK + orderItems.getProductDetailId();
            redisTemplate.opsForValue().increment(stockKey, orderItems.getQuantity());
          });
        }
        // 说明该商品库存已经不足了，将缓存删掉就可以了
        redisTemplate.delete(RedisConstants.PRODUCT_DETAIL + cartDO.getProductDetailId());

        return Result.fail("商品库存不足：" + cartDO.getProductDetailId());
      }
      // 库存充足
      // 获取商品信息
      ProductDetail productDetail = getProductDetailsById(cartDO.getProductDetailId());
      // 这个商品的数量 * 价格
      double amount = productDetail.getPrice() * cartDO.getNum();
      // 库存充足，增加价格即可
      price += amount;
      // 存储数据，方便回滚
      map.put(productDetail.getId(), new Cart(cartDO.getNum(), productDetail));
      // 列表添加商品小订单
      OrderItems orderItems = new OrderItems();
      orderItems.init(productDetail, amount, cartDO.getNum());
      // 数组里面增加数据
      list.add(orderItems);
    }
    // 将对象序列化成字符串类型的JSON格式
    OrderMessage orderMessage = new OrderMessage(price, userId, list, map);
    // 发送订单消息到RabbitMQ
    rabbitTemplate.convertAndSend(MQConstants.orderQueue, JSONUtil.toJsonStr(orderMessage));
    // 将总订单传给前端即可
    return Result.success("创建订单成功");
  }

  // 异步处理这些订单消息
  @RabbitListener(queues = MQConstants.orderQueue)
  public void handleOrderMessage(String message) {
    // 将JSON格式反序列化成对象类型
    OrderMessage orderMessage = JSONUtil.toBean(message, OrderMessage.class);
    try {
      List<OrderItems> orderItems = orderMessage.getList();
      // 生成一个总订单
      payOrderService.createOrder(orderMessage.getPrice(), orderMessage.getUserId(), orderItems, orderMessage.getMap());
      // 同步到数据库即可
      for (OrderItems order : orderItems) {
        // 减少数据库的库存
        productDetailDAO.reduceStock(order.getQuantity(), order.getProductDetailId());
        // 再删除缓存
        redisTemplate.delete(RedisConstants.PRODUCT_DETAIL + order.getProductDetailId());
      }
    } catch (Exception e) {
      log.error("订单处理失败: ", e);
    }
  }

  // 库存不足，数据回滚
  @Override
  public void rollBack(Map<Long, Cart> map) {
    // 对之前所有修改过的数据进行循环遍历
    map.forEach((productDetailId, cart) -> {
      // 将对象序列化成JSON格式
      handleRollBackMessage(JSONUtil.toJsonStr(cart));
    });
  }

  // 异步回滚订单数据
  @RabbitListener(queues = MQConstants.rollBack)
  public void handleRollBackMessage(String message){
    // 先将JSON反序列化成对象
    Cart cart = JSONUtil.toBean(message, Cart.class);
    // 获取商品详情的ID
    long id = cart.getProductDetail().getId();
    // 进行redis的原子操作（不存在则不用管）
    if (redisTemplate.opsForValue().get(RedisConstants.PRODUCT_DETAIL_STOCK + id) != null) {
      redisTemplate.opsForValue().increment(RedisConstants.PRODUCT_DETAIL_STOCK + id,cart.getNum());
    }
    // 删除缓存
    redisTemplate.delete(RedisConstants.PRODUCT_DETAIL + id);
    // 更改数据库数据
    productDetailDAO.incrementNum(cart.getNum(),id);
  }

  @Override
  public void takeDown() {
    // 直接查询数据库里面库存为零的商品
    List<ProductDetailDO> productDetailDOS = query().eq("product_detail_number", 0).list();
    if (productDetailDOS.isEmpty()) {
      // 如果没有为0库存的，直接返回即可
      return;
    }
    // 如果有，那么调用删除商品接口即可
    productDetailDOS.forEach(productDetailDO -> {
      delete(productDetailDO.getAuthorId(), productDetailDO.getId());
    });
  }

  // 分页查询
  @Override
  public Result pagination(QueryParam query) {
    long pageSize = ((long) query.getPageSize() * (query.getPageNum() - 1)); // 从第几个数据开始

    // 先分页查询得到指定数据
    List<ProductDetailDO> productDetailDOS = productDetailDAO.pagination(query.getPageSize(), pageSize);

    // 转换对象
    List<ProductDetail> list = new ArrayList<>();
    for (ProductDetailDO productDetailDO : productDetailDOS) {

      // 获取对应商品大类
      Product product = productService.selectProductById(productDetailDO.getProductId());
      // 获取商品的发布者
      User user = userService.getUserById(productDetailDO.getAuthorId()).toUser();

      list.add(productDetailDO.toProductDetail(product, user));
    }
    // 获取表中的总记录数
    long count = productDetailDAO.count();
    // 创建分页对象
    Paging<ProductDetail> page = new Paging<>(query.getPageNum(), query.getPageSize(), count % query.getPageSize() == 0 ? count / query.getPageSize() : count / query.getPageSize() + 1, count, list);

    return Result.success("刷新成功", page.getPageSize(), page);
  }

  // 搜索框查询（缓存空对象来防止缓存穿透）
  @Override
  public Result selectByName(String name) {
    Result result = new Result();

    // 先查询redis里面有没有数据
    List<Object> list = redisTemplate.opsForList().range(name, 0, -1);
    // 如果redis里面数据不为空，则直接返回即可
    if (list != null && !list.isEmpty()) {
      // 刷新redis有效期时间
      redisTemplate.expire(name, RedisConstants.INPUT_TIME, TimeUnit.MINUTES);

      return Result.success(list.size(), list);
    }
    // 如果缓存没有数据则查询数据库
    List<ProductDetailDO> productDetailDOS = productDetailDAO.selectByName(name);
    if (productDetailDOS.isEmpty()) {
      // 如果数据库查询后也是空的，那么将空数据存到redis里面即可，防止缓存穿透
      redisTemplate.opsForList().leftPush(name, " ");
      // 设置有效期为5分钟
      redisTemplate.expire(name, RedisConstants.INPUT_TIME, TimeUnit.MINUTES);

      result.setMessage("您要搜索的东西不存在");
      result.setCode(200);
      result.setTotal(0);

      return result;
    }
    // 如果数据库中有数据，那么先进行对象转换
    List<ProductDetail> productDetails = productDetailDOS.stream().map(productDetailDO -> productDetailDO.toProductDetail(productService.selectProductById(productDetailDO.getProductId()), userService.getUserById(productDetailDO.getAuthorId()).toUser())).toList();
    // 如果数据库中有数据，那么存储到redis直接返回
    productDetails.forEach(productDetail -> {
      redisTemplate.opsForList().rightPush(name, productDetail);
    });

    return Result.success(productDetails.size(), productDetails);
  }
}
