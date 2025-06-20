package com.secondhand.trading.controller;

import com.secondhand.trading.model.Result;
import com.secondhand.trading.service.SseService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/sse")
@AllArgsConstructor
public class SseController {

    private final SseService sseService;

    // 订阅 SSE 事件
    @GetMapping("/subscribe/{merchantId}")
    public SseEmitter subscribe(@PathVariable String merchantId) {
        return sseService.subscribe(merchantId);
    }

    // 清除未读消息 =》 即将未读消息改为已读
    @PostMapping("/delete/{merchantId}")
    public Result deleteUnreadCount(@PathVariable String merchantId) {
        return sseService.deleteUnreadCount(merchantId);
    }
}
