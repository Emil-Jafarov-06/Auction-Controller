package com.project.msauction.auction.mapper;


import com.project.msauction.auction.model.Auction;
import com.project.msauction.enums.AuctionStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AuctionMapper {

    Auction findById(@Param("id") Long id);

    List<Auction> findAll();

    List<Auction> findAllByStatus(@Param("status") AuctionStatus status);

    Auction insertAuction(Auction auction);

    int updateStatus(@Param("id") Long id, @Param("status") AuctionStatus status);

    int updateWinner(@Param("id") Long id, @Param("winnerId") Long winnerId);

    int finishExpiredAuctions();

    int startAuctions();
}
