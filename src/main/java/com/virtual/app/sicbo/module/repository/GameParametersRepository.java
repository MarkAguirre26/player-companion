package com.virtual.app.sicbo.module.repository;

import com.virtual.app.sicbo.module.data.GameParameters;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GameParametersRepository extends JpaRepository<GameParameters, Integer> {
    // You can define custom query methods here if needed

    // Example: Find by user UUID
    GameParameters findByUserUuid(String userUuid);

    @Query(value = "SELECT\n" +
            "Sum(journal.profit) as profit\n" +
            "FROM\n" +
            "journal\n" +
            "WHERE\n" +
            "journal.user_uuid = :userUuid\n",nativeQuery = true)
    Integer currentProfit(@Param("userUuid") String userUuid);

    // You can add more methods based on your requirements
}
