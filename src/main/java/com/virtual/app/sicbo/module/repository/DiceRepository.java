package com.virtual.app.sicbo.module.repository;


import com.virtual.app.sicbo.module.data.sicbo.Dice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DiceRepository extends JpaRepository<Dice, Integer> {
    // Custom query methods can be added here if needed


    Optional<Dice> findByWhen(String when);


    @Query(value = "SELECT COUNT(DISTINCT useruuid) AS activeUsers " +
            "FROM game_response_sicbo " +
            "WHERE DATE_FORMAT(date_last_updated, '%Y-%m-%d') = CURRENT_DATE()",
            nativeQuery = true)
    Integer getActiveUsers();


@Query(value = "SELECT\n" +
        "    d.dice_id,\n" +
        "    d.`when`,\n" +
        "    d.sum,\n" +
        "    d.size,\n" +
        "    d.dice_one,\n" +
        "    d.dice_two,\n" +
        "    d.dice_three,\n" +
        "    d.created_at\n" +
        "FROM\n" +
        "    dice d\n" +
        "ORDER BY\n" +
        "    d.dice_id DESC\n" +
        "LIMIT 5;\n" +
        "\n",nativeQuery = true)
    List<Dice> findDiceToday();

}
