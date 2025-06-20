package com.thor.util;

import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class LoginOrRefreshInterceptor implements HandlerInterceptor {

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,@NonNull HttpServletResponse response,@NonNull Object handler) throws Exception {
        // 查找请求头里面的accessToken存储值
        String accessToken = request.getHeader("Authorization");

        // 检查是否含有accessToken
        if (StrUtil.isEmpty(accessToken)) {
            // 如果请求头中没有 Authorization 字段，返回 JSON 响应，拒绝执行
            writeJsonResponse(response, 403, "请先进行登录");
            return false;
        }

        // 如果accessToken错误则进行拦截
        if (!JWTUtils.verify(accessToken)) {
            // 因为验证不正确，绝对是有人恶意篡改token字段，返回 JSON 响应
            writeJsonResponse(response, 403, "token恶意修改，请进行登录");
            return false;
        }

        // 检查accessToken是否已经过期
        if (JWTUtils.isTokenExpiring(accessToken)) {
            // 过期了，返回JSON响应
            writeJsonResponse(response, 401, "会话已过期，请重新登录，跳转到登陆页面");
            return false;
        }

        return true;
    }

    private void writeJsonResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("message", message);

        String jsonResponse = objectMapper.writeValueAsString(responseMap);
        response.getWriter().write(jsonResponse);
    }
}