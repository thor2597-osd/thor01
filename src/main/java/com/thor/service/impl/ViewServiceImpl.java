package com.thor.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.thor.dao.UserDAO;
import com.thor.dao.ViewDAO;
import com.thor.model.ProductDetail;
import com.thor.model.Result;
import com.thor.service.ProductDetailService;
import com.thor.service.ViewService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.thor.util.RedisConstants;
import com.thor.entity.UserDO;
import com.thor.entity.ViewDO;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author OSD
 * @since 2024-11-01
 */
@Service
@AllArgsConstructor
public class ViewServiceImpl extends ServiceImpl<ViewDAO, ViewDO> implements ViewService {

  private final RedisTemplate<String,Object> redisTemplate;

  private final ProductDetailService productDetailService;

  private final UserDAO userDAO;

  // 添加历史浏览记录
  @Override
  public Result addView(long userId, long productDetailId) {
    // 先判断userId和productDetailId
    if (userId <= 0 || productDetailId <= 0) {
      return Result.fail("用户或商品不存在");
    }
    // 先查询redis判断浏览记录是否已经存在
    String name = RedisConstants.VIEW + userId;
    Object object = redisTemplate.opsForHash().get(name, productDetailId);

    if (object != null) {
      // 那就说明浏览记录里面已经有该商品数据了，刷新缓存有效期时间即可
      redisTemplate.expire(name,RedisConstants.VIEW_TIME,TimeUnit.HOURS);

      return Result.success("已经添加到历史浏览");
    }
    // 查询redis里面的所有数据
    Map<Object, Object> entries = redisTemplate.opsForHash().entries(name);
    if (entries.isEmpty()) {
      // 那就说明redis过期了，需要查询数据库
      List<ViewDO> viewDOs = query().eq("user_id",userId).list();
      // 判断数据库里面是否有改浏览记录
      boolean isPresent = false;

      if (!viewDOs.isEmpty()) {
        // 设置map集合
        Map<Long, ProductDetail> map = new HashMap<>();
        // 判断里面是否有该商品的浏览记录
        for (ViewDO viewDO: viewDOs) {
          // 添加到map里面
          map.put(viewDO.getProductDetailId(),productDetailService.getProductDetailsById(productDetailId));
          if (viewDO.getProductDetailId() == productDetailId) {
            // 代表该浏览记录已经存在，待会无需再添加数据库了
            isPresent = true;
          }
        }
        // 判断是否需要添加到数据库
        if (!isPresent) {
          // 添加到数据库
          ViewDO viewDO = ViewDO.init(userId, productDetailId);
          // 修改用户表的数据
          userDAO.UserViewPlus(userId);
          // 先添加数据库
          save(viewDO);
        }
        // 查询用户返回给前端
        UserDO userDO = userDAO.selectById(userId);
        // 最后添加缓存
        redisTemplate.opsForHash().putAll(name,map);
        // 设置缓存有效期时间
        redisTemplate.expire(name,RedisConstants.VIEW_TIME,TimeUnit.HOURS);

        return Result.success("添加成功",userDO.toUser());
      }
    }
    // redis里面没过期，那么直接添加到数据库和redis里面即可
    // 表示该用户还没有浏览记录，直接添加即可
    ViewDO viewDO = ViewDO.init(userId, productDetailId);
    log.error(viewDO.toString());
    // 修改用户表的数据
    userDAO.UserViewPlus(userId);
    // 查询用户返回给前端
    UserDO userDO = userDAO.selectById(userId);
    // 先添加数据库
    save(viewDO);
    // 再添加缓存
    redisTemplate.opsForHash().put(RedisConstants.VIEW + userId,productDetailId,productDetailService.getProductDetailsById(productDetailId));
    // 设置缓存有效期时间
    redisTemplate.expire(name,RedisConstants.VIEW_TIME,TimeUnit.HOURS);

    return Result.success("添加成功",userDO.toUser());
  }

  // 查询该用户的所有历史浏览记录
  @Override
  public Result selectUserViewByUserId(long userId) {
    // 先判断userId
    if (userId <= 0) {
      return Result.fail("该用户主键Id不存在");
    }
    // 设置redis里面的键名
    String name = RedisConstants.VIEW + userId;
    // 先查询redis里面是否有数据
    Map<Object, Object> map = redisTemplate.opsForHash().entries(name);
    // 用于存储该用户发布的所有商品
    List<ProductDetail> list = new ArrayList<>();
    if (!map.isEmpty()) {
      // redis里面数据不为空，先刷新redis的有效期时间
      redisTemplate.expire(name,RedisConstants.VIEW_TIME, TimeUnit.HOURS);
      // 对map循环遍历，将redis里面的该用户所有发布的商品存储到list
      map.forEach((key,value) -> {
        list.add(BeanUtil.toBean(value,ProductDetail.class));
      });
      // 循环完毕后直接返回
      return Result.success("查询成功",list.size(),list);
    }
    // 查询数据库即可
    List<ViewDO> viewDOS = query().eq("user_id", userId).list();
    if (viewDOS.isEmpty()) {
      // 说明用户的浏览记录为空，啥也没有
      return Result.fail("这里空空如也，您还未发布商品或商品均已下架被购买");
    }
    // 数据库里面不为空那么直接调用商品的查找服务即可
    viewDOS.forEach(viewDO -> {
      // 直接调用接口查找商品
      Long productDetailId = viewDO.getProductDetailId();
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

  // 清理该用户的所有浏览记录
  @Override
  @Transactional
  public Result removeUserViewByUserId(long userId) {
    // 先判断userId
    if (userId <= 0) {
      return Result.fail("该用户主键Id不存在");
    }
    // 先删除缓存
    redisTemplate.delete(RedisConstants.VIEW + userId);
    // 再删除数据库即可
    QueryWrapper<ViewDO> wrapper = new QueryWrapper<>();
    wrapper.eq("user_id",userId);
    remove(wrapper);

    // 用户表里面也需要修改
    userDAO.updateUserView(userId);
    // 查询用户并返回
    UserDO userDO = userDAO.selectById(userId);

    return Result.success("删除成功",userDO.toUser());
  }

}
