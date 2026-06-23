package com.project.msbidding.client;

import com.project.msbidding.client.dto.AuctionInfoResponse;
import com.project.msbidding.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class AuctionClientForBidding {

    private final RestClient restClient;
    private final String baseUrl;

    public AuctionClientForBidding(RestClient restClient,
                                   @Value("${services.auction.base-url}") String baseUrl) {
        this.restClient = restClient;
        this.baseUrl = baseUrl;
    }

    public AuctionInfoResponse getAuctionInfoResponse(Long auctionId){
        String infoResponseEndPoint = "/internal/auctions/{id}/auction-info";
        String endPoint = baseUrl + infoResponseEndPoint;
        try {
            return restClient.get()
                    .uri(endPoint, auctionId)
                    .retrieve()
                    .body(AuctionInfoResponse.class);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new NotFoundException("Auction not found with id: " + auctionId);
        } catch (RestClientException ex) {
            throw new IllegalStateException("Auction service is not available");
        }
    }

}
