package com.thor.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.Calendar;
import java.util.Map;

public class JWTUtils {
    private static final String SING = "ldjfklajsfjas";

    /**
     * 生成Token  header.payload.sign
     */
    public static String getToken(Map<String, String> map) {
        // 设置日历实例
        Calendar instance = Calendar.getInstance();
        // 默认7天过期
        instance.add(Calendar.DATE, 7);
        // 创建jwt builder
        JWTCreator.Builder builder = JWT.create();
        // payload
        map.forEach(builder::withClaim);
        // 设置过期时间并用密钥签名
        return builder.withExpiresAt(instance.getTime())
                                .sign(Algorithm.HMAC256(SING));
    }


    /**
     * 验证 token 的合法性
     *
     * @param token 待验证的 token
     * @return 验证是否成功
     */
    public static boolean verify(String token) {
        try {
            JWT.require(Algorithm.HMAC256(SING)).build().verify(token);
            return true; // 验证成功
        } catch (JWTVerificationException e) {
            return false; // 验证失败
        }
    }

    /**
     * 获取token的信息方法
     */
    public static DecodedJWT getTokenInfo(String token){
        return JWT.require(Algorithm.HMAC256(SING)).build().verify(token);
    }

    /**
     * 检查 Token 是否已经过期
     *
     * @param token 待检查的 token
     * @return 是否已经过期
     */
    public static boolean isTokenExpiring(String token) {
        DecodedJWT decodedJWT = getTokenInfo(token);
        if (decodedJWT == null) {
            return false; // 解析失败
        }
        // 获取当前时间段
        long currentTime = System.currentTimeMillis();
        // 获取token的过期时间
        long expirationTime = decodedJWT.getExpiresAt().getTime();
        // 计算时间差进行比较
        return expirationTime - currentTime <= 0;
    }

    /**
     * 检查 Token 是否即将过期
     *
     * @param token 待检查的 token
     * @param thresholdHours 阈值（小时）
     * @return 是否即将过期
     */
    public static boolean isTokenExpiringSoon(String token, long thresholdHours) {
        DecodedJWT decodedJWT = getTokenInfo(token);
        if (decodedJWT == null) {
            return false; // 解析失败
        }
        // 获取当前时间段
        long currentTime = System.currentTimeMillis();
        // 获取token的过期时间
        long expirationTime = decodedJWT.getExpiresAt().getTime();
        // 计算时间差进行比较
        long remainingTime = expirationTime - currentTime;
        // 默认是传入24小时，即一天的有效期
        // 这里主要判断是否快要过期，如果remainingTime <= 0则代表已经过期了
        return remainingTime <= (thresholdHours * 60 * 60 * 1000) && remainingTime > 0;
    }
}
