package com.virtual.app.sicbo.module.controllers;

import com.virtual.app.sicbo.module.common.concrete.Strategies;
import com.virtual.app.sicbo.module.data.GameParameters;
import com.virtual.app.sicbo.module.model.UserPrincipal;
import com.virtual.app.sicbo.module.services.impl.GameParametersService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/game")
public class GameParametersController {

    private static final Logger logger = LoggerFactory.getLogger(GameParametersController.class);
    private final GameParametersService gameParametersService;


    @Value("${game.sicbo}")
    public String gameSicbo;


    public GameParametersController(GameParametersService gameParametersService) {
        this.gameParametersService = gameParametersService;
    }

    private static int calculateBetAmount(int currentFund, int startingBetUnit) {
        // Calculate the bet amount based on current fund and starting bet unit
        return currentFund / startingBetUnit;
    }

    // Function to calculate the current fund
    public int calculateCurrentFund(double startingFund, int profit, double unitValue) {
        // Calculate the current fund and cast it to int
        return (int) (startingFund + (profit * unitValue));
    }

    // Function to calculate the unit value based on starting fund and daily goal percentage
    public int calculateUnitValue(double startingFund) {
        // Calculate the unit value directly rounded to the nearest 10
        return (int) (Math.round((startingFund / 100) / 10.0) * 10);
    }



    @GetMapping("/platform")
    public String getPlatform() {
        boolean isSicBoEnabled = Boolean.parseBoolean(gameSicbo);
        if(isSicBoEnabled){
            return "SicBo";
        }
        return "Baccarat";
    }


    @PostMapping("/parameters")
    public ResponseEntity<GameParameters> saveGameParameters(@RequestBody GameParameters gameParameters) {

        logger.info("Saving game parameters for user: {}", gameParameters);
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Optional<GameParameters> userGameParameters = gameParametersService.getGameParametersByUserUuid(userPrincipal.getUserUuid());

        if (userGameParameters.isPresent()) {
            gameParameters.setParameterId(userGameParameters.get().getParameterId());
            gameParameters.setUserUuid(userGameParameters.get().getUserUuid());
        }

        int sl  = gameParameters.getStopLoss() * -1;
        gameParameters.setStopLoss(sl);

        logger.info("Saved game parameters: {}", gameParameters);
        GameParameters savedParameters = gameParametersService.saveGameParameters(gameParameters);
        return ResponseEntity.ok(savedParameters);
    }

    @GetMapping("/parameters")
    public ResponseEntity<GameParameters> getGameParameters() {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        logger.info("Fetching game parameters for user UUID: {}", userPrincipal.getUserUuid());

        Optional<GameParameters> gameParameters = gameParametersService.getGameParametersByUserUuid(userPrincipal.getUserUuid());

        if (gameParameters.isEmpty()) {
            logger.warn("No existing game parameters found for user UUID: {}. Creating new parameters.", userPrincipal.getUserUuid());
            GameParameters newUserParams = new GameParameters();
            newUserParams.setUserUuid(userPrincipal.getUserUuid());
            newUserParams.setStopLoss(0);
            newUserParams.setStopProfit(0);
            newUserParams.setTrailingStopSpread(0);
            newUserParams.setTrailingStopActivation(0);
            newUserParams.setMoneyManagement(Strategies.FLAT.getValue());
            newUserParams.setIsShield(0);
            newUserParams.setIsCompounding(0);
            newUserParams.setStopTrigger(0);
            newUserParams.setSensitivity(55);
            newUserParams.setVirtualWin(0);
            newUserParams.setStartingFund(0);
            newUserParams.setCurrentFund(0);
            newUserParams.setDailyGoalPercentage(0);
            newUserParams.setBetAmount(0);
            newUserParams.setCreatedAt(LocalDateTime.now());
            gameParametersService.saveGameParameters(newUserParams);
            // Optionally, save the new user parameters to the database if needed
            // gameParametersService.save(newUserParams);

            logger.info("New game parameters created for user UUID: {}", userPrincipal.getUserUuid());
            return ResponseEntity.ok(newUserParams);
        }

        // Retrieve the current profit for the user
        int profit = gameParametersService.getCurrentProfit(userPrincipal.getUserUuid()) == null ? 0 : gameParametersService.getCurrentProfit(userPrincipal.getUserUuid());

    // Initialize currentFund and betAmount
        int currentFund = 0;
        int betAmount = 0;

    // Get the compounding flag; default to 0 if null
//        int isCompounding = gameParameters.get() != null && gameParameters.get().getIsCompounding() != null
//                ? gameParameters.get().getIsCompounding()
//                : 0;

    // Calculate current fund and bet amount based on compounding status

        // Calculate bet amount from starting fund
        betAmount = calculateUnitValue(gameParameters.get().getStartingFund());

        // Calculate current fund based on starting fund and profit
        currentFund = calculateCurrentFund(gameParameters.get().getStartingFund(), profit, betAmount);

        if(gameParameters.get().getTrailingStopActivation() == null){
            gameParameters.get().setTrailingStopActivation(0);
            gameParameters.get().setTrailingStopSpread(0);
        }

        if(gameParameters.get().getStopTrigger() == null){
            gameParameters.get().setStopTrigger(0);
            gameParameters.get().setVirtualWin(0);
        }

        gameParameters.get().setBetAmount(betAmount);
        gameParameters.get().setCurrentFund(currentFund);

        gameParameters.get().setStopLoss(gameParameters.get().getStopLoss() * -1);

        logger.info("Game parameters retrieved for user UUID: {}", userPrincipal.getUserUuid());
        gameParameters.get().setParameterId(0);
        gameParameters.get().setUserUuid("");
        return ResponseEntity.of(gameParameters);
    }


}
