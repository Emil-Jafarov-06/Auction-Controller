package com.project.msauction.client;

import com.project.msauction.client.dto.BidInfoResponse;
import com.project.msauction.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class BiddingClientForAuction {

    private final RestClient restClient;
    public final String baseUrl;

    public BiddingClientForAuction(RestClient restClient, @Value("${services.bidding.base-url}") String baseUrl){
        this.restClient = restClient;
        this.baseUrl = baseUrl;
    }

    public BidInfoResponse findHighestBidInfo(Long auctionId) {
        String infoResponseEndPoint = "/internal/biddings/{id}/bid-info";
        String endPoint = baseUrl + infoResponseEndPoint;
        try{
            return restClient.get()
                    .uri(endPoint, auctionId)
                    .retrieve()
                    .body(BidInfoResponse.class);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new NotFoundException("Auction not found with id: " + auctionId);
        } catch (RestClientException ex) {
            throw new IllegalStateException("Auction service is not available");
        }
    }

}
