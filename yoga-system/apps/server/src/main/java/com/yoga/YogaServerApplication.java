package com.yoga;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 瑜伽馆会员管理系统 - 启动入口
 */
@EnableScheduling
@MapperScan("com.yoga.modules.**.mapper")
@SpringBootApplication
public class YogaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(YogaServerApplication.class, args);
        System.out.println("\n" +
                "  __  __   __  __    ___    ___ \n" +
                " |  \\/  |  \\ \\/ /   / _ \\  / __|\n" +
                " | |\\/| |   \\  /   | (_) | \\__ \\\n" +
                " |_|  |_|   /_/     \\___/  |___/\n" +
                "  Yoga Server started at http://localhost:8080/api\n");
    }
}
