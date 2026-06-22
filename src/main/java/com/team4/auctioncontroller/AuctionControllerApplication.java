package com.team4.auctioncontroller;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class AuctionControllerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuctionControllerApplication.class, args);
    }

    // TODO Design corrections

    //TODO bid separate microservice kimi yazilmaqi (ms-bidding) daha yaxshidir. Auction hemcinin (ms-auction)
    // TODO logging  Scheduler-den basqa yerlerde nezere alinmayib

}
