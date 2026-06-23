package com.project.msauction.auction.mapper;


import com.project.msauction.auction.model.Auction;
import com.project.msauction.auction.model.dto.AuctionFilter;
import com.project.msauction.auction.model.dto.ExpiredAuctionsUpdate;
import com.project.msauction.client.dto.BidInfoResponse;
import com.project.msauction.enums.AuctionStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AuctionMapper {

    Auction findById(@Param("id") Long id);

    Auction insertAuction(Auction auction);

    int updateStatus(@Param("id") Long id, @Param("status") AuctionStatus status);

    int updateWinner(@Param("id") Long id, @Param("winnerId") Long winnerId);

    List<Long> findExpiredActiveAuctions();

    int startAuctions();

    List<Auction> findFiltered(@Param("filter") AuctionFilter filter, @Param("limit") int limit, @Param("offset") int offset);

    long countFiltered(@Param("filter") AuctionFilter filter);

    int finishExpiredAuctions(@Param("updates") List<ExpiredAuctionsUpdate> updates);

}
