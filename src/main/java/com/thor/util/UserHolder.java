package com.thor.util;

import com.thor.model.Cart;
import java.util.HashMap;
import java.util.Map;

public class UserHolder {
  private static final ThreadLocal<Map<Long, Cart>> threadLocal = new ThreadLocal<>();

  // 获取ThreadLocal里面的map集合
  public static Map<Long, Cart> get(){
    return threadLocal.get();
  }

  // 往ThreadLocal里面存储数据
  public static void set(long id,Cart cart){
    // 如果里面是为空，那就创建一个map集合
    if (threadLocal.get() == null) {
      Map<Long, Cart> map = new HashMap<>();
      map.put(id,cart);
      threadLocal.set(map);
    } else {
      // 说明已经存储过数据了，添加即可
      Map<Long, Cart> map = threadLocal.get();
      map.put(id,cart);
    }
  }

  // 移除该线程的ThreadLocal
  public static void remove(){
    threadLocal.remove();
  }
}
