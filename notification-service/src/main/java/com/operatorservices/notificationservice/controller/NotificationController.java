package com.operatorservices.notificationservice.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/notifications")
@RefreshScope
public class NotificationController {

    @Value("${show.notification.count.enabled}")
    private String setting;


    @GetMapping("/settings")
    public String showNotificationCountEnabled(){
        return "Notification count will be shown: " + setting;
    }

}

