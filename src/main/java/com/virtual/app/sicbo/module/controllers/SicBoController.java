package com.virtual.app.sicbo.module.controllers;

import com.virtual.app.sicbo.module.common.concrete.RiskLevel;
import com.virtual.app.sicbo.module.common.concrete.Strategies;
import com.virtual.app.sicbo.module.common.concrete.UserConfig;
import com.virtual.app.sicbo.module.data.*;
import com.virtual.app.sicbo.module.data.response.GameResultResponse;
import com.virtual.app.sicbo.module.data.response.GameResultStatus;
import com.virtual.app.sicbo.module.helper.BaccaratBetting;
import com.virtual.app.sicbo.module.helper.TriggerFinder;
import com.virtual.app.sicbo.module.model.Pair;
import com.virtual.app.sicbo.module.model.UserPrincipal;
import com.virtual.app.sicbo.module.services.TrailingStopService;
import com.virtual.app.sicbo.module.services.impl.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/api/sicBo")
public class SicBoController {


    private static final String STOP_PROFIT_REACHED = "Stop profit reached! Restart the game.";
    private static final String STOP_LOSS_REACHED = "Stop loss reached! Restart the game.";
    private static final String DAILY_LIMIT_REACHED = "Daily limit! Please play again after ";
    private static final String PREDICTION_CONFIDENCE_LOW = "Prediction confidence too low, no bet suggested.";
    private static final String TRAILING_STOP_TRIGGERED_LABEL = "Trailing stop triggered! Restart the game.";
    private static final String PLACE_YOUR_BET = "Place your bet";
    private static final String YOU_WON = "You won!";
    private static final String YOU_LOST = "You lost!";
    private static final int ZERO = 0;
    private static final String ON = "ON";
    private static final String OFF = "OFF";


    private static final Logger logger = LoggerFactory.getLogger(SicBoController.class);


    private final MarkovChain markovChain;
    private final JournalServiceImpl journalService;
    private final GameStatusService gameStatusService;
    private final GameResponseService gameResponseService;
    private final TrailingStopService trailingStopService;
    private final GamesArchiveService gamesArchiveService;
    private final UserConfigService configService;
    private final WebSocketMessageService messageService;
    private final GameParametersService gameParametersService;
    private final DiceService diceService;

    private final String WAIT = "Wait..";
    private final String SMALL = "Small";
    private final String BIG = "Big";
    //    private final String WAIT_FOR_VIRTUAL_WIN = "Wait for virtual win.";
    private final String VIRTUAL_WON = "Virtual won!";


    @Autowired
    public SicBoController(MarkovChain markovChain, JournalServiceImpl journalService, GameStatusService gameStatusService, GameResponseService gameResponseService, TrailingStopService trailingStopService, GamesArchiveService gamesArchiveService, UserConfigService configService, WebSocketMessageService messageService, GameParametersService gameParametersService, DiceService diceService) {
        this.markovChain = markovChain;

        this.journalService = journalService;
        this.gameStatusService = gameStatusService;
        this.gameResponseService = gameResponseService;
        this.trailingStopService = trailingStopService;
        this.gamesArchiveService = gamesArchiveService;
        this.configService = configService;

        this.messageService = messageService;
        this.gameParametersService = gameParametersService;

        this.diceService = diceService;
    }


    @PostMapping("/play")
    public GameResultResponse play(@RequestParam String userInput,
                                   @RequestParam String recommendedBet,
                                   @RequestParam int suggestedUnit
    ) {

        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userName = userPrincipal.getUsername();

        logger.info(userName + ": Received user input: {}", userInput);
        logger.info(userName + ": Received recommendedBet input: {}", recommendedBet);
        logger.info(userName + ": Received suggestedUnit input: {}", suggestedUnit);


        return processGame(userInput, recommendedBet, suggestedUnit);
    }

    public GameResultResponse processGame(String userInput, String predictedBet, int suggestedUnit) {


        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userName = userPrincipal.getUsername();

        GameResultResponse existingGame = getGameResponse(userPrincipal);

        String[] diceInputArray = userInput.split(":");
        String diceSizeValue = diceInputArray[0];
        String userInputSmallOrBig = diceInputArray[1];


        if (existingGame.getMessage().equals(TRAILING_STOP_TRIGGERED_LABEL)) {
            logger.info(userName + ": Trailing stop triggered, skipping game");
            return existingGame;
        }




        GameResultResponse gameResultResponse = updateSequenceAndUpdateHandCount(existingGame, diceSizeValue, userInputSmallOrBig);


        // Generate predictions using Markov chain and pattern recognition
        String sequence = gameResultResponse.getSequence();
        Optional<Pair<Character, Double>> markovPrediction = markovChain.predictNext(sequence);

        // Combine predictions and handle the bet
        Pair<Character, Double> combinedPrediction = combinePredictions(markovPrediction);


        return handleBet(gameResultResponse, diceSizeValue, combinedPrediction, predictedBet, suggestedUnit);
//    return  validateGameResult(suggestedUnit, betSize, predictedBet, userInput, gameResultResponse, recommendedBet, skipState);
    }

    public void initialize(GameResultResponse game) {


        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userUuid = userPrincipal.getUserUuid();

        GameResponse gameResponse;

        gameResponse = new GameResponse();
        gameResponse.setUserUuid(userUuid);
        gameResponse.setBaseBetUnit(game.getBaseBetUnit());
        gameResponse.setSuggestedBetUnit(game.getSuggestedBetUnit());
        gameResponse.setInitialPlayingUnits(game.getInitialPlayingUnits());
        gameResponse.setLossCounter(game.getLossCounter());
        gameResponse.setRecommendedBet(game.getRecommendedBet());
        gameResponse.setSequence(game.getSequence());
        gameResponse.setDiceNumber(game.getDiceSum());
        gameResponse.setMessage("Initialized");
        gameResponse.setRiskLevel(game.getRiskLevel());
        gameResponse.setDateLastUpdated(LocalDateTime.now());
        gameResponseService.createOrUpdateGameResponse(gameResponse);

        GameResponse g = gameResponseService.getGameResponseByUserUuid(userUuid);

        GameStatus s = new GameStatus();
        s.setGameResponseId(g.getGameResponseId());
        s.setHandCount(game.getGameStatus().getHandCount());
        s.setWins(game.getGameStatus().getWins());
        s.setLosses(game.getGameStatus().getLosses());
        s.setProfit(game.getGameStatus().getProfit());
        s.setPlayingUnits(game.getGameStatus().getPlayingUnits());
        s.setDateLastUpdated(LocalDateTime.now());
        gameStatusService.save(s);

//        setStrategy(Strategies.FLAT.getValue());
        freezeState(OFF);
    }

    private GameResultResponse saveAndReturn(GameResultResponse response) {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userUuid = userPrincipal.getUserUuid();

//        String strategy = currentStrategy().equals(Strategies.FLAT.getValue()) ? "FLAT" : currentStrategy();
        String strategy = getGameParameters().getMoneyManagement();
        String winLose = response.getGameStatus().getWins() + "/" + response.getGameStatus().getLosses();
        Journal savedJournal = journalService.saveJournal(new Journal(ZERO, userUuid, winLose, response.getGameStatus().getHandCount(),
                response.getGameStatus().getProfit(), strategy.toLowerCase()));

        if (savedJournal != null) {

            GameResultStatus gameResultStatus = response.getGameStatus();

            gamesArchiveService.addGameArchive(new GamesArchive(savedJournal.getJournalId(), response.getBaseBetUnit(),
                    response.getSuggestedBetUnit(), response.getLossCounter(), response.getRecommendedBet(),
                    response.getSequence(), response.getHandResult(), response.getSkipState(), "Archived", gameResultStatus.getHandCount(),
                    gameResultStatus.getWins(), gameResultStatus.getLosses(), gameResultStatus.getProfit(),
                    gameResultStatus.getPlayingUnits(), response.getRiskLevel()));
        }


        return response;

    }

//    private String currentStrategy() {
//
//        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        String userUuid = userPrincipal.getUserUuid();
//        Config strategyConfig = configService.findByUserUuidAndName(userUuid, Strategies.STRATEGY.getValue())
//                .stream()
//                .filter(config -> config.getName().equals(Strategies.STRATEGY.getValue()))
//                .findFirst()
//                .orElse(null);
//
//        if (strategyConfig != null) {
//            return strategyConfig.getValue();
//        } else {
//            setStrategy(Strategies.FLAT.getValue());
//            return Strategies.FLAT.getValue();
//        }
//
//    }

    private GameResultResponse updateSequenceAndUpdateHandCount(GameResultResponse existingGame, String diceSizeValue, String diceSum) {

        // Fetch the current game response once

        GameResultStatus gameStatus = existingGame.getGameStatus();
        // Update the sequence and hand count
        String sequence = existingGame.getSequence() == null ? "1111" : existingGame.getSequence() + diceSizeValue;
        String diceSums = existingGame.getDiceSum() == null ? "1111" : existingGame.getDiceSum() + "," + diceSum;
        int handCount = gameStatus == null ? 1 : gameStatus.getHandCount() + 1;

        assert gameStatus != null;

        gameStatus.setHandCount(handCount);
        existingGame.setGameStatus(gameStatus);
        existingGame.setSequence(sequence);
        existingGame.setDiceSum(diceSums);

        return existingGame;
    }


    private boolean hasReachedStopLoss(GameResultResponse gameResultResponse) {

        GameParameters gameParameters = getGameParameters();
        int STOP_LOSS_UNIT = gameParameters.getStopLoss();

        if (STOP_LOSS_UNIT == 0) {
            return false;
        }

        int profit = gameResultResponse.getGameStatus().getProfit();

        return profit <= STOP_LOSS_UNIT;
    }


    private GameResultResponse handleBet(GameResultResponse gameResultResponse, String diceSizeValue,
                                         Pair<Character, Double> combinedPrediction, String predictedBet,
                                         int suggestedUnit) {


        String prediction = "";
        String recommendedBet = "";

        GameParameters gameParameters = getGameParameters();


        int sensitivity = gameParameters.getSensitivity() == null ? 55 : gameParameters.getSensitivity();
        double CONFIDENCE_THRESHOLD = (double) sensitivity / 100;


        if (combinedPrediction.second < CONFIDENCE_THRESHOLD) {

            gameResultResponse.setMessage(PREDICTION_CONFIDENCE_LOW);

            gameResultResponse.setSequence(gameResultResponse.getSequence());
            gameResultResponse.setDiceSum(gameResultResponse.getDiceSum());


        }
        prediction = String.valueOf(combinedPrediction.first);
        recommendedBet = Objects.equals(prediction, "s") ? SMALL : BIG;

        gameResultResponse.setConfidence(combinedPrediction.second);


        return validateGameResult(suggestedUnit, predictedBet, diceSizeValue, gameResultResponse, recommendedBet);
    }


    private GameResultResponse validateGameResult(int suggestedUnit, String predictedBet, String diceSizeValue,
                                                  GameResultResponse gameResultResponse, String nextPredictedBet) {


        GameResultResponse game = updateProfitAndFund(predictedBet, diceSizeValue, gameResultResponse, suggestedUnit);

        return settleLimitAndValidation(game, nextPredictedBet);

    }

    private GameParameters getGameParameters() {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return gameParametersService.getGameParametersByUserUuid(userPrincipal.getUserUuid()).get();
    }

    private GameResultResponse settleLimitAndValidation(GameResultResponse gameResultResponse, String nextPredictedBet) {


        int profit = gameResultResponse.getGameStatus().getProfit();
        int playingUnit = gameResultResponse.getGameStatus().getPlayingUnits();


        gameResultResponse.setSequence(gameResultResponse.getSequence());
        gameResultResponse.setDiceSum(gameResultResponse.getDiceSum());

//        gameResultResponse.setRecommendedBet(WAIT);


        if (hasReachedStopLoss(gameResultResponse)) {
            logger.warn(": Reached stop loss. New profit: {}, New playing fund: {}", profit, playingUnit);
            gameResultResponse.setMessage(STOP_LOSS_REACHED);
            gameResultResponse.setSuggestedBetUnit(0);
//            gameResultResponse.setRecommendedBet(WAIT);

            return provideGameResponse(gameResultResponse);
        } else {

            String currentStrategy = getGameParameters().getMoneyManagement();
            //            ---------------------MONEY MANAGEMENT-------------------------------------
            int betSize = 0;
            if (gameResultResponse.getSkipState() != null) {

                if (currentStrategy.equals(Strategies.FLAT.getValue())) {
                    betSize = 1;
                } else if (currentStrategy.equals(Strategies.STOCHASTIC.getValue())) {

                    betSize = BaccaratBetting.stochasticBetting(gameResultResponse);
                } else if (currentStrategy.equals(Strategies.KISS_MODIFIED.getValue())) {
                    betSize = BaccaratBetting.kissModifiedBetting(gameResultResponse);
                }
            }



//
            //            ----------------------------------------------------------

            gameResultResponse.setRecommendedBet(nextPredictedBet);

            GameParameters gameParameters = getGameParameters();
            int stopTrigger = gameParameters.getStopTrigger();

            if (gameResultResponse.getLossCounter() >= stopTrigger) {
                gameResultResponse.setSuggestedBetUnit(0);
            }


            GameResultResponse gameResultResponseWithTrailingStop = trailingStop(gameResultResponse, false);
            //evaluate trailing stop
            if (gameParameters.getTrailingStopSpread() > 0 && gameParameters.getTrailingStopActivation() > 0) {
                if (gameResultResponseWithTrailingStop.getMessage().equals(TRAILING_STOP_TRIGGERED_LABEL)) {
                    return provideGameResponse(gameResultResponseWithTrailingStop);
                }
            }

            // code below will not be executed if the above condition is true
            gameResultResponse.setTrailingStop(gameResultResponseWithTrailingStop.getTrailingStop());


            double confidence = gameResultResponse.getConfidence() == null ? 0 : gameResultResponse.getConfidence();
            gameResultResponse.setConfidence(confidence);


            int stopProfit = gameParameters.getStopProfit();


            if (stopProfit > 0) {
                if (gameResultResponse.getGameStatus().getProfit() >= stopProfit) {
                    gameResultResponse.setMessage(STOP_PROFIT_REACHED);
                    return provideGameResponse(gameResultResponse);
                }
            }


//          SET FREEZE STATE----------------------------------------------------

            int virtualWin = gameParameters.getVirtualWin();

            if (stopTrigger > 0 && virtualWin > 0) {


                if (gameResultResponse.getHandResult() != null) {

                    String stopTriggerKey = "L".repeat(stopTrigger);
                    String stopTriggerKeyValue = TriggerFinder.getLastPart(gameResultResponse.getHandResult(), stopTriggerKey);

                    if (stopTriggerKey.equals(stopTriggerKeyValue)) {
                        saveFreezeState(ON);
                    }


                    String virtualWinKey = "W".repeat(virtualWin);

                    boolean isGoodToBet = TriggerFinder.isGoodToBet(gameResultResponse.getHandResult(), virtualWinKey);
                    if (isGoodToBet) {
                        saveFreezeState(OFF);
                    } else {
//                        saveFreezeState(ON);
                    }


                    if (gameResultResponse.getVirtualWin() >= gameParameters.getVirtualWin()) {


                        gameResultResponse.setHandResult(gameResultResponse.getHandResult() + "W");
                        gameResultResponse.setMessage(PLACE_YOUR_BET);
                        gameResultResponse.setLossCounter(0);
                        gameResultResponse.setSuggestedBetUnit(betSize);

                    }

                }
            } else {
                saveFreezeState(OFF);
            }


            int sensitivity = gameParameters.getSensitivity() == null ? 55 : gameParameters.getSensitivity();
            double CONFIDENCE_THRESHOLD = (double) sensitivity / 100;
            if (gameResultResponse.getConfidence() < CONFIDENCE_THRESHOLD) {
                gameResultResponse.setMessage(PREDICTION_CONFIDENCE_LOW);
            }

            System.out.println("CONFIDENCE_THRESHOLD:"+CONFIDENCE_THRESHOLD);
            System.out.println("confidence:"+gameResultResponse.getConfidence());

            String isSKippedValue = isFrozen() ? "Y" : "N";
            String currentSkipState = gameResultResponse.getSkipState() == null ? "" : gameResultResponse.getSkipState();
            gameResultResponse.setSkipState(currentSkipState + isSKippedValue);

            if (!isFrozen() && !gameResultResponse.getMessage().equals(PREDICTION_CONFIDENCE_LOW)) {
                gameResultResponse.setSuggestedBetUnit(betSize);
            } else {
                gameResultResponse.setSuggestedBetUnit(0);
            }


            return provideGameResponse(gameResultResponse);
        }

    }


    private GameResultResponse trailingStop(GameResultResponse gameResultResponse, boolean isUndo) {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        int profit = gameResultResponse.getGameStatus().getProfit();
        GameParameters gameParameters = getGameParameters();

        int TRAILING_STOP_ACTIVATION = gameParameters.getTrailingStopActivation();

        if (gameResultResponse.getGameStatus().getProfit() >= TRAILING_STOP_ACTIVATION) {


            int TRAILING_STOP_SPREAD = gameParameters.getTrailingStopSpread();

            int currentProfit = gameResultResponse.getGameStatus().getProfit();

            double stopProfit = currentProfit - TRAILING_STOP_SPREAD;

            TrailingStop existingTrailingStop = trailingStopService.getTrailingStopByUserUuid(userPrincipal.getUserUuid());

            if (existingTrailingStop == null) {
                trailingStopService.saveOrUpdate(new TrailingStop(userPrincipal.getUserUuid(), currentProfit
                        , TRAILING_STOP_SPREAD, stopProfit, currentProfit));
            } else {

                if (!isUndo) {
                    if (profit > existingTrailingStop.getHighestProfit()) {
                        existingTrailingStop.setHighestProfit(profit);
                        existingTrailingStop.setStopProfit(stopProfit);
                        trailingStopService.saveOrUpdate(existingTrailingStop);
                    }
                } else {
                    existingTrailingStop.setHighestProfit(profit);
                    existingTrailingStop.setStopProfit(stopProfit);
                    trailingStopService.saveOrUpdate(existingTrailingStop);
                }
            }

        } else {
            if (isUndo) {
                trailingStopService.deleteTrailingStopByUserUuid(userPrincipal.getUserUuid());
            }
        }

        String trailingStop = evaluateTrailingStop(userPrincipal.getUserUuid(), profit);
        if (trailingStop.equals(TRAILING_STOP_TRIGGERED_LABEL)) {
            gameResultResponse.setRecommendedBet(WAIT);
            gameResultResponse.setSuggestedBetUnit(0);
            gameResultResponse.setMessage(trailingStop);
        } else {
            gameResultResponse.setTrailingStop(trailingStop);
        }
        gameResultResponse.setSequence(gameResultResponse.getSequence().replace("1111", ""));
        gameResultResponse.setDiceSum(gameResultResponse.getDiceSum().replace("1111", ""));

        return gameResultResponse;
    }

    public String evaluateTrailingStop(String userUuid, int currentProfit) {

        if (getGameParameters().getTrailingStopActivation() == 0 && getGameParameters().getTrailingStopSpread() == 0) {
            return "";
        }

        TrailingStop trailingStop = trailingStopService.getTrailingStopByUserUuid(userUuid);

        if (trailingStop == null) {
            return "";
        }

        double stopProfit = trailingStop.getStopProfit();
        int highestProfit = trailingStop.getHighestProfit();
        double trailingPercent = trailingStop.getTrailingPercent();


        if (currentProfit > highestProfit) {
            highestProfit = currentProfit;
            stopProfit = highestProfit * (1 - trailingPercent); // Recalculate the stop price

            trailingStopService.saveOrUpdate(new TrailingStop(trailingStop.getTrailingStopId(), userUuid,
                    trailingStop.getInitial(), trailingStop.getTrailingPercent(), stopProfit, highestProfit));
        }

        // Check if the stop price has been triggered
        if (currentProfit <= stopProfit) {
            return TRAILING_STOP_TRIGGERED_LABEL;
        } else {
            System.out.println("highestProfit: " + highestProfit);
            System.out.println("Current Profit: " + currentProfit + ", Stop Profit: " + stopProfit);
            return stopProfit + "";
        }
    }

    private boolean isFrozen() {
        String userUuid = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUserUuid();
        return configService.findByUserUuidAndName(userUuid, UserConfig.FREEZE.getValue())
                .map(Config::getValue)
                .map(ON::equals)
                .orElse(false);
    }


    private GameResultResponse updateProfitAndFund(String predictedBet, String diceSizeValue, GameResultResponse gameResultResponse, int suggestedUnit) {

        GameParameters gameParameters = getGameParameters();

        int profit = gameResultResponse.getGameStatus().getProfit();
        int playingUnit = gameResultResponse.getGameStatus().getPlayingUnits();
        int totalWins = gameResultResponse.getGameStatus().getWins();
        int totalLosses = gameResultResponse.getGameStatus().getLosses();
        int currentLossCount = gameResultResponse.getLossCounter();

        String previousPrediction = predictedBet.equals(SMALL) ? "s" : predictedBet.equals(BIG) ? "b" : "t";


        if (previousPrediction.equals(diceSizeValue)) {


            profit = (isFrozen() ? profit : profit + suggestedUnit);
            playingUnit += suggestedUnit;
            totalWins++;
            currentLossCount = 0;
            gameResultResponse.setHandResult(gameResultResponse.getHandResult() + "W");
            gameResultResponse.setMessage(PLACE_YOUR_BET);


        } else {
            if (!gameResultResponse.getSequence().contains("1111")) {


                if (gameParameters.getStopTrigger() > 0) {
                    currentLossCount++;
                }

                profit = (isFrozen() ? profit : profit - suggestedUnit);
                playingUnit -= suggestedUnit;
                totalLosses++;
                gameResultResponse.setHandResult(gameResultResponse.getHandResult() + "L");
            }
        }

        GameResultStatus gameResultStatus = gameResultResponse.getGameStatus();
        gameResultStatus.setProfit(profit);
        gameResultStatus.setPlayingUnits(playingUnit);
        gameResultStatus.setWins(totalWins);
        gameResultStatus.setLosses(totalLosses);
        gameResultResponse.setLossCounter(currentLossCount);
        gameResultResponse.setGameStatus(gameResultStatus);

        return gameResultResponse;
    }


    @PostMapping("/freeze-state")
    public ResponseEntity<String> freezeState(@RequestParam String onOff) {
        try {

            saveFreezeState(onOff);
            // Return success with the updated value
            return ResponseEntity.ok(onOff);

        } catch (Exception e) {
            e.printStackTrace(); // Replace with proper logging

            // Return Internal Server Error in case of failure
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Operation failed");
        }
    }

    private void saveFreezeState(String onOff) {
        // Get the current authenticated user
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userUuid = userPrincipal.getUserUuid();

        // Find the freeze configuration or create a new one if it doesn't exist
        Config freezeConfig = configService.findByUserUuidAndName(userUuid, UserConfig.FREEZE.getValue())
                .orElseGet(() -> new Config(userUuid, UserConfig.FREEZE.getValue(), "OFF"));

        logger.info("Saving freeze state for {}", freezeConfig);
        // Update the value of the freeze configuration
        freezeConfig.setValue(onOff);

        // Save or update the configuration
        configService.saveOrUpdateConfig(freezeConfig);
    }

    @GetMapping("/get-freeze-state")
    public ResponseEntity<String> freezeState() {
        try {
            // Get the current authenticated user
            UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String userUuid = userPrincipal.getUserUuid();

            // Find the freeze configuration or create a new one if it doesn't exist
            Config freezeConfig = configService.findByUserUuidAndName(userUuid, UserConfig.FREEZE.getValue())
                    .orElseGet(() -> new Config(userUuid, UserConfig.FREEZE.getValue(), "OFF"));

            // Update the value of the freeze configuration
            freezeConfig.setValue(freezeConfig.getValue());


            // Return success with the updated value
            return ResponseEntity.ok(freezeConfig.getValue());

        } catch (Exception e) {
            e.printStackTrace(); // Replace with proper logging

            // Return Internal Server Error in case of failure
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Operation failed");
        }
    }

//    @GetMapping("/strategy")
//    public ResponseEntity<String> getStrategy() {
//
//        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        String userUuid = userPrincipal.getUserUuid();
//        Config strategyConfig = configService.getConfigsByUserUuid(userUuid)
//                .stream()
//                .filter(config -> config.getName().equals(Strategies.STRATEGY.getValue()))
//                .findFirst()
//                .orElse(null);
//
//        if (strategyConfig != null) {
//            return ResponseEntity.ok(strategyConfig.getValue());
//        }
//        return ResponseEntity.ok(Strategies.FLAT.getValue());
//    }

//    @PostMapping("/strategy")
//    public ResponseEntity<String> setStrategy(@RequestParam String strategy) {
//        try {
//            // Get the current authenticated user
//            UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//            String userUuid = userPrincipal.getUserUuid();
//
//            // Find the freeze configuration or create a new one if it doesn't exist
//            Config strategyConfig = configService.findByUserUuidAndName(userUuid, Strategies.STRATEGY.getValue())
//                    .orElseGet(() -> new Config(userUuid, Strategies.STRATEGY.getValue(), Strategies.FLAT.getValue()));
//
//            // Update the value of the freeze configuration
//            strategyConfig.setValue(strategy);
//
//
//            // Save or update the configuration
//            configService.saveOrUpdateConfig(strategyConfig);
//
//            // Return success with the updated value
//            return ResponseEntity.ok("ok");
//
//        } catch (Exception e) {
//            e.printStackTrace(); // Replace with proper logging
//
//            // Return Internal Server Error in case of failure
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Operation failed");
//        }
//
//    }


    @GetMapping("/current-state")
    public GameResultResponse getCurrentState(@RequestParam String message) {

        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        logger.info(userPrincipal.getUsername() + ": Fetching current game state. " + message);


        return getGameResponse(userPrincipal);
    }


    @PostMapping("/undo")
    public GameResultResponse back() {

        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        GameResultResponse existingGame = getGameResponse(userPrincipal);


        if (existingGame.getMessage().equals(TRAILING_STOP_TRIGGERED_LABEL)) {
            return existingGame;
        }


        Optional<GameResponse> g2 = gameResponseService.findFirstByUserUuidOrderByGameResponseIdDesc(userPrincipal.getUserUuid());
        GameResponse gameResponseToDelete = g2.get();

        gameResponseService.deleteGameResponse(gameResponseToDelete.getGameResponseId());

        GameStatus gameStatus = gameStatusService.findByGameResponseId(gameResponseToDelete.getGameResponseId()).get();
        gameStatusService.deleteById(gameStatus.getGameStatusId());

        GameResultResponse newGameResponse = getGameResponse(userPrincipal);
        trailingStop(newGameResponse, true);

        return newGameResponse;

    }


    @PostMapping("/reset")
    public GameResultResponse reset(@RequestParam String yesNo) {
        logger.info("Resetting game state to initial values.");

        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userUuid = userPrincipal.getUserUuid();

        try {

            GameResultResponse grr = getGameResponse(userPrincipal);

            if (yesNo.equalsIgnoreCase("yes")) {
                saveAndReturn(grr);
            }


            deleteGamesByUserUuid(userUuid);
            trailingStopService.deleteTrailingStopByUserUuid(userUuid);

            GameStatus gameStatus = createInitialGameStatus();
            GameResultResponse gameResultResponse = createInitialGameResultResponse(gameStatus);

            initialize(gameResultResponse);

            logger.info(userPrincipal.getUsername() + ": Game state reset!");

            String p = grr.getGameStatus().getProfit() < 0 ? "lost" : "won";
            String message = String.format("%s just %s %s!", userPrincipal.getUsername(), p, grr.getGameStatus().getProfit());


            messageService.sendMessage(message);

            return gameResultResponse;
        } catch (Exception e) {
            logger.error("Error resetting game state", e);
            throw new RuntimeException("Failed to reset game state", e);
        }
    }

    private void deleteGamesByUserUuid(String userUuid) {
        List<GameResponse> games = gameResponseService.findAllByUserUuid(userUuid);
        for (GameResponse game : games) {
            gameResponseService.deleteGameResponse(game.getGameResponseId());
            gameStatusService.deleteById(gameStatusService.findByGameResponseId(game.getGameResponseId()).get().getGameStatusId());
        }
    }


    private GameStatus createInitialGameStatus() {
        GameStatus gameStatus = new GameStatus();
        gameStatus.setHandCount(0);
        gameStatus.setWins(0);
        gameStatus.setLosses(0);
        gameStatus.setProfit(0);
        gameStatus.setPlayingUnits(100);
        return gameStatus;
    }

    private GameResultResponse createInitialGameResultResponse(GameStatus gameStatus) {
        GameResultResponse gameResultResponse = new GameResultResponse();
        gameResultResponse.setSequence("1111");
        gameResultResponse.setDiceSum("1111");
        gameResultResponse.setMessage("Game reset!");
        gameResultResponse.setBaseBetUnit(1);
        gameResultResponse.setInitialPlayingUnits(100);
        gameResultResponse.setSuggestedBetUnit(1);
        gameResultResponse.setLossCounter(0);
        gameResultResponse.setRecommendedBet(WAIT);
        gameResultResponse.setRiskLevel(RiskLevel.LOW.getValue());
        GameResultStatus gameResultStatus = new GameResultStatus(
                gameStatus.getHandCount(),
                gameStatus.getWins(),
                gameStatus.getLosses(),
                gameStatus.getProfit(),
                gameStatus.getPlayingUnits()
        );
        gameResultResponse.setGameStatus(gameResultStatus);

        return gameResultResponse;
    }


    private Pair<Character, Double> combinePredictions(Optional<Pair<Character, Double>> markovResult) {
        // Handle both results being absent
        if (markovResult.isEmpty()) {
            return new Pair<>(null, 0.0);
        }
        return markovResult.get();
    }


    private GameResultResponse getGameResponse(UserPrincipal userPrincipal) {

        // Get the GameResponse for the user
        Optional<GameResponse> g1 = gameResponseService.findFirstByUserUuidOrderByGameResponseIdDesc(userPrincipal.getUserUuid());
        if (g1.isEmpty()) {

            GameStatus gameStatus = createInitialGameStatus();
            GameResultResponse gameResultResponse = createInitialGameResultResponse(gameStatus);

            initialize(gameResultResponse);

        }

        Optional<GameResponse> g2 = gameResponseService.findFirstByUserUuidOrderByGameResponseIdDesc(userPrincipal.getUserUuid());
        GameResponse latesGameResponse = g2.get();


        // Create the GameResultResponse and populate it with default values if gameResponse is null
        GameResultResponse response = new GameResultResponse();

        response.setBaseBetUnit(latesGameResponse.getBaseBetUnit());
        response.setSuggestedBetUnit(latesGameResponse.getSuggestedBetUnit());
        response.setInitialPlayingUnits(latesGameResponse.getInitialPlayingUnits());
        response.setLossCounter(latesGameResponse.getLossCounter());
        response.setRecommendedBet(latesGameResponse.getRecommendedBet());
        response.setSequence(latesGameResponse.getSequence());
        response.setDiceSum(latesGameResponse.getDiceNumber());
        response.setHandResult(latesGameResponse.getHandResult());
        response.setMessage(latesGameResponse.getMessage());
        response.setRiskLevel(latesGameResponse.getRiskLevel());
        response.setConfidence(latesGameResponse.getConfidence());
        response.setVirtualWin(latesGameResponse.getVirtualWin());
        response.setSkipState(latesGameResponse.getSkipState());
        // Get the GameStatus for the user
        GameStatus gameStatus = gameStatusService.findByGameResponseId(latesGameResponse.getGameResponseId()).orElse(null);
        GameResultStatus gameResultStatus;

        if (gameStatus != null) {
            gameResultStatus = new GameResultStatus(gameStatus.getHandCount(), gameStatus.getWins(), gameStatus.getLosses(), gameStatus.getProfit(), gameStatus.getPlayingUnits());

        } else {
            gameResultStatus = new GameResultStatus(0, 0, 0, 0, 100);
        }

        response.setGameStatus(gameResultStatus);


//        TrailingStop trailingStop = trailingStopService.getTrailingStopByUserUuid(userPrincipal.getUserUuid());
//        if (trailingStop != null) {
//            response.setTrailingStop(String.valueOf(trailingStop.getStopProfit()));
//        } else {
//            response.setTrailingStop("0");
//        }
        // Log the response
        logger.info("{}: Game Response-> {}", userPrincipal.getUsername(), response);

        // Return the populated GameResultResponse
        return response;
    }


    public GameResultResponse provideGameResponse(GameResultResponse gameResultResponse) {


        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        GameResponse gameResponse = new GameResponse();// gameResponseService.getGameResponseByUserUuid(userPrincipal.getUserUuid());

        // Update the game response fields
        gameResponse.setUserUuid(userPrincipal.getUserUuid());
        gameResponse.setBaseBetUnit(gameResultResponse.getBaseBetUnit());
        gameResponse.setSuggestedBetUnit(gameResultResponse.getSuggestedBetUnit());
        gameResponse.setLossCounter(gameResultResponse.getLossCounter());
        gameResponse.setInitialPlayingUnits(gameResultResponse.getInitialPlayingUnits());
        gameResponse.setRecommendedBet(gameResultResponse.getRecommendedBet());
        gameResponse.setSequence(gameResultResponse.getSequence());
        gameResponse.setDiceNumber(gameResultResponse.getDiceSum());
        gameResponse.setHandResult(gameResultResponse.getHandResult() == null ? "" : gameResultResponse.getHandResult());
        gameResponse.setMessage(gameResultResponse.getMessage());
        gameResponse.setRiskLevel(gameResultResponse.getRiskLevel());
        gameResponse.setConfidence(gameResultResponse.getConfidence());
        gameResponse.setVirtualWin(gameResultResponse.getVirtualWin());
        gameResponse.setSkipState(gameResultResponse.getSkipState() == null ? "" : gameResultResponse.getSkipState());
//        gameResponse.setDateLastUpdated(LocalDateTime.now());
        // Persist the updated game response
        GameResponse newOneGameResponse = gameResponseService.createOrUpdateGameResponse(gameResponse);


        GameResultStatus gameResultStatus = new GameResultStatus();

        gameResultStatus.setHandCount(gameResultResponse.getGameStatus().getHandCount());
        gameResultStatus.setWins(gameResultResponse.getGameStatus().getWins());
        gameResultStatus.setLosses(gameResultResponse.getGameStatus().getLosses());
        gameResultStatus.setProfit(gameResultResponse.getGameStatus().getProfit());
        gameResultStatus.setPlayingUnits(gameResultResponse.getGameStatus().getPlayingUnits());


        GameStatus gameStatus = new GameStatus();
        gameStatus.setGameResponseId(newOneGameResponse.getGameResponseId());
        gameStatus.setHandCount(gameResultResponse.getGameStatus().getHandCount());
        gameStatus.setWins(gameResultResponse.getGameStatus().getWins());
        gameStatus.setLosses(gameResultResponse.getGameStatus().getLosses());
        gameStatus.setProfit(gameResultResponse.getGameStatus().getProfit());
        gameStatus.setPlayingUnits(gameResultResponse.getGameStatus().getPlayingUnits());
        gameStatus.setDateLastUpdated(LocalDateTime.now());
        gameStatusService.save(gameStatus);

        gameResultResponse.setGameStatus(gameResultStatus);


        return gameResultResponse;
    }


}
