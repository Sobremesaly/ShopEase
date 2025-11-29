package com.shopease;

import com.baomidou.mybatisplus.core.toolkit.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author hspcadmin
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients // 开启 OpenFeign
@MapperScan("mapper")
public class UserApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }
}
