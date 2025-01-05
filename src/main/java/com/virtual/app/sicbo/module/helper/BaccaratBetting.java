package com.virtual.app.sicbo.module.helper;

import com.virtual.app.sicbo.module.data.response.GameResultResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BaccaratBetting {


    public static int kissModifiedBetting(GameResultResponse gameResultResponse) {

        String handResult = gameResultResponse.getHandResult().replace("null", "");
        String skipSequence = gameResultResponse.getSkipState();

        // Ensure both sequences have the same length to avoid errors
        if (handResult.isEmpty()) {
            return 1;
        }

        // 1-2 progression bets
        int initialBet = 1;
        int[] progression = {1, 2};
        int currentStage = 0;
        int currentBetUnit = initialBet;

        // Iterate through the results and skip states
        for (int i = 0; i < handResult.length(); i++) {
            char outcome = handResult.charAt(i);
            char skipOutcome = skipSequence.charAt(i);

            // Only proceed if not skipped
            if (skipOutcome == 'N') {
                if (outcome == 'L') {

                    currentBetUnit = initialBet;
                    // Reset the progression after a loss
                    currentStage = 0;


                } else if (outcome == 'W') {

                    // Move to the next stage in the progression
                    currentStage++;
                    if (currentStage == progression.length) {
                        // Reset progression if the 6-unit stage is reached
                        currentStage = 0;
                    }
                    currentBetUnit = progression[currentStage] * initialBet;
                }
            } else {

            }
        }

        return currentBetUnit;  // Return the last progression value used
    }



    //    1,3,5
    public static int allRed(GameResultResponse gameResultResponse) {

        String handResult = gameResultResponse.getHandResult().replace("null", "");
        String skipSequence = gameResultResponse.getSkipState();

        // Ensure both sequences have the same length to avoid errors
        if (handResult.isEmpty()) {
            return 1;
        }

        // 1-3-2-6 progression bets
        int initialBet = 1;
        int[] progression = {1, 3, 5};
        int currentStage = 0;
        int currentBetUnit = initialBet;

        // Iterate through the results and skip states
        for (int i = 0; i < handResult.length(); i++) {
            char outcome = handResult.charAt(i);
            char skipOutcome = skipSequence.charAt(i);

            // Only proceed if not skipped
            if (skipOutcome == 'N') {
                if (outcome == 'W') {

                    currentBetUnit = initialBet;
                    // Reset the progression after a loss
                    currentStage = 0;


                } else if (outcome == 'L') {

                    // Move to the next stage in the progression
                    currentStage++;
                    if (currentStage == progression.length) {
                        // Reset progression if the 6-unit stage is reached
                        currentStage = 0;
                    }
                    currentBetUnit = progression[currentStage] * initialBet;
                }
            }
        }

        return currentBetUnit;  // Return the last progression value used
    }

    //1,2
    public static int rgp(GameResultResponse gameResultResponse) {

        String handResult = gameResultResponse.getHandResult().replace("null", "");
        String skipSequence = gameResultResponse.getSkipState();

        // Ensure both sequences have the same length to avoid errors
        if (handResult.isEmpty()) {
            return 1;
        }


        int initialBet = 1;
        int[] progression = {1, 2}; // 10th levels
        int currentStage = 0;
        int currentBetUnit = initialBet;

        // Iterate through the results and skip states
        for (int i = 0; i < handResult.length(); i++) {
            char outcome = handResult.charAt(i);
            char skipOutcome = skipSequence.charAt(i);

            // Only proceed if not skipped
            if (skipOutcome == 'N') {

                // Move to the next stage in the progression
                currentStage++;
                if (currentStage == progression.length) {
                    // Reset progression if the 6-unit stage is reached
                    currentStage = 0;
                }
                currentBetUnit = progression[currentStage] * initialBet;

            }
        }

        return currentBetUnit;  // Return the last progression value used
    }



//1,2,4,3,6,9
    public static int hybrid(GameResultResponse gameResultResponse) {

        String handResult = gameResultResponse.getHandResult().replace("null", "");
        String skipSequence = gameResultResponse.getSkipState();

        // Ensure both sequences have the same length to avoid errors
        if (handResult.isEmpty()) {
            return 1;
        }

        //Repeated Geometric Progression (RGP)
        int initialBet = 1;
        int[] progression = {1,2,4,3,6,9}; // 6th levels
        int currentStage = 0;
        int currentBetUnit = initialBet;

        // Iterate through the results and skip states
        for (int i = 0; i < handResult.length(); i++) {
            char outcome = handResult.charAt(i);
            char skipOutcome = skipSequence.charAt(i);

            // Only proceed if not skipped
            if (skipOutcome == 'N') {
                if (outcome == 'W') {

                    currentBetUnit = initialBet;
                    // Reset the progression after a loss
                    currentStage = 0;


                } else if (outcome == 'L') {

                    // Move to the next stage in the progression
                    currentStage++;
                    if (currentStage == progression.length) {
                        // Reset progression if the 6-unit stage is reached
                        currentStage = 0;
                    }
                    currentBetUnit = progression[currentStage] * initialBet;
                }
            }
        }

        return currentBetUnit;  // Return the last progression value used
    }


    //1,1,2,2,4,4,8,8
    public static int high(GameResultResponse gameResultResponse) {

        String handResult = gameResultResponse.getHandResult().replace("null", "");
        String skipSequence = gameResultResponse.getSkipState();

        // Ensure both sequences have the same length to avoid errors
        if (handResult.isEmpty()) {
            return 1;
        }

        //Repeated Geometric Progression (RGP)
        int initialBet = 1;
        int[] progression = {1, 1, 2, 2, 4, 4, 8, 8}; // 8 steps
        int currentStage = 0;
        int currentBetUnit = initialBet;

        // Iterate through the results and skip states
        for (int i = 0; i < handResult.length(); i++) {
            char outcome = handResult.charAt(i);
            char skipOutcome = skipSequence.charAt(i);

            // Only proceed if not skipped
            if (skipOutcome == 'N') {
                if (outcome == 'W') {

                    currentBetUnit = initialBet;
                    // Reset the progression after a loss
                    currentStage = 0;


                } else if (outcome == 'L') {

                    // Move to the next stage in the progression
                    currentStage++;
                    if (currentStage == progression.length) {
                        // Reset progression if the 6-unit stage is reached
                        currentStage = 0;
                    }
                    currentBetUnit = progression[currentStage] * initialBet;
                }
            }
        }

        return currentBetUnit;  // Return the last progression value used
    }

    //1, 1, 2, 3, 5, 7
    public static int rLiza(GameResultResponse gameResultResponse) {

        String handResult = gameResultResponse.getHandResult().replace("null", "");
        String skipSequence = gameResultResponse.getSkipState();

        // Ensure both sequences have the same length to avoid errors
        if (handResult.isEmpty()) {
            return 1;
        }

        // 1-3-2-6 progression bets
        int initialBet = 1;
        int[] progression = {1, 1, 2, 3, 5, 7};
        int currentStage = 0;
        int currentBetUnit = initialBet;

        // Iterate through the results and skip states
        for (int i = 0; i < handResult.length(); i++) {
            char outcome = handResult.charAt(i);
            char skipOutcome = skipSequence.charAt(i);

            // Only proceed if not skipped
            if (skipOutcome == 'N') {
                if (outcome == 'W') {

                    currentBetUnit = initialBet;
                    // Reset the progression after a loss
                    currentStage = 0;


                } else if (outcome == 'L') {

                    // Move to the next stage in the progression
                    currentStage++;
                    if (currentStage == progression.length) {
                        // Reset progression if the 6-unit stage is reached
                        currentStage = 0;
                    }
                    currentBetUnit = progression[currentStage] * initialBet;
                }
            }
        }

        return currentBetUnit;  // Return the last progression value used
    }


    public static int reverseLabouchere(GameResultResponse gameResultResponse, int stopLossValue) {
        int stopLoss = Math.abs(stopLossValue);

        String handResult = gameResultResponse.getHandResult().replace("null", "");
        String skipSequence = gameResultResponse.getSkipState();

        // Ensure both sequences have the same length to avoid errors
        if (handResult.isEmpty()) {
            return 1;
        }

        final double initialBalance = 100.0;
        final int maxBetSequenceSize = 1000; // Optional: limit the max sequence size to avoid infinite loop

        // Initialize the bet amount sequence (can be customized)
        List<Integer> betAmountSequence = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));

        // Adjust the bet sequence if the sum exceeds the stop loss
        int sequenceSum = betAmountSequence.stream().mapToInt(Integer::intValue).sum();
        if (sequenceSum > stopLoss) {
            System.out.println("Adjusting sequence to fit stop loss.");
            int difference = sequenceSum - stopLoss;
            betAmountSequence.set(betAmountSequence.size() - 1, betAmountSequence.get(betAmountSequence.size() - 1) - difference);
        }

        double balance = initialBalance;

        // Iterate through each result in the betting sequence
        for (int i = 0; i < handResult.length(); i++) {
            char outcome = handResult.charAt(i);
            char skipOutcome = skipSequence.charAt(i);

            // Calculate the current bet amount (sum of the first and last elements in the sequence)
            int betAmount = betAmountSequence.size() > 1 ? betAmountSequence.get(0) + betAmountSequence.get(betAmountSequence.size() - 1) : betAmountSequence.get(0) * 2;
            System.out.println("Bet Amount: " + betAmount);

            if (skipOutcome == 'N') {
                // Process the current bet result
                if (outcome == 'W') {  // Win
                    balance += betAmount;
                    betAmountSequence.add(betAmount);  // Add bet to sequence on win
                } else if (outcome == 'L') {  // Loss
                    balance -= betAmount;
                    if (betAmountSequence.size() > 1) {
                        betAmountSequence.remove(0);  // Remove first element on loss
                        betAmountSequence.remove(betAmountSequence.size() - 1);  // Remove last element on loss
                    } else {
                        betAmountSequence.remove(0);  // Remove the only element if the sequence is down to one
                    }
                }
            }

            // Stop the betting if balance is less than or equal to the stop loss or if the sequence is empty
            if (balance <= 0 || betAmountSequence.isEmpty()) {
                System.out.println("Stop loss reached or sequence is empty.");
                return 0;
            }

            // Prevent too many iterations (optional safeguard against infinite loops)
            if (betAmountSequence.size() > maxBetSequenceSize) {
                System.out.println("Max sequence size reached.");
                return 0;
            }

//            // Return the next bet amount after processing the current bet
//            if (!betAmountSequence.isEmpty()) {
//                return betAmountSequence.size() > 1 ? betAmountSequence.get(0) + betAmountSequence.get(betAmountSequence.size() - 1) : betAmountSequence.get(0) * 2;
//            }
        }

        // Final results after processing the full betAmountSequence
        return 0;
    }

}
