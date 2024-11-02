package com.virtual.app.sicbo.module.helper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class AdvancedMarkovDicePredictor {
    public static int[] convertListToArray(List<Integer> list) {
        return list.stream().mapToInt(Integer::intValue).toArray();
    }

    public static String getTopThreeNumbers(List<Integer> sequenceList) {


        int[] sequence = convertListToArray(sequenceList);
        // Step 1: Build the transition matrix
        Map<Integer, Map<Integer, Integer>> transitionCounts = new HashMap<>();

        for (int i = 0; i < sequence.length - 1; i++) {
            int current = sequence[i];
            int next = sequence[i + 1];

            transitionCounts.putIfAbsent(current, new HashMap<>());
            transitionCounts.get(current).put(next, transitionCounts.get(current).getOrDefault(next, 0) + 1);
        }

        // Step 2: Calculate probabilities
        Map<Integer, Map<Integer, Double>> transitionProbabilities = new HashMap<>();
        for (Map.Entry<Integer, Map<Integer, Integer>> entry : transitionCounts.entrySet()) {
            int current = entry.getKey();
            Map<Integer, Integer> counts = entry.getValue();
            int total = counts.values().stream().mapToInt(Integer::intValue).sum();

            Map<Integer, Double> probabilities = new HashMap<>();
            for (Map.Entry<Integer, Integer> countEntry : counts.entrySet()) {
                probabilities.put(countEntry.getKey(), (double) countEntry.getValue() / total);
            }
            transitionProbabilities.put(current, probabilities);
        }

        // Step 3: Predict the top 3 next states based on the last number in the sequence
        int lastNumber = sequence[sequence.length - 1];
        PriorityQueue<Map.Entry<Integer, Double>> maxHeap = new PriorityQueue<>(
                (a, b) -> Double.compare(b.getValue(), a.getValue())); // Max heap for top probabilities

        if (transitionProbabilities.containsKey(lastNumber)) {
            for (Map.Entry<Integer, Double> entry : transitionProbabilities.get(lastNumber).entrySet()) {
                maxHeap.offer(entry);
            }
        }

        // Step 4: Get the top 3 predictions and build the response string
        StringBuilder response = new StringBuilder();
        for (int i = 0; i < 3 && !maxHeap.isEmpty(); i++) {
            Map.Entry<Integer, Double> entry = maxHeap.poll();
            response.append(String.format("%d,", entry.getKey()));
        }

        return response.toString();
    }

}
