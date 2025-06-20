package com.secondhand.trading.service;

import com.secondhand.trading.entity.ListingDO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.secondhand.trading.model.ProductDetail;
import com.secondhand.trading.model.Result;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author OSD
 * @since 2024-11-01
 */
public interface ListingService extends IService<ListingDO> {

  Result save(long productDetailId, List<String> path);

  Result selectByUserId(long userId,String brand);

  void delete(long userId,long productDetailId);
}
