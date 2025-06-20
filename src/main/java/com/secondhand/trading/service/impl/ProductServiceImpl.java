package com.secondhand.trading.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.secondhand.trading.entity.ProductDO;
import com.secondhand.trading.dao.ProductDAO;
import com.secondhand.trading.model.Product;
import com.secondhand.trading.model.Result;
import com.secondhand.trading.service.ProductService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.secondhand.trading.util.RedisConstants;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author OSD
 * @since 2024-11-01
 */
@Service
@AllArgsConstructor
public class ProductServiceImpl extends ServiceImpl<ProductDAO, ProductDO> implements ProductService {

  private final RedisTemplate<String, Object> redisTemplate;

  private final ProductDAO productDAO;

  @Override
  public Result queryBrandAndChildrenNumber() {
    // 先查询redis
    List<Object> list = redisTemplate.opsForList().range(RedisConstants.QUERY_PRODUCT, 0, -1);
    if (list == null || list.isEmpty()) {
      // 查询数据库
      List<ProductDO> productDOS = query().list();
      if (productDOS.isEmpty()) {
        return Result.fail("看来还没有添加商品大类");
      }
      // 如果数据库不为空则添加到redis里面
      redisTemplate.opsForList().rightPushAll(RedisConstants.QUERY_PRODUCT, productDOS);

      return Result.success("查询成功", productDOS);
    }

    return Result.success("查询成功", list);
  }

  @Override
  @Transactional
  public Result delete(String brand) {
    // 先查询数据库里面是否有这个数据
    ProductDO productDO = query().eq("brand", brand).one();
    if (productDO == null) {
      return Result.fail("数据库为空");
    }
    // 为了保证数据一致，应先删除缓存
    redisTemplate.delete(RedisConstants.QUERY_PRODUCT);
    // 删除数据库
    removeById(productDO);

    return Result.success("");
  }

  @Override
  @Transactional
  public Result add(ProductDO productDO) {
    // 传入数据是否为空
    if (productDO == null || StrUtil.isEmpty(productDO.getBrand())) {
      return Result.fail("数据或者品牌信息不可为空");
    }

    // 先查询数据是否已经在数据库中存在
    ProductDO product = query()
        .eq("brand", productDO.getBrand())
        .one();
    if (product != null) {
      return Result.fail("该商品品牌在数据库中已经存在");
    }
    // 更新数据库
    boolean save = save(productDO);
    if (!save) {
      return Result.fail("更新失败");
    }
    // 更新缓存
    redisTemplate.opsForList().rightPush(RedisConstants.QUERY_PRODUCT, productDO);

    return Result.success("添加成功", productDO);
  }


  @Override
  @Transactional
  public Result updateProduct(ProductDO productDO) {
    Result result = new Result();

    if (productDO == null || productDO.getId() == null) {
      return Result.fail("该商品大类不存在");
    }
    // 先删除缓存
    redisTemplate.delete(RedisConstants.QUERY_PRODUCT);
    // 再修改数据库
    int update = productDAO.updateById(productDO);
    // 查询数据库并存储在redis里面
    List<ProductDO> productDOS = query().list();
    redisTemplate.opsForList().rightPushAll(RedisConstants.QUERY_PRODUCT, productDOS);

    if (update == 0) {
      return Result.fail("修改失败");
    }

    result.setCode(200);
    result.setMessage("修改成功");
    result.setTotal(1);
    result.setData(productDO);

    return result;
  }

  // 增加商品的发布数量
  @Override
  public Product addProductDetail(String brand) {
    // 先删除缓存
    redisTemplate.delete(RedisConstants.QUERY_PRODUCT);
    // 增加商品的发布数量
    productDAO.addProductNumByBrand(brand);
    // 查询商品大类
    List<ProductDO> productDOS = query().list();
    // 更新redis
    redisTemplate.opsForList().rightPushAll(RedisConstants.QUERY_PRODUCT, productDOS);
    for (ProductDO productDO : productDOS) {
      if (productDO.getBrand().equals(brand)) {
        // 如果商品名一致，则直接返回即可
        return productDO.toConvert();
      }
    }
    return null;
  }

  @Override
  public void decProductDetail(long id) {
    // 先删除缓存
    redisTemplate.delete(RedisConstants.QUERY_PRODUCT);
    // 在修改数据库
    productDAO.decProductNumById(id);
    // 查询数据库并添加到缓存
    List<ProductDO> productDOS = query().list();
    // 更新redis
    redisTemplate.opsForList().rightPushAll(RedisConstants.QUERY_PRODUCT, productDOS);
  }

  // 通过商品大类名称来查询商品大类
  @Override
  public Product selectProductById(long id) {
    // 先判断商品大类id
    if (id <= 0) {
      return null;
    }
    // 查询redis
    List<Object> list = redisTemplate.opsForList().range(RedisConstants.QUERY_PRODUCT, 0, -1);
    if (list == null || list.isEmpty()) {
      // redis里面为空则查询数据库
      return selectDatabase(id);
    }
    // 遍历查找对应的商品大类
    for (Object value : list) {
      Product product = BeanUtil.copyProperties(value, Product.class);
      if (product.getId() == id) {
        return product;
      }
    }
    // redis里面没有该数据也查询数据库
    return selectDatabase(id);
  }

  private Product selectDatabase(long id) {
    ProductDO productDO = query().eq("id", id).one();
    // 并将这个商品类存放到redis里面
    redisTemplate.opsForList().rightPush(RedisConstants.QUERY_PRODUCT, productDO.toConvert());

    return productDO.toConvert();
  }
}
