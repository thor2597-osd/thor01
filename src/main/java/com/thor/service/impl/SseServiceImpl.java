package com.thor.service.impl;

import com.thor.model.Result;
import com.thor.service.SseService;
import com.thor.util.RedisConstants;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Service
@AllArgsConstructor
public class SseServiceImpl implements SseService {

  private final ConcurrentHashMap<String, SseEmitter> clients = new ConcurrentHashMap<>();

  private final StringRedisTemplate stringRedisTemplate;

  // 订阅 SSE 事件
  @Override
  public SseEmitter subscribe(String merchantId) {
    SseEmitter emitter = new SseEmitter(0L); // 0L 表示永不过期
    clients.put(merchantId, emitter);

    // 发送当前未读消息数，也就是说离线重新上线就可以收到消息
    sendUnreadCount(merchantId);

    emitter.onCompletion(() -> clients.remove(merchantId));
    emitter.onTimeout(() -> clients.remove(merchantId));

    return emitter;
  }

  // 发送未读消息数
  public void sendUnreadCount(String merchantId) {
    SseEmitter emitter = clients.get(merchantId);
    if (emitter != null) {
      // 集合里面必须已经有商家客户端
      try {
        // 获取商家的未读消息
        String unreadCount = stringRedisTemplate.opsForValue().get(RedisConstants.UNREAD + merchantId);
        if (unreadCount == null) unreadCount = "0";
        emitter.send(SseEmitter.event().data(unreadCount));
      } catch (IOException e) {
        clients.remove(merchantId);
      }
    }
  }

  // 清除未读消息
  @Override
  public Result deleteUnreadCount(String merchantId) {
    SseEmitter emitter = clients.get(merchantId);
    if (emitter != null) {
      try {
        stringRedisTemplate.delete(RedisConstants.UNREAD + merchantId);
        emitter.send(SseEmitter.event().data("0"));
        return Result.success();
      } catch (IOException e) {
        clients.remove(merchantId);
      }
    }
    return Result.fail("该商家不存在");
  }
}
