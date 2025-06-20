package com.secondhand.trading.service.impl;

import com.secondhand.trading.service.EmailService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * @author OSD
 * @time 2024/11/04 14:05
 * @description 邮件业务层实例，实现对应接口的方法
 */
@Service
public class EmailServiceImpl implements EmailService {
    //配置文件里面已经设置了邮箱的QQ账号。
    @Value("${spring.mail.username}")
    private String emailForm;

    @Resource
    private JavaMailSender mailSender;
    @Override
    public void sendSimpleMail(String sendTo, String title, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(emailForm);
        message.setTo(sendTo);
        message.setSubject(title);
        message.setText(content);
        try {
            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
