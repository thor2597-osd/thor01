package com.secondhand.trading.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.secondhand.trading.dao.UserDAO;
import com.secondhand.trading.entity.CollectionDO;
import com.secondhand.trading.dao.CollectionDAO;
import com.secondhand.trading.entity.UserDO;
import com.secondhand.trading.model.ProductDetail;
import com.secondhand.trading.model.Result;
import com.secondhand.trading.service.CollectionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.secondhand.trading.service.ProductDetailService;
import com.secondhand.trading.util.RedisConstants;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author LJX
 * @since 2024-11-01
 */
@Service
@AllArgsConstructor
public class CollectionServiceImpl extends ServiceImpl<CollectionDAO, CollectionDO> implements CollectionService {

  private final RedisTemplate<String,Object> redisTemplate;

  private final ProductDetailService productDetailService;

  private final UserDAO userDAO;

  // 查询一个用户的所有收藏
  @Override
  public Result selectCollectionById(Long userId) {
    // 先判断userId
    if (userId <= 0) {
      return Result.fail("该用户主键Id不存在");
    }
    // 设置redis里面的键名
    String name = RedisConstants.COLLECTION + userId;
    // 先查询redis里面是否有数据
    Map<Object, Object> map = redisTemplate.opsForHash().entries(name);
    // 用于存储该用户发布的所有商品
    List<ProductDetail> list = new ArrayList<>();
    if (!map.isEmpty()) {
      // redis里面数据不为空，先刷新redis的有效期时间
      redisTemplate.expire(name,RedisConstants.COLLECTION_TIME, TimeUnit.HOURS);
      // 对map循环遍历，将redis里面的该用户所有发布的商品存储到list
      map.forEach((key,value) -> {
        list.add(BeanUtil.toBean(value,ProductDetail.class));
      });
      // 循环完毕后直接返回
      return Result.success("查询成功",list.size(),list);
    }
    // 查询数据库即可
    List<CollectionDO> collectionDOS = query().eq("user_id", userId).list();
    if (collectionDOS.isEmpty()) {
      // 说明用户还未发布商品或商品均已下架被购买
      return Result.fail("这里空空如也，您还未发布商品或商品均已下架被购买");
    }
    // 数据库里面不为空那么直接调用商品的查找服务即可
    collectionDOS.forEach(listingDO -> {
      // 直接调用接口查找商品
      Long productDetailId = listingDO.getProductDetailId();
      ProductDetail productDetail = productDetailService.getProductDetailsById(productDetailId);
      // 添加商品详情
      list.add(productDetail);
      // 也要添加redis
      redisTemplate.opsForHash().put(name,productDetailId,productDetail);
    });
    // 刷新redis缓存有效期时间并返回数据
    redisTemplate.expire(name,RedisConstants.COLLECTION_TIME, TimeUnit.HOURS);
    return Result.success("查询成功",list.size(),list);
  }

  // 添加到收藏
  @Override
  public Result addToCollection(long userId, long productDetailId) {
    // 先判断userId
    if (userId <= 0 || productDetailId <= 0) {
      return Result.fail("该用户或商品主键id不存在");
    }
    // 先查看redis里面有没有
    if (isHasCollection(userId,productDetailId)) {
      // 收藏里面已经有数据了
      return Result.fail("已经在收藏里面了");
    }
    // 商品详情的收藏数量也要增加
    productDetailService.updateCommentToProductDetail(productDetailId,0,1);
    // 先写入数据库，确保数据持久性
    CollectionDO collectionDO = new CollectionDO();
    collectionDO.init(userId,productDetailId);
    save(collectionDO);
    // 用户表的数据也需要修改
    userDAO.UserCollectionPlus(userId);
    // 查询用户需要返回数据
    UserDO userDO = userDAO.selectById(userId);
    // 查询商品，获得商品模型
    ProductDetail productDetail = productDetailService.getProductDetailsById(productDetailId);
    // 写入到redis中
    String name = RedisConstants.COLLECTION + userId;
    redisTemplate.opsForHash().put(name,productDetailId,productDetail);
    // 刷新redis缓存有效期时间并返回数据
    redisTemplate.expire(name,RedisConstants.COLLECTION_TIME, TimeUnit.HOURS);

    // 另外，发布商品的redis相关数据也要删除
    redisTemplate.delete(RedisConstants.LISTING + userId);
    // 收藏的redis相关数据也要删除
    redisTemplate.delete(RedisConstants.COLLECTION + userId);
    // 浏览的redis相关数据也要删除
    redisTemplate.delete(RedisConstants.VIEW + userId);

    // 将更新后的用户对象返回给前端
    return Result.success("添加成功",userDO.toUser());
  }

  // 查看是否已经收藏
  private boolean isHasCollection(long userId, long productDetailId) {
    Object object = redisTemplate.opsForHash().get(RedisConstants.COLLECTION + userId, productDetailId);
    if (object == null) {
      // 如果redis里面没有再去查看数据库
      CollectionDO collectionDO = query()
          .eq("user_id", userId)
          .eq("product_detail_id", productDetailId)
          .one();
      return collectionDO != null;
    }
    return true;
  }

  // 移除收藏
  @Override
  public Result removeFromCollection(long userId, long productDetailId) {
    // 先判断userId
    if (userId <= 0 || productDetailId <= 0) {
      return Result.fail("该用户或商品主键id不存在");
    }
    // 商品详情的收藏数量也要增加
    productDetailService.updateCommentToProductDetail(productDetailId,0,-1);
    // 先删除缓存hash里面某一个商品即可
    String name = RedisConstants.COLLECTION + userId;
    redisTemplate.opsForHash().delete(name,productDetailId);
    // 再删除数据库
    QueryWrapper<CollectionDO> wrapper = new QueryWrapper<>();
    wrapper.eq("product_detail_id",productDetailId);

    remove(wrapper);
    // 用户表的数据也需要修改
    userDAO.UserCollectionMinus(userId);
    // 再查询更新后的用户对象
    UserDO userDO = userDAO.selectById(userId);
    // 另外，发布商品的redis相关数据也要删除
    redisTemplate.delete(RedisConstants.LISTING + userId);
    // 收藏的redis相关数据也要删除
    redisTemplate.delete(RedisConstants.COLLECTION + userId);
    // 浏览的redis相关数据也要删除
    redisTemplate.delete(RedisConstants.VIEW + userId);

    // 将更新后的用户对象返回给前端
    return Result.success("添加成功",userDO.toUser());
  }
}


