package com.example.demo.expense.dto;

public record PushNotificationRequest(
    String packageName,
    Long postedAt,
    String text,
    String title
) {}

