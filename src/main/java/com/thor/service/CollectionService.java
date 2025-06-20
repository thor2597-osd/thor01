package com.thor.service;

import com.thor.entity.CollectionDO;
import com.baomidou.mybatisplus.extension.service.IService;

import com.thor.model.Result;

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
