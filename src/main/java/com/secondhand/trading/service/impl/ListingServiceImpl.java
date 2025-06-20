package com.secondhand.trading.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.secondhand.trading.dao.UserDAO;
import com.secondhand.trading.entity.ListingDO;
import com.secondhand.trading.dao.ListingDAO;
import com.secondhand.trading.model.ProductDetail;
import com.secondhand.trading.model.Result;
import com.secondhand.trading.service.ListingService;
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
public class ListingServiceImpl extends ServiceImpl<ListingDAO, ListingDO> implements ListingService {

  private final RedisTemplate<String,Object> redisTemplate;

  private final ProductDetailService productDetailService;

  private final UserDAO userDAO;

  // 存储发表的商品列表
  @Override
  public Result save(long productDetailId,List<String> path) {
    // 直接查询redis即可
    Object object = redisTemplate.opsForValue().get(RedisConstants.PRODUCT_DETAIL + productDetailId);
    // 反序列化
    ProductDetail detail = BeanUtil.copyProperties(object, ProductDetail.class);
    // 添加图片路径
    detail.setPhoto(path.toString());

    ListingDO listingDO = new ListingDO();
    // 数据表都是bigInt类型，要转换
    listingDO.setProductDetailId(productDetailId);
    // 存储商品的发布者的id
    long userId = detail.getAuthor().getId();
    listingDO.setUserId(userId);
    // 用户的发表商品数量也要增加
    userDAO.UserListingPlus(userId);
    // 将商品Id也返回给前端
    detail.setId(productDetailId);
    // 设置发布商品里面的大类名称
    listingDO.setBrand(detail.getProductName());
    // 再将发布商品存放到数据库
    save(listingDO);
    // 返回发布成功得商品给前端
    return Result.success("商品发布成功",detail);
  }

  // 查询单个用户里面已发布的商品
  @Override
  public Result selectByUserId(long userId,String brand) {
    // 先判断userId
    if (userId <= 0) {
      return Result.fail("该用户不存在");
    }
    // 设置redis里面的键名
    String name = RedisConstants.LISTING + userId;
    // 先查询redis里面是否有数据
    Map<Object, Object> map = redisTemplate.opsForHash().entries(name);
    // 用于存储该用户发布的所有商品
    List<ProductDetail> list = new ArrayList<>();
    if (!map.isEmpty()) {
      // redis里面数据不为空，先刷新redis的有效期时间
      redisTemplate.expire(name,RedisConstants.LISTING_TIME, TimeUnit.HOURS);
      // 对map循环遍历，将redis里面的该用户所有发布的商品存储到list
      map.forEach((key,value) -> {
        ProductDetail productDetail = BeanUtil.toBean(value, ProductDetail.class);

        // 先检查brand是否为null
        if (productDetail.getProductName().equals(brand)) {
          list.add(productDetail);
        } else if (StrUtil.isEmpty(brand)) {
          list.add(productDetail);
        }
      });
      // 循环完毕后直接返回
      return Result.success("查询成功",list);
    }
    // 查询数据库即可
    List<ListingDO> listingDOS = query().eq("user_id", userId).list();
    if (listingDOS.isEmpty()) {
      // 说明用户还未发布商品或商品均已下架被购买
      return Result.fail("这里空空如也，您还未发布商品或商品均已下架被购买");
    }
    // 数据库里面不为空那么直接调用商品的查找服务即可
    for (ListingDO listingDO : listingDOS) {
      Long productDetailId = listingDO.getProductDetailId();
      // 获取商品详情
      ProductDetail productDetail = productDetailService.getProductDetailsById(productDetailId);
      if (productDetail == null) {
        // 如果商品详情为空，那么继续
        continue;
      }
      if (StrUtil.isEmpty(brand)) {
        list.add(productDetail);
      } else if (productDetail.getProduct().getBrand().equals(brand)) {
        list.add(productDetail);
      }
      // 存到redis
      redisTemplate.opsForHash().put(name, productDetailId, productDetail);
    }
    // 刷新redis缓存有效期时间并返回数据
    redisTemplate.expire(name,RedisConstants.LISTING_TIME, TimeUnit.HOURS);
    return Result.success("查询成功",list);
  }

  @Override
  public void delete(long userId,long productDetailId) {
    // 先删除缓存
    redisTemplate.opsForHash().delete(RedisConstants.LISTING + userId,productDetailId);
    // 再修改数据库
    QueryWrapper<ListingDO> wrapper = new QueryWrapper<>();
    wrapper.eq("product_detail_id",productDetailId);
    // 删除
    remove(wrapper);
  }
}
