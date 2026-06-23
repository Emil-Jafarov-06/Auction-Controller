package com.project.msauction;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@MapperScan("com.project.msauction.auction.mapper")
public class MsAuctionApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsAuctionApplication.class, args);
    }

}
