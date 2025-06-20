package com.secondhand.trading.service;

import com.secondhand.trading.model.Result;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface SseService {
  // 订阅 SSE 事件
  SseEmitter subscribe(String merchantId);
  // 发送未读消息数
  void sendUnreadCount(String merchantId);
  // 删除未读消息数
  Result deleteUnreadCount(String merchantId);
}
