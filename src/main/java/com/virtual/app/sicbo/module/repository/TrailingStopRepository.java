package com.virtual.app.sicbo.module.repository;

import com.virtual.app.sicbo.module.data.TrailingStop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TrailingStopRepository extends JpaRepository<TrailingStop, Integer> {


    @Query(value = "SELECT\n" +
            "ts.trialing_stop_id,\n" +
            "ts.user_uuid,\n" +
            "ts.initial,\n" +
            "ts.trailing_percent,\n" +
            "ts.stop_profit,\n" +
            "ts.highest_profit\n" +
            "FROM\n" +
            "trailing_stop AS ts\n" +
            "WHERE\n" +
            "ts.user_uuid = :userUuid\n" +
            "ORDER BY\n" +
            "ts.trialing_stop_id ASC\n" +
            "LIMIT 1\n", nativeQuery = true)
    TrailingStop findByUserUuidLimit1(@Param("userUuid") String userUuid);
}