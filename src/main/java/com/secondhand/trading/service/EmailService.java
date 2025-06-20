package com.secondhand.trading.service;

/**
 * @author lyt
 * @time 2024/6/27 14:04
 * @description 邮件服务接口，该实体用于用户找回密码
 */
public interface EmailService {
    /**
     * 发送简单邮件
     * @param sendTo 收件人地址
     * @param title  邮件标题
     * @param content 邮件内容
     */
    void sendSimpleMail(String sendTo, String title, String content);
    //原文链接：https://blog.csdn.net/wohuizuofan1/article/details/115618201

}
