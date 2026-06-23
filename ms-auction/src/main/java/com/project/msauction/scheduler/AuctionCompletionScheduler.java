package com.project.msauction.scheduler;

import com.project.msauction.auction.service.AuctionService;import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class AuctionCompletionScheduler {

    private final AuctionService auctionService;

    @Scheduled(
            initialDelay = 30000,
            fixedRate = 30000
    )
    public void finishExpiredAuctions(){
        log.info("Checking Expired Auctions...");
        int completed = auctionService.finishExpiredAuctions();
        log.info("Finished Expired Auctions | Count: {}", completed);
    }

    @Scheduled(
            initialDelay = 30000,
            fixedRate = 30000
    )
    public void checkCompletion(){
        log.info("Checking Auctions to start...");
        int started = auctionService.startAuctions();
        log.info("Started Auctions | Count: {}", started);
    }

}
