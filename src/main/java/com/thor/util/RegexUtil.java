package com.thor.util;

public class RegexUtil {
    // 中国大陆手机号码正则表达式
    private static final String CHINA_MOBILE_PHONE_REGEX = "^1[3-9]\\d{9}$";

    // 邮箱的正则表达式
    private static final String EMAIL_REGEX = "^[a-zA-Z0-9_.-]+@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*\\.[a-zA-Z0-9]{2,6}$";

    // 判断手机号格式是否正确
    public static boolean matchPhone(String phone){
        return phone.matches(CHINA_MOBILE_PHONE_REGEX);
    }

    // 判断邮箱格式是否正确
    public static boolean matchEmail(String email){
        return email.matches(EMAIL_REGEX);
    }
}
