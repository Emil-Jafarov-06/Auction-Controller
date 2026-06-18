package com.team4.auctioncontroller.bidder.repository;

import com.team4.auctioncontroller.bidder.model.entity.Bidder;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BidderRepository extends JpaRepository<Bidder, Long> {

    @Query(value = "select b from Bidder b where b.id = :id and b.deleted = false", nativeQuery = false)
    Bidder findByIdAndDeletedFalse(@Param("id") Long id);

    boolean existsBidderByEmail(String email);

    boolean existsBidderByPin(String pin);
}
