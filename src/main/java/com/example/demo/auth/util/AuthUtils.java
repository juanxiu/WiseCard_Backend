package com.example.demo.auth.util;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

public class AuthUtils {

    private AuthUtils() {
    }

    public static Long getMemberId() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            User user = (User) principal;
            return Long.parseLong(user.getUsername());
        } catch (Exception e) {
            throw new RuntimeException("인증에 실패했습니다.");
        }
    }
}