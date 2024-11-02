package com.virtual.app.sicbo.module.services.impl;

import com.virtual.app.sicbo.module.data.GameParameters;
import com.virtual.app.sicbo.module.repository.GameParametersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GameParametersService {

    private final GameParametersRepository gameParametersRepository;

    @Autowired
    public GameParametersService(GameParametersRepository gameParametersRepository) {
        this.gameParametersRepository = gameParametersRepository;
    }

    public Optional<GameParameters> getGameParametersByUserUuid(String userUuid) {
        return Optional.ofNullable(gameParametersRepository.findByUserUuid(userUuid));
    }

    public GameParameters saveGameParameters(GameParameters gameParameters) {

        return gameParametersRepository.save(gameParameters);
    }

    public Integer getCurrentProfit(String userUuid) {
        return gameParametersRepository.currentProfit(userUuid);
    }

    // Additional service methods can be defined here
}
