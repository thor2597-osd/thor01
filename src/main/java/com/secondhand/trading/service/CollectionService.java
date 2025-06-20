package com.secondhand.trading.service;

import com.secondhand.trading.entity.CollectionDO;
import com.baomidou.mybatisplus.extension.service.IService;

import com.secondhand.trading.model.Result;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author OSD
 * @since 2024-11-01
 */
public interface CollectionService extends IService<CollectionDO> {
  Result selectCollectionById(Long userId);

  Result addToCollection(long userId, long productDetailId);

  Result removeFromCollection(long userId, long productDetailId);
}
