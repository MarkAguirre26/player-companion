package com.virtual.app.sicbo.module.helper;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

public class SicBoMarkovChain {
    private final Map<String, Map<String, Integer>> transitionCounts;
    private final Map<String, Integer> stateCounts;

    public SicBoMarkovChain() {
        transitionCounts = new HashMap<>();
        stateCounts = new HashMap<>();
    }

    public static String getPrediction(String collection) {
        if (collection == null || collection.length() < 3) {
            throw new IllegalArgumentException("Input collection must contain at least three characters.");
        }

        SicBoMarkovChain markovChain = new SicBoMarkovChain();

        // Build the Markov chain from the sample data using IntStream
        IntStream.range(0, collection.length() - 1).forEach(i -> {
            String currentState = String.valueOf(collection.charAt(i));
            String nextState = String.valueOf(collection.charAt(i + 1));
            markovChain.addObservation(currentState, nextState);
        });

        // Get the last three states for prediction
        String lastState1 = String.valueOf(collection.charAt(collection.length() - 1)); // Most recent state
        String lastState2 = String.valueOf(collection.charAt(collection.length() - 2)); // Second most recent state
        String lastState3 = String.valueOf(collection.charAt(collection.length() - 3)); // Third most recent state

        // Predict the next state
        String predictedState = markovChain.predict(lastState1, lastState2, lastState3);
        System.out.println("Predicted next state after " + lastState3 + ", " + lastState2 + ", and " + lastState1 + ": " + predictedState);

        return predictedState;
    }

    // Method to add observations (states) to the Markov chain
    public void addObservation(String currentState, String nextState) {
        // For a third-order Markov chain, we need to store triplets of previous states
        String previousStates = getPreviousStates(currentState);

        // Initialize the transition map if not already present
        transitionCounts.putIfAbsent(previousStates, new HashMap<>());
        transitionCounts.get(previousStates).put(nextState,
                transitionCounts.get(previousStates).getOrDefault(nextState, 0) + 1);

        // Count the occurrence of the previous states
        stateCounts.put(previousStates, stateCounts.getOrDefault(previousStates, 0) + 1);
    }

    // Method to predict the next state based on the previous three states
    public String predict(String state1, String state2, String state3) {
        String key = state1 + state2 + state3; // Create the key for the transition map
        Map<String, Integer> transitions = transitionCounts.getOrDefault(key, new HashMap<>());

        // Find the most probable next state
        String mostProbableState = null;
        int maxCount = 0;

        for (Map.Entry<String, Integer> entry : transitions.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                mostProbableState = entry.getKey();
            }
        }

        return mostProbableState != null ? mostProbableState : "Unknown"; // Return "Unknown" if no predictions
    }

    // Helper method to get the previous states (in this example, using the last three states)
    private String getPreviousStates(String currentState) {
        return currentState.length() >= 3 ? currentState.substring(currentState.length() - 3) : currentState;
    }


}
