package com.secondhand.trading.service;

import com.secondhand.trading.entity.ViewDO;
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
public interface ViewService extends IService<ViewDO> {
  Result addView(long userId, long productDetailId);

  Result selectUserViewByUserId(long userId);

  Result removeUserViewByUserId(long userId);
}
