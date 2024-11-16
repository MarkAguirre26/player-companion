package com.virtual.app.sicbo.module.repository;

import com.virtual.app.sicbo.module.data.GameStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GameStatusRepository extends JpaRepository<GameStatus, Integer> {
    // Additional query methods (if needed) can be defined here
    void deleteByGameResponseId(Integer gameResponseId);

@Query(value = "SELECT\n" +
        "gs.game_status_id,\n" +
        "gs.game_response_id,\n" +
        "gs.hand_count,\n" +
        "gs.wins,\n" +
        "gs.losses,\n" +
        "gs.profit,\n" +
        "gs.playing_units,\n" +
        "gs.date_last_updated\n" +
        "FROM\n" +
        "game_status_sicbo AS gs\n" +
        "WHERE\n" +
        "gs.game_response_id = :gameResponseId\n" +
        "ORDER BY\n" +
        "gs.game_status_id ASC\n" +
        "LIMIT 1\n", nativeQuery = true)
    GameStatus findByGameResponseId(@Param("gameResponseId") Integer gameResponseId);

}
