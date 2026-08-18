package com.scau.village;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
// @EnableCaching
@MapperScan("com.scau.village.module.**.mapper")
public class VillageApplication {
    public static void main(String[] args) {
        SpringApplication.run(VillageApplication.class, args);
    }
}