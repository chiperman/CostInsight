package com.costinsight.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
// import org.springframework.cloud.client.discovery.EnableDiscoveryClient; // 暂时注释掉 Nacos 相关注解

@SpringBootApplication
// @EnableDiscoveryClient // 暂时注释掉 Nacos 相关注解
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }

}