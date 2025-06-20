package com.thor.util;

public class RedisConstants {
  // token公共前缀名
  public static final String LOGIN_TOKEN = "login_token:";

  // token的有效期时间，单位小时
  public static final long LOGIN_TOKEN_TIME = 24L;

  // 商品大类前缀名
  public static final String QUERY_PRODUCT = "product";

  // 商品大类有效期时间，单位分钟
  public static final long PRODUCT_TIME = 3600L;

  // 用户前缀名
  public static final String USER_UPDATE = "user:";

  // 用户有效期时间，单位小时
  public static final long USER_TIME = 2L;

  // 商品详情前缀名
  public static final String PRODUCT_DETAIL = "productDetail:";

  // 商品详情有效期时间，单位分钟
  public static final long PRODUCT_DETAIL_TIME = 45L;

  // 商品详情库存
  public static final String PRODUCT_DETAIL_STOCK = "productDetailStock:";

  // 商品详情库存时间
  public static final long PRODUCT_DETAIL_STOCK_TIME = 45L;

  // 评论详情前缀名
  public static final String COMMENT = "comment:";

  // 评论详情有效期时间，单位分钟
  public static final long COMMENT_TIME = 45L;

  // 模糊查询的数据有效期时间，单位分钟
  public static final long INPUT_TIME = 5L;

  // 订单前缀名
  public static final String ORDER = "order:";

  // 订单有效期时间，单位分钟
  public static final long ORDER_TIME = 180L;

  // 订单里面的商品详情前缀名
  public static final String ORDER_ITEMS = "order_items:";

  // 订单里面的商品详情又消失时间，单位分钟
  public static final long ORDER_ITEMS_TIME = 180L;

  // 发布商品的商品详情前缀名
  public static final String LISTING = "listing:";

  // 发布商品的商品详情有效期时间，单位小时
  public static final long LISTING_TIME = 4L;

  // 收藏里面的商品详情前缀名
  public static final String COLLECTION = "collection:";

  // 收藏里面的商品详情有效期时间，单位小时
  public static final long COLLECTION_TIME = 4L;

  // 浏览记录里面的商品详情前缀名
  public static final String VIEW = "view:";

  // 浏览记录里面的商品详情有效期时间，单位小时
  public static final long VIEW_TIME = 3L;

  //抢购的商品锁的前缀名
  public static final String PRODUCT_DETAIL_LOCK = "product_detail_lock:";

  // 商家未读消息的前缀名
  public static final String UNREAD = "unread:";
}
