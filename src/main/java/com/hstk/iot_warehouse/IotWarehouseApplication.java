package com.hstk.iot_warehouse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 物联网仓库管理系统启动类
 */
@EnableScheduling
@SpringBootApplication
public class IotWarehouseApplication {

    public static void main(String[] args) {
        SpringApplication.run(IotWarehouseApplication.class, args);
    }

}
