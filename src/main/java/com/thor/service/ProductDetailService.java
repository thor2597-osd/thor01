package com.thor.service;

import com.thor.entity.CartDO;
import com.thor.entity.ProductDetailDO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.thor.model.*;
import com.thor.model.Cart;
import com.thor.model.ProductDetail;
import com.thor.model.QueryParam;
import com.thor.model.Result;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author OSD
 * @since 2024-11-01
 */
public interface ProductDetailService extends IService<ProductDetailDO> {

  long initAndSave(ProductDetail productDetail, List<String> path); // 初始化并保存商品

  void updateCommentToProductDetail(long productDetailId,long comment,long collection); // 更新评论数量和收藏数量

  ProductDetail getProductDetailsById(long id); // 获取商品详情

  Result delete(long userId, long productDetailId); // 下架商品

  Result purchase(long userId, List<CartDO> carts); // 清空购物车

  Result selectByName(String name); // 搜索框查询

  void rollBack(Map<Long, Cart> map); // 数据回滚

  void takeDown(); // 付款成功后查询是否有库存为0的商品进行下架

  Result pagination(QueryParam query); // 分页查询
}
