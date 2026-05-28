package com.itticket.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PwdTest {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        // 明文密码
        String pwd = "123456";
        String encodePwd = encoder.encode(pwd);
        System.out.println("BCrypt加密结果：" + encodePwd);
    }
}