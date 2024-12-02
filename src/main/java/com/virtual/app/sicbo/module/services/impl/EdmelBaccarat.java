package com.virtual.app.sicbo.module.services.impl;

import com.virtual.app.sicbo.module.model.Pair;
import com.virtual.app.sicbo.module.model.PairPattern;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class EdmelBaccarat {
    // Stores counts for Banker ('b') and Player ('p')
    private static final Map<List<String>, Map<String, Integer>> patterns = new HashMap<>();

    public static void updatePatterns(List<String> history) {
        if (history.size() >= 4) {
            // Last 3 outcomes as the key
            List<String> key = new ArrayList<>(history.subList(history.size() - 4, history.size() - 1));
            String nextOutcome = history.get(history.size() - 1);

            // Update the pattern count
            patterns.putIfAbsent(key, new HashMap<>());
            patterns.get(key).put(nextOutcome, patterns.get(key).getOrDefault(nextOutcome, 0) + 1);
        }
    }

    public static Optional<PairPattern<Character, Double, String>> analyze(List<String> history) {
        if (history.size() < 3) {
            return Optional.of(new PairPattern<>('-', 0.0, "Need at least 3 outcomes to make a prediction."));
        }

        // Use the last 3 outcomes to predict
        List<String> key = new ArrayList<>(history.subList(history.size() - 3, history.size()));
        if (patterns.containsKey(key)) {
            // Get probabilities from the pattern knowledge base
            int bCount = patterns.get(key).getOrDefault("b", 0);
            int pCount = patterns.get(key).getOrDefault("s", 0);
            int total = bCount + pCount;

            if (total == 0) {
                return Optional.of(new PairPattern<>('-', 0.0, "Pattern exists but no data to determine probability."));
            }

            // Calculate probabilities
            double bProb = (double) bCount / total;
            double pProb = (double) pCount / total;

            // Make a decision
            if (bProb > pProb) {
                return Optional.of(new PairPattern<>('b', bProb, "Place your bet")); // Bet on Banker
            } else if (pProb > bProb) {
                return Optional.of(new PairPattern<>('s', pProb, "Place your bet")); // Bet on Player
            } else {
                return Optional.of(new PairPattern<>('-', 0.0, "No clear prediction. Skipping this round")); // Bet on Player
            }
        } else {

            return Optional.of(new PairPattern<>('-', 0.0, "Pattern not found. Skipping this round."));
        }
    }

    public Optional<PairPattern<Character, Double, String>> predict(String sequence) {

        List<String> history = new ArrayList<>();
        PairPattern<Character, Double, String> prediction = null;

        // Process the sequence and predict the next outcome
        for (char outcome : sequence.toCharArray()) {
            history.add(String.valueOf(outcome));
            if (history.size() >= 4) {
                updatePatterns(history);  // Learn patterns dynamically
            }
        }

        if (history.size() >= 4) {
            // Make a prediction based on the learned patterns
            Optional<PairPattern<Character, Double, String>> predictionResult = analyze(history);
            if (predictionResult.isPresent()) {
                prediction = predictionResult.get();
                // Output the prediction
                System.out.println("Prediction: Bet on " + (prediction.first == 'b' ? "Big" : "Small") +
                        " (Confidence: " + String.format("%.2f%%", prediction.second * 100) + ")");


            }
        } else {

            return Optional.of(new PairPattern<>('-', 0.0, "NO_PREDICTION"));
        }

        return Optional.ofNullable(prediction);
    }
}
