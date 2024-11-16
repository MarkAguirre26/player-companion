package com.virtual.app.sicbo.module.services.impl;

import com.virtual.app.sicbo.module.model.Pair;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MarkovChain {

    private int CHUNK_SIZE = 0;


    /**
     * Predicts the next character based on the current character.
     *
     * @return an Optional containing a Pair of the predicted character and its probability,
     * or empty if no prediction is possible.
     */
    public Optional<Pair<Character, Double>> predictNext(String sequenceInput, int chunkSize) {

        this.CHUNK_SIZE = chunkSize;



        // Get the last chunk of the sequence for prediction
        String sequence = getSequenceChunk(sequenceInput);
        System.out.println(sequence);
        if (sequence.isEmpty()) {
            throw new IllegalArgumentException("Sequence cannot be empty for training.");
        }

        Map<Character, Map<Character, Integer>> transitions = new HashMap<>();
        Map<Character, Integer> totalCounts = new HashMap<>();

        // Build the transition model
        for (int i = 0; i < sequence.length() - 1; i++) {
            char current = sequence.charAt(i);
            char next = sequence.charAt(i + 1);
            transitions.computeIfAbsent(current, k -> new HashMap<>())
                    .merge(next, 1, Integer::sum);
            totalCounts.merge(current, 1, Integer::sum);
        }

        char currentChar = sequence.charAt(sequence.length() - 1);
        Map<Character, Integer> nextCharCounts = transitions.getOrDefault(currentChar, Collections.emptyMap());

        if (nextCharCounts.isEmpty()) {
            // Fallback: If no transition exists, use the most frequent character
            char fallbackChar = getMostFrequentCharacter(sequence);
            return Optional.of(new Pair<>(fallbackChar, 1.0));
        }

        // Apply Laplace smoothing and calculate probabilities
        double totalCount = totalCounts.getOrDefault(currentChar, 1); // Avoid division by zero
        double smoothedTotal = totalCount + nextCharCounts.size(); // Laplace smoothing

        return nextCharCounts.entrySet().stream()
                .map(entry -> {
                    double smoothedProbability = (entry.getValue() + 1.0) / smoothedTotal; // Laplace smoothing applied
                    return new Pair<>(entry.getKey(), smoothedProbability);
                })
                .max(Comparator.comparingDouble(pair -> pair.first));
    }

    /**
     * Returns the last x characters from the sequence for prediction, or the entire sequence if shorter.
     */
    private String getSequenceChunk(String sequenceInput) {
        String sequence = "";
        if (sequenceInput.length() > CHUNK_SIZE) {
            sequence = sequenceInput.substring((sequenceInput.length() / CHUNK_SIZE) * CHUNK_SIZE);

            if (sequence.isEmpty()) {
                sequence = sequenceInput.substring(sequenceInput.length() - 1);
            }

        } else {
            sequence = sequenceInput;
        }
        return sequence;
    }

    /**
     * Returns the most frequent character in the sequence.
     */
    private char getMostFrequentCharacter(String sequence) {
        Map<Character, Integer> frequencyMap = new HashMap<>();
        for (char c : sequence.toCharArray()) {
            frequencyMap.merge(c, 1, Integer::sum);
        }
        return frequencyMap.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElseThrow(() -> new IllegalStateException("No characters in the sequence.")); // Should never happen
    }
}