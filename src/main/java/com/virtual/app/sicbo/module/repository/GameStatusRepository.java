package com.virtual.app.sicbo.module.repository;

import com.virtual.app.sicbo.module.data.GameStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameStatusRepository extends JpaRepository<GameStatus, Integer> {
    // Additional query methods (if needed) can be defined here
    void deleteByGameResponseId(Integer gameResponseId);


    GameStatus findByGameResponseId(Integer gameResponseId);

}
